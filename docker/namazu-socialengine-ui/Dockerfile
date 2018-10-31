# base image
#FROM nginx:1.15.5-alpine
FROM nginx:1.15.5

ARG WEB_UI_DIR
COPY $WEB_UI_DIR /usr/share/nginx/html

# need to change nginx to serve on port 8080 (update config)
# expose port 8080
# finally, need to update scripts to create/update config.json
EXPOSE 8080

# run nginx
CMD ["nginx", "-g", "daemon off;"]