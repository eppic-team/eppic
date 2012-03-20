#!/bin/sh

dir=`dirname $0`
uniprotjar=uniprotjapi.jar
eppicjar=eppic.jar


if [ ! -f "$dir/$uniprotjar" ]
then
	echo "$uniprotjar is missing in directory $dir. Can't run eppic without it."
	exit 1
fi

java -cp $dir/$uniprotjar:$dir/$eppicjar crk.CRKMain $*
