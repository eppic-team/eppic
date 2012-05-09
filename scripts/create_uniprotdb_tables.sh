#!/bin/sh

if [ -z "$1" ]
then
	echo "Usage: $0 <database>"
	exit 1
fi

db=$1
table=uniprot

# note 1: uniprot ids are 6 char, but for isoforms they can be 8 or 9 chars (hyphen and number additionally)
# note 2: primary keys are created later in index script so that load data is a lot quicker
# note 3: in first versions of uniprot there are duplicate uniprot ids, e.g. Q9WHW0 in version 1.0
#      Because of that we need to create the uniprot table with a primary key in uniparc id so that 
#      on loading the duplicates will be eliminated. Also a primary key on uniprot_clusters (member)
#      The other indexes are created with the separate script



mysql $db <<EOF
DROP TABLE IF EXISTS $table;
DROP TABLE IF EXISTS ${table}_clusters;
CREATE TABLE $table (
 id varchar(23),
 uniprot_id varchar(9),
 uniparc_id char(13) PRIMARY KEY,
 tax_id int,
 sequence text
);
CREATE TABLE ${table}_clusters (
 representative varchar(9),
 member varchar(9) PRIMARY KEY,
 tax_id int
);

EOF



exit 0

# code to create the taxonomy tables

mysql uniprot_taxonomy <<EOF
DROP TABLE IF EXISTS taxonomy;

CREATE TABLE taxonomy (
 tax_id int,
 mnemonic varchar(20),
 scientific varchar(255),
 common varchar(255),
 synonym varchar(255),
 other text,
 reviewed varchar(20),
 rank varchar(20),
 lineage text,
 parent int
 
);
EOF


