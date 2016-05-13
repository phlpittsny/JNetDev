#!/bin/sh
# Run a GS script
# Usage: run.sh N where N is 1 2 3 or 4
#
N=${1:-1}
shift

case "$N" in
	1)	CLASS=GS1
		;;
	2)	CLASS=GS2
		;;
	3)	CLASS=Ping
		;;
	4)	CLASS=Ping2
		;;
	*)	echo "Usage: run.sh N where N is one of 1, 2, 3, or 4"
		exit 1
		;;
esac

cd GS${N}_d
java -classpath 'C:\Windows\System32\jNetDev.jar;.' $CLASS "$@"

