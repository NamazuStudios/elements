#!/usr/bin/env bash

# Converts the dot-environment variable syntax to the ENVIRONMENT_VARIABLE convention. This honors only
# those environment variables which start with com.namazustudios. This is useful to prevent

while IFS='=' read -r name value ; do
  if [[ $name == 'com.namazustudios'* ]]; then
    remapped="$(echo "$name" | tr . _)"
    export "$remapped"="$value"
    echo "$remapped"="$value"
  fi
done < <(env)

# Just-in-time, we write the files to the appropriate location on disk where nginx will serve the assets
# in the UI properly as well as configure the port and server names.

envsubst < "$ELEMENTS_CONF/index.html.template" > /usr/share/nginx/html/index.html
envsubst < "$ELEMENTS_CONF/default.conf.template" > /etc/nginx/conf.d/default.conf
envsubst < "$ELEMENTS_CONF/config.json.template" > /usr/share/nginx/html/assets/config.json

# Actually runs NGINX
nginx -g "daemon off;"
