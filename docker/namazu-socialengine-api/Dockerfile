FROM jetty:9.4-jre8

ARG REST_WAR
RUN mkdir -p "/opt/socialengine/script-repos"
COPY $REST_WAR /var/lib/jetty/webapps/api.war
