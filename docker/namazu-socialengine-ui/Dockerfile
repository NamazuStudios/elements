FROM ubuntu:20.04

# Home Directory
ENV ELEMENTS_HOME "/opt/elements"
ENV NGINX_VERSION 1.18.0-0ubuntu1.2

# Sub Directories

# Application Directories. Used by the deployment scripts.
# - ELEMENTS_BIN holds any binaries the application may need. Added to the OS PATH
# - ELEMETNS_CONF houes any configuration files used by the service (Including elements.properties)

ENV ELEMENTS_BIN "$ELEMENTS_HOME/bin"
ENV ELEMENTS_CONF "$ELEMENTS_HOME/conf"

RUN mkdir -p $ELEMENTS_BIN && \
    mkdir -p $ELEMENTS_CONF && \
    apt-get update && \
    apt-get install -y nginx-light=1.18.0-0ubuntu1.2 && \
    apt-get install -y gettext=0.19.8.1-10build1

ARG WEB_UI_DIR
COPY $WEB_UI_DIR /usr/share/nginx/html

EXPOSE 8080

COPY bin/nginx.sh "$ELEMENTS_BIN"
COPY conf/config.json.template "$ELEMENTS_CONF"
COPY conf/default.conf.template "$ELEMENTS_CONF"

RUN sed 's#<base href="/web-ui/">#<base href="/\${com_namazustudios_socialengine_http_path_prefix}/">#' \
        "/usr/share/nginx/html/index.html" > \
        "$ELEMENTS_CONF/index.html.template" && \
    chmod ugo+x "$ELEMENTS_BIN/nginx.sh"

WORKDIR $ELEMENTS_HOME

ENTRYPOINT $ELEMENTS_BIN/nginx.sh
