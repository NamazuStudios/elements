FROM jetty:9.4-jre8

RUN mkdir -p "/opt/socialengine"

ARG WEB_UI_WAR
COPY $WEB_UI_WAR /var/lib/jetty/webapps/web-ui.war
