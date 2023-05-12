#!/usr/bin/env bash

# Before all else, we ensure we're running in a proper environment.

echo "Original Environment"
echo ""
env
echo ""

# Converts the dot-environment variable syntax to the ENVIRONMENT_VARIABLE convention. This honors only
# those environment variables which start with com.namazustudios

# shellcheck disable=SC2034

export dev_getelements_socialengine_dollar='$'

if [ -z ${dev_getelements_socialengine_http_port+x} ]
then
  export dev_getelements_socialengine_http_port=8080
fi

if [ -z ${dev_getelements_socialengine_http_path_prefix+x} ]
then
  export dev_getelements_socialengine_http_path_prefix=web-ui
fi

if [ -z ${dev_getelements_socialengine_dns_name+x} ]
then
  export dev_getelements_socialengine_dns_name=localhost:8080
fi

if [ -z ${dev_getelements_socialengine_api_url+x} ]
then
  export dev_getelements_socialengine_api_url=http://localhost:8080/api/rest
fi

index_html="/usr/share/nginx/html/index.html"
default_conf="/etc/nginx/conf.d/default.conf"
config_json="/usr/share/nginx/html/assets/config.json"

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
