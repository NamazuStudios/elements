FROM jetty:9.4-jre8

ARG REST_WAR


USER root:root
RUN mkdir -p "/opt/socialengine/script-repos"
RUN chown --recursive jetty:jetty /opt/socialengine/script-repos
USER jetty:jetty

RUN mkdir -p "/opt/socialengine"
COPY $REST_WAR /var/lib/jetty/webapps/api.war
