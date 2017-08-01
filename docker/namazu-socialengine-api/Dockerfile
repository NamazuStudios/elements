FROM jetty:9.4-jre8

ARG REST_WAR

RUN mkdir -p "/opt/socialengine"
COPY $REST_WAR /var/lib/jetty/webapps/api.war
