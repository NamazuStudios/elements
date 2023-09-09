#!/usr/bin/env bash

sshd_conf_dir="${ELEMENTS_CONF}/sshd"

echo 'y' | ssh-keygen -f "${sshd_conf_dir}/ssh_host_rsa_key" -N '' -t rsa
echo 'y' | ssh-keygen -f "${sshd_conf_dir}/ssh_host_dsa_key" -N '' -t dsa
echo 'y' | ssh-keygen -f "${sshd_conf_dir}/ssh_host_ecdsa_key" -N '' -t ecdsa -b 521

chmod go-rwx "${sshd_conf_dir}/ssh_host_rsa_key"
chmod go-rwx "${sshd_conf_dir}/ssh_host_dsa_key"
chmod go-rwx "${sshd_conf_dir}/ssh_host_ecdsa_key"
