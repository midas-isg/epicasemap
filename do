#!/bin/bash

NAME=epidemap
echo $@
echo using following env variables: PORT=$PORT NAME=$NAME 

##############################################################################

if [ "$1" = "start" ]; then
	if [ "$PORT" = "" ]; then
		PORT=9000
	fi

	./activator start -Dhttp.port=$PORT
fi
##############################################################################

if [ "$1" = "shutdown" ]; then
	FILE=target/universal/stage/RUNNING_PID
	kill -9 `cat $FILE`
	rm $FILE
	echo kill PID from and remove file $FILE
fi
##############################################################################

if [ "$1" = "update" ]; then
	git fetch --tags
	TAG=$NAME-$2
	git checkout tags/$TAG
	echo checkout form tags/$TAG
fi
##############################################################################
