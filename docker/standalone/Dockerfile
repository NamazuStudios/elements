FROM jetty:9.4-jre8
MAINTAINER Patrick Twohig <patrick@namazustudios.com>

ARG REST_WAR
ARG WEB_UI_WAR
ARG SETUP_JAR

RUN apt-get update
RUN apt-get install -y mongodb

RUN mkdir /opt/socialengine
RUN mkdir /opt/socialengine/mongodb

COPY socialengine-entrypoint.bash /opt/socialengine/socialengine-entrypoint.bash
RUN chmod u+x /opt/socialengine/socialengine-entrypoint.bash

COPY $SETUP_JAR /opt/socialengine/setup.jar
COPY $REST_WAR /var/lib/jetty/webapps/api.war
COPY $WEB_UI_WAR /var/lib/jetty/webapps/web-ui.war

ENTRYPOINT /opt/socialengine/socialengine-entrypoint.bash
