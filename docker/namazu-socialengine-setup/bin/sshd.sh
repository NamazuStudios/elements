#!/usr/bin/env bash

run_sshd=/run/sshd
sshd_pid=/run/sshd.pid
pam_env_conf=/etc/security/pam_env.conf

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
env | grep '^com[\._]namazustudios[\._].*' >> "$pam_env_conf"

echo "PAM Environment."
cat "$pam_env_conf"

chown root:root "$ELEMENTS_CONF/authorized_keys"
chmod 644 "$ELEMENTS_CONF/authorized_keys"

/usr/sbin/sshd -D -e -f "$ELEMENTS_CONF/sshd_config"
rm -f "$sshd_pid"
