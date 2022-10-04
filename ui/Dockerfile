FROM nginx
COPY ./nginx-config /etc/nginx/conf.d/default.conf

RUN mkdir -p /app/frontend
ADD ./build /app/frontend
