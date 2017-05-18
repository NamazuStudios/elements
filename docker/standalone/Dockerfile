FROM ubuntu:16.04
MAINTAINER Patrick Twohig <patrick@namazustudios.com>

ARG REST_WAR
ARG WEB_UI_WAR
ARG SETUP_JAR

RUN apt-get update
RUN apt-get install -y mongodb
RUN apt-get install -y jetty9

RUN mkdir /opt/socialengine

COPY $SETUP_JAR /opt/socialengine/setup.jar
COPY $REST_WAR /var/lib/jetty/webapps/api.war
COPY $WEB_UI_WAR /var/lib/jetty/webapps/web-ui.war
