#!/bin/sh

if [ -z "$1" ]
then
	echo "Usage: $0 <database> <uniprot tab data file> <uniprot clusters tab data file>"
	exit 1
fi

db=$1
file=$2
clustersfile=$3
table=uniprot
mysql $db <<EOF
LOAD DATA LOCAL INFILE '$file' INTO TABLE $table;
LOAD DATA LOCAL INFILE '$clustersfile' INTO TABLE ${table}_clusters;
EOF

exit 0

mysql uniprot_taxonomy <<EOF
LOAD DATA LOCAL INFILE '/nfs/data/dbs/uniprot/taxonomy-all.tab' INTO TABLE taxonomy IGNORE 1 LINES;
EOF

