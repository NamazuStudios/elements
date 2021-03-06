FROM nginx:1.15.5

ARG WEB_UI_DIR
COPY $WEB_UI_DIR /usr/share/nginx/html

EXPOSE 8080

COPY default.conf /etc/nginx/conf.d
