FROM ubuntu:16.04
MAINTAINER Patrick Twohig <patrick@namazustudios.com>

RUN apt-get update
RUN apt-get install -y mongodb
RUN apt-cache search jetty
RUN apt-get install -y jetty9

COPY $restWar  /var/lib/jetty/webapps/api.war
COPY $webUiWar /var/lib/jetty/webapps/web-ui.war
