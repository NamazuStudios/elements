#!/usr/bin/env bash

# A simple bash script which runs dnsmasq on the localhost (port 5353) and allows for quick and easy
# modifications to the SRV records on the fly. This can easily simulate DNS record changes in AWS/GCP
# for when app-node instances are taken offline.

echo "Enter ports to serve: "

while true
do

  IFS=' ' read -ra ports

  if [[ -n $last_pid ]]
  then
    echo "Killing dnsmasq pid ${last_pid}"
    kill "$last_pid"
  fi

  params=()
  records=()

  for port in "${ports[@]}"
  do

    if ! [[ $port =~ ^[0-9]+$ ]]
    then
      echo "Invalid port. Quitting."
      exit 0
    fi

    record="_elements._tcp.internal,127.0.0.1,${port},1"
    records+=("${record}")

    params+=("-W")
    params+=("${record}")

  done

  echo
  echo "Using SRV Records:"

  for record in "${records[@]}"
  do
    echo "  ${record}"
  done

  dnsmasq -d --port=5353 "${params[@]}" &
  last_pid=$!
  
  echo
  echo "Launched with pid ${last_pid}"

done

