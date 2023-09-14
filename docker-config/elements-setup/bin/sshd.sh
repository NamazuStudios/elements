#!/usr/bin/env bash

run_sshd=/run/sshd
sshd_pid=/run/sshd.pid
pam_env_conf=/etc/security/pam_env.conf

if [[ -z "${ELEMENTS_CONF}" ]];
then
  echo "ELEMENTS_CONF not specified."
  exit 1
fi

sshd_conf_dir="${ELEMENTS_CONF}"

ssh_host_rsa_key="${sshd_conf_dir}/ssh_host_rsa_key"
ssh_host_dsa_key="${sshd_conf_dir}/ssh_host_dsa_key"
ssh_host_ecdsa_key="${sshd_conf_dir}/ssh_host_ecdsa_key"
authorized_keys="${sshd_conf_dir}/authorized_keys"

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

chown root:root "${sshd_conf_dir}/authorized_keys"

chmod -f 600 "${ssh_host_rsa_key}"
chmod -f 600 "${ssh_host_dsa_key}"
chmod -f 600 "${ssh_host_ecdsa_key}"
chmod -f 644 "${authorized_keys}"

/usr/sbin/sshd -D -e -f "${sshd_conf_dir}/sshd_config"
rm -f "$sshd_pid"
