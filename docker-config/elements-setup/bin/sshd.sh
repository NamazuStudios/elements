#!/usr/bin/env bash

run_sshd=/var/run/sshd
sshd_pid=/var/run/sshd.pid
pam_env_conf=/etc/security/pam_env.conf

if [[ -z "${ELEMENTS_CONF}" ]];
then
  echo "ELEMENTS_CONF not specified."
  exit 1
fi

SSHD_CONFIG_DIR="${SSHD_CONFIG_DIR:-${ELEMENTS_CONF}/sshd}"
SSHD_CONFIG="${SSHD_CONFIG:-${SSHD_CONFIG_DIR}/sshd_config}"

echo "SSHD_CONFIG_DIR: ${SSHD_CONFIG_DIR}"
echo "SSHD_CONFIG: ${SSHD_CONFIG}"

if [ -f "$sshd_pid" ]
then
    kill "$(cat "$sshd_pid")" > /dev/null 2>&1
    rm -f "$sshd_pid"
fi

rm -rf "$run_sshd"
mkdir -p "$run_sshd"
chown root:root "$run_sshd"
chmod go-rwx "$run_sshd"

echo "CLASSPATH=$CLASSPATH" > "$pam_env_conf"
env | grep "ELEMENTS_*" >> "$pam_env_conf"
env | grep '^dev[\._]getelements[\._].*' >> "$pam_env_conf"

echo "PAM Environment."
cat "$pam_env_conf"

/usr/sbin/sshd -D -e -f "${SSHD_CONFIG}"
rm -f "$sshd_pid"
