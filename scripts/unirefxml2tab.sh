#!/bin/sh

if [ -z "$4" ]
then
	echo "Usage: $0 <eppic jar file> <input uniref xml> <output uniprot tab file> <output clusters members tab file>"
	exit 1
fi

jar=$1
in=$2
outuni=$3
outclu=$4


cmd="java -cp $jar owl.core.connections.UnirefXMLParser"

$cmd $in $outuni $outclu
