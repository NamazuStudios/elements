# base image
#FROM nginx:1.15.5-alpine
FROM nginx:1.15.5

ARG WEB_UI_DIR
COPY $WEB_UI_DIR /usr/share/nginx/html

# expose port 8080
EXPOSE 8080

COPY default.conf /etc/nginx/conf.d