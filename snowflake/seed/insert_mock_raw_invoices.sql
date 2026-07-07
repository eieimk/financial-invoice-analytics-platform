-- Mock seed data for raw_invoice_ocr - lets the rest of the pipeline
-- (streams -> tasks -> star schema -> dashboard) be tested without the
-- S3/Snowpipe wiring. id and updatetimestamp fill in from table defaults.
-- Strings use $$ dollar-quoting so the embedded JSON needs no escaping.
--
-- Row 1: the real invoice from sample-data/invoices_ocr.csv (line items sum
--        to the stated total: 46.55+15.40+39.00+110.00+22.00 = 232.95).
-- Row 2: clean synthetic invoice (120.00+80.50 = 200.50, matches total).
-- Row 3: deliberate mismatch (300.00+150.00 = 450.00 vs stated 480.00) so
--        reconciliation_exception_task has a discrepancy to flag.

USE DATABASE INVOICE_ANALYTICS;
USE SCHEMA CORE;

INSERT INTO raw_invoice_ocr (json_data)
VALUES
($$
{
  "invoice": {
    "client_name": "Clark-Foster",
    "client_address": "77477 Troy Cliff Apt. 853\nWashingtonbury, MS 78346",
    "seller_name": "Nguyen-Roach",
    "seller_address": "247 David Highway\nLake John, WV 84178",
    "invoice_number": "84652373",
    "invoice_date": "02/23/2021",
    "due_date": ""
  },
  "items": [
    { "description": "Stemware Rack Display Kitchen Wine Glass Holder", "quantity": "1.00", "total_price": "46.55" },
    { "description": "VTG (4) 7 Ounce Since 1852 Milk Bottle Wine Carafe", "quantity": "1.00", "total_price": "15.40" },
    { "description": "Vintage Crystal Red Wine Glasses NOS West Germany", "quantity": "1.00", "total_price": "39.00" },
    { "description": "3 Ikea Stainless Steel 4-bottle Wine Rack", "quantity": "4.00", "total_price": "110.00" },
    { "description": "Lolita Wine Bouquet Hand Painted Wine Glass NIB", "quantity": "1.00", "total_price": "22.00" }
  ],
  "subtotal": { "tax": "21.18", "discount": "", "total": "232.95" },
  "payment_instructions": { "due_date": "", "bank_name": "", "account_number": "", "payment_method": "" }
}
$$),
($$
{
  "invoice": {
    "client_name": "Meridian Trading Co",
    "client_address": "12 Harbor Street\nPortland, OR 97201",
    "seller_name": "Blue Ridge Supplies",
    "seller_address": "88 Mountain View Road\nAsheville, NC 28801",
    "invoice_number": "10000001",
    "invoice_date": "03/15/2021",
    "due_date": "04/14/2021"
  },
  "items": [
    { "description": "Office chairs, ergonomic", "quantity": "2.00", "total_price": "120.00" },
    { "description": "Standing desk converter", "quantity": "1.00", "total_price": "80.50" }
  ],
  "subtotal": { "tax": "18.23", "discount": "", "total": "200.50" },
  "payment_instructions": { "due_date": "04/14/2021", "bank_name": "First National", "account_number": "552-88-1907", "payment_method": "wire" }
}
$$),
($$
{
  "invoice": {
    "client_name": "Hollis and Sons",
    "client_address": "301 Elm Court\nDayton, OH 45402",
    "seller_name": "Cascade Industrial",
    "seller_address": "1500 Riverfront Ave\nSpokane, WA 99201",
    "invoice_number": "10000002",
    "invoice_date": "03/20/2021",
    "due_date": "04/19/2021"
  },
  "items": [
    { "description": "Hydraulic pump assembly", "quantity": "1.00", "total_price": "300.00" },
    { "description": "Pressure gauge kit", "quantity": "3.00", "total_price": "150.00" }
  ],
  "subtotal": { "tax": "43.64", "discount": "", "total": "480.00" },
  "payment_instructions": { "due_date": "04/19/2021", "bank_name": "Union Bank", "account_number": "771-24-6630", "payment_method": "check" }
}
$$);

-- Then run the pipeline immediately instead of waiting for the schedules:
--   EXECUTE TASK load_star_schema_task;
--   EXECUTE TASK reconciliation_exception_task;
--   ALTER DYNAMIC TABLE item_detail REFRESH;
--   ALTER DYNAMIC TABLE daily_summary REFRESH;
--
-- Verify:
--   SELECT * FROM fact_invoice;                       -- expect 3 rows
--   SELECT * FROM fact_invoice_line;                  -- expect 9 rows
--   SELECT * FROM fact_reconciliation_exception
--     WHERE is_discrepancy = TRUE;                    -- expect invoice 10000002 (30.00 gap)
