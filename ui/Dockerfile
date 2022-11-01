FROM nginx:1.23.2
COPY ./nginx-config /etc/nginx/conf.d/default.conf

RUN mkdir -p /app/frontend
COPY ./build /app/frontend
