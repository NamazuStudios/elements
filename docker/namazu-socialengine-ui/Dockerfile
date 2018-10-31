# base image
#FROM nginx:1.15.5-alpine
FROM nginx:1.15.5

ARG WEB_UI_DIR
COPY $WEB_UI_DIR /usr/share/nginx/html

# expose port 8080
EXPOSE 8080

RUN sed -i 's/listen.*80;/listen\t8080;/' /etc/nginx/conf.d/default.conf

# run nginx
CMD ["nginx", "-g", "daemon off;"]