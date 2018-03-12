FROM jetty

ARG CODE_SERVE_WAR

USER root:root
RUN mkdir -p "/opt/socialengine/script-repos"
RUN chown -R jetty:jetty /opt/socialengine/script-repos

USER jetty:jetty
COPY $CODE_SERVE_WAR /var/lib/jetty/webapps/code-serve.war
