# $Id$

# CollaborillaService start script
# (c) 2007, Hannes Ebner <hebner@nada.kth.se>

#!/bin/sh

LOGFILE=/var/log/collaborilla.log
PIDFILE=/var/run/collaborilla.pid

NAME=CollaborillaService
CONFIGFILE=collaborilla.properties

PRG=$0

while [ -h "$PRG" ]
do
	ls=`ls -ld "$PRG"`
	link=`expr "$ls" : '^.*-> \(.*\)$' 2>/dev/null`
	
	if [ expr "$link" : '^/' 2> /dev/null >/dev/null ]
	then
		PRG="$link"
	else
		PRG="`dirname "$PRG"`/$link"
	fi
done

PROGDIR=`dirname "$PRG"`

pushd $PROGDIR >/dev/null 2>&1

case "$1" in
	start)
		echo -n "Starting $NAME... "
		
		if [ -f $PIDFILE ]
		then
			echo -e "PID file found: $PIDFILE. Is another instance of Collaborilla running?\n"
			echo "Aborting."
			exit 1
		fi
		
		java -cp lib/jldap.jar:collaborilla.jar \
			se.kth.nada.kmr.collaborilla.service.CollaborillaService \
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
