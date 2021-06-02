#!/usr/bin/env bash

# Converts the dot-environment variable syntax to the ENVIRONMENT_VARIABLE convention. This honors only
# those environment variables which start with com.namazustudios

index_html="/usr/share/nginx/html/index.html"
default_conf="/etc/nginx/conf.d/default.conf"
config_json="/usr/share/nginx/html/assets/config.json"

echo "Original Environment"
echo ""
env
echo ""

while IFS='=' read -r name value ; do
  if [[ $name == 'com.namazustudios'* ]]; then
    remapped="$(echo "$name" | tr . _)"
    export "$remapped"="$value"
    echo "$name -> $remapped = $value"
  fi
done < <(env)

echo "Remapped Environment Variables. Current environment: "
echo ""
env
echo ""

# Just-in-time, we write the files to the appropriate location on disk where nginx will serve the assets
# in the UI properly as well as configure the port and server names.

echo "Generated '${index_html}'"
echo BOF
envsubst < "$ELEMENTS_CONF/index.html.template"
echo EOF
echo

echo "Generated '${default_conf}'"
echo BOF
envsubst < "$ELEMENTS_CONF/default.conf.template"
echo EOF
echo

echo "Generated '${config_json}'"
echo BOF
envsubst < "$ELEMENTS_CONF/config.json.template"
echo EOF
echo

envsubst < "$ELEMENTS_CONF/index.html.template" > "${index_html}"
envsubst < "$ELEMENTS_CONF/default.conf.template" > "${default_conf}"
envsubst < "$ELEMENTS_CONF/config.json.template" > "${config_json}"

nginx -g "daemon off;"
