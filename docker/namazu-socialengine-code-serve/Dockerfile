FROM jetty:9.4-jre8

ARG CODE_SERVE_WAR

RUN mkdir -p "/opt/socialengine/script-repos"
RUN chown -R jetty:jetty /opt/socialengine/script-repos

COPY $CODE_SERVE_WAR /var/lib/jetty/webapps/code-serve.war
