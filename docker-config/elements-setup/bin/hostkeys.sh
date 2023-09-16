#!/usr/bin/env bash

SSHD_CONFIG_DIR="${SSHD_CONFIG_DIR:-${ELEMENTS_CONF}}"

echo 'y' | ssh-keygen -f "${SSHD_CONFIG_DIR}/ssh_host_rsa_key" -N '' -t rsa
echo 'y' | ssh-keygen -f "${SSHD_CONFIG_DIR}/ssh_host_dsa_key" -N '' -t dsa
echo 'y' | ssh-keygen -f "${SSHD_CONFIG_DIR}/ssh_host_ecdsa_key" -N '' -t ecdsa -b 521

chmod go-rwx "${SSHD_CONFIG_DIR}/ssh_host_rsa_key"
chmod go-rwx "${SSHD_CONFIG_DIR}/ssh_host_dsa_key"
chmod go-rwx "${SSHD_CONFIG_DIR}/ssh_host_ecdsa_key"
