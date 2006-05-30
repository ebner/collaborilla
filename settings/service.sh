# CollaborillaService start script
# Hannes Ebner <hebner@kth.se>, 2006

#!/bin/sh

NAME=CollaborillaService
PIDFILE=$0.pid

case "$1" in
  start)
	echo -n "Starting $NAME... "
	java -cp lib/jldap.jar:collaborilla.jar se.kth.nada.kmr.collaborilla.service.CollaborillaService >>collaborilla.log 2>&1 &
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
