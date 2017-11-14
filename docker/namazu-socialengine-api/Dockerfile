FROM jetty:9.4-jre8

ARG REST_WAR

USER root
RUN mkdir -p "/opt/socialengine/script-repos"
RUN chown --recursive jetty:jetty /opt/socialengine/script-repos
USER jetty

COPY $REST_WAR /var/lib/jetty/webapps/api.war
