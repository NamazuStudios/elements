# base image
#FROM nginx:1.15.5-alpine
FROM nginx:1.15.5

ARG WEB_UI_DIR
COPY $WEB_UI_DIR /usr/share/nginx/html

# expose port 80
EXPOSE 80

# run nginx
CMD ["nginx", "-g", "daemon off;"]