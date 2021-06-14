#!/usr/bin/env bash

echo 'y' | ssh-keygen -f "$ELEMENTS_CONF/ssh_host_rsa_key" -N '' -t rsa
echo 'y' | ssh-keygen -f "$ELEMENTS_CONF/ssh_host_dsa_key" -N '' -t dsa
echo 'y' | ssh-keygen -f "$ELEMENTS_CONF/ssh_host_ecdsa_key" -N '' -t ecdsa -b 521

chmod go-rwx "$ELEMENTS_CONF/ssh_host_rsa_key"
chmod go-rwx "$ELEMENTS_CONF/ssh_host_dsa_key"
chmod go-rwx "$ELEMENTS_CONF/ssh_host_ecdsa_key"
