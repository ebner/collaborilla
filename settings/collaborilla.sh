# $Id$

# CollaborillaService start script
# (c) 2008, Hannes Ebner <hebner@nada.kth.se>

#!/bin/bash

DEPLOYDIR=/var/collaborilla-rest
LOGFILE=/var/log/collaborilla-rest.log
PIDFILE=/var/run/collaborilla-rest.pid

NAME=CollaborillaApplication
CONFIGFILE=collaborilla.properties

pushd $DEPLOYDIR >/dev/null 2>&1

case "$1" in
	start)
		echo -n "Starting $NAME... "
		
		if [ -f $PIDFILE ]
		then
			echo -e "\n\nPID file found: $PIDFILE. Is another instance of Collaborilla running?"
			echo "Aborting."
			exit 1
		fi

		java -cp lib/commons-logging.jar:lib/com.noelios.restlet.ext.net.jar:lib/com.noelios.restlet.ext.servlet_2.5.jar:lib/com.noelios.restlet.ext.simple_3.1.jar:lib/com.noelios.restlet.jar:lib/icu4j.jar:lib/iri.jar:lib/jdom-1.0.jar:lib/jena.jar:lib/jldap.jar:lib/org.json.jar:lib/org.restlet.ext.json_2.0.jar:lib/org.restlet.ext.wadl_1.0.jar:lib/org.restlet.jar:lib/org.simpleframework.jar:lib/rome-0.9.jar:lib/xercesImpl.jar:collaborilla.jar \
			se.kth.nada.kmr.collaborilla.rest.CollaborillaApplication \
			--config=$CONFIGFILE \
			>>$LOGFILE 2>&1 &
		echo $! >$PIDFILE
		echo "done."
		;;

	stop)
		echo -n "Stopping $NAME... "

		if [ ! -f $PIDFILE ]
		then
			echo "No PID file found!"
			exit 1
		fi

		kill -15 `cat $PIDFILE`
		rm $PIDFILE
		echo "done."
		;;

	restart)
		echo -n "Restarting $NAME... "
		$0 stop >/dev/null
		sleep 2
		$0 start >/dev/null
		echo " done."
		;;
	
	*)
		echo "Usage: $0 {start|stop|restart}" >&2
		exit 1
		;;
esac

popd >/dev/null 2>&1
