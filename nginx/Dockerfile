FROM nginx:1.27-alpine

# Pure gateway now: reverse-proxies to the backend and frontend services,
# no static assets baked in here (see frontend/Dockerfile for that).
COPY nginx.conf /etc/nginx/nginx.conf
