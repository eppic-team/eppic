#!/bin/sh

taxonomytabfile=/nfs/data/dbs/uniprot/taxonomy-all.tab
db=uniprot_taxonomy
table=taxonomy


# creating tables
mysql $db <<EOF
DROP TABLE IF EXISTS $table;

CREATE TABLE $table (
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


# loading data
mysql $db <<EOF
LOAD DATA LOCAL INFILE '$taxonomytabfile' INTO TABLE $table IGNORE 1 LINES;
EOF


# indexing
mysql $db <<EOF
ALTER TABLE $table ADD PRIMARY KEY (tax_id);
EOF
