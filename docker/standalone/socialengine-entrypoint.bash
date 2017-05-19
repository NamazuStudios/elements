#!/bin/sh

setup_file="/opt/socialengine/setup-complete"

mongo_db_path="/opt/socialengine/mongodb"
mongo_db_log_path="/opt/socialengine/socialengine-mongodb.log"
mongo_db_pidfile="/opt/socialengine/mongodb.pid"

echo "Starting MongoDB"
mongod --fork --dbpath ${mongo_db_path} --syslog --pidfilepath ${mongo_db_pidfile}

status=$?

if [ ${status} = 0 ]
then
	echo "Successfully started MongoDB"
else
	echo "Failed to start MongoDB.  Status ${status}"
	exit $status
fi

echo "Checking for Setup."

if [ -f ${setup_file} ]
then
	echo "Found Setup File.  Skipping Setup."
else

	echo "Setup Not Found."

	java -jar /opt/socialengine/setup.jar \
		add-user \
		-email=root@namazustudios.net \
		-user=root -password=root \
		-level=SUPERUSER \
		--strict false

	status=$?

	echo "Setup Exit Status ${status}"

	if [ ${status} = 0 ]
	then
		echo "Setup Complete.  Writing Setup File."
		touch ${setup_file}
		echo "Wrote Setup File."
	else
		echo "Setup failed with status ${status}"
		exit $status
	fi

fi

echo "Starting Jetty."
/docker-entrypoint.sh

echo "Jetty Stopped.  Shutting down MongoDB."
mongod --shutdown --pidfilepath ${mongo_db_pidfile}

status=$?

if [[$status != 0]]
then
	echo "Failed to cleanly stop MongoDB:  Status ${status}"
	exit $status
fi
