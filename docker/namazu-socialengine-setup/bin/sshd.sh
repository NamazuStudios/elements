#!/usr/bin/env bash

run_sshd=/run/sshd
sshd_pid=/run/sshd.pid
pam_env_conf=/etc/security/pam_env.conf

if [[ -z "${ELEMENTS_CONF}" ]];
then
  echo "No file specified. Exiting."
  exit 1
fi

ssh_host_rsa_key="${ELEMENTS_CONF}/ssh_host_rsa_key"
ssh_host_dsa_key="${ELEMENTS_CONF}/ssh_host_dsa_key"
ssh_host_ecdsa_key="${ELEMENTS_CONF}/ssh_host_ecdsa_key"
authorized_keys="${ELEMENTS_CONF}/authorized_keys"

function to_file() {

  if [[ -n "${1}" ]];
  then

      if [[ -z "${2}" ]];
      then
        echo "No file specified. Exiting."
        exit 1
      fi

    echo "Writing secret to ${2}"
    echo "${1}" > "${2}"
    status=$?
    [ "${status}" -eq 0 ] || exit $status

  else
    echo "Secret not defined for ${2}"
  fi

}

function decode_base64_to_file() {

  if [[ -n "${1}" ]];
  then

      if [[ -z "${2}" ]];
      then
        echo "No file specified. Exiting."
        exit 1
      fi

    echo "Writing secret to ${2}"
    echo "${1}" | base64 --decode > "${2}"
    status=$?
    [ "${status}" -eq 0 ] || exit $status

  else
    echo "Secret not defined for ${2}"
  fi

}

if [ -f "$sshd_pid" ]
then
    kill "$(cat "$sshd_pid")" > /dev/null 2>&1
    rm -f "$sshd_pid"
fi

rm -rf "$run_sshd"
mkdir -p "$run_sshd"
chown root:root "$run_sshd"
chmod go-rwx "$run_sshd"

to_file "${SECRET_ELEMENTS_SSH_HOST_RSA_KEY}" "$ELEMENTS_CONF/ssh_host_rsa_key"
to_file "${SECRET_ELEMENTS_SSH_HOST_DSA_KEY}" "$ELEMENTS_CONF/ssh_host_dsa_key"
to_file "${SECRET_ELEMENTS_SSH_HOST_ECDSA_KEY}" "$ELEMENTS_CONF/ssh_host_ecdsa_key"
to_file "${SECRET_ELEMENTS_SSH_AUTHORIZED_KEYS}" "$ELEMENTS_CONF/authorized_keys"

decode_base64_to_file "${SECRET_ELEMENTS_SSH_HOST_RSA_KEY_BASE64}" "$ELEMENTS_CONF/ssh_host_rsa_key"
decode_base64_to_file "${SECRET_ELEMENTS_SSH_HOST_DSA_KEY_BASE64}" "$ELEMENTS_CONF/ssh_host_dsa_key"
decode_base64_to_file "${SECRET_ELEMENTS_SSH_HOST_ECDSA_KEY_BASE64}" "$ELEMENTS_CONF/ssh_host_ecdsa_key"
decode_base64_to_file "${SECRET_ELEMENTS_SSH_AUTHORIZED_KEYS_BASE64}" "$ELEMENTS_CONF/authorized_keys"

echo "CLASSPATH=$CLASSPATH" > "$pam_env_conf"
env | grep "ELEMENTS_*" >> "$pam_env_conf"
env | grep '^com[\._]namazustudios[\._].*' >> "$pam_env_conf"

echo "PAM Environment."
cat "$pam_env_conf"

chown root:root "$ELEMENTS_CONF/authorized_keys"

chmod 600 "${ssh_host_rsa_key}"
chmod 600 "${ssh_host_dsa_key}"
chmod 600 "${ssh_host_ecdsa_key}"
chmod 644 "${authorized_keys}"

/usr/sbin/sshd -D -e -f "$ELEMENTS_CONF/sshd_config"
rm -f "$sshd_pid"
