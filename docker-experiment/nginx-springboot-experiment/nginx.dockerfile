FROM nginx
ADD nginx.conf /etc/nginx/nginx.conf
ADD web/ /tmp/web/
