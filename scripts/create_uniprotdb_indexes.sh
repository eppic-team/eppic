#!/bin/sh

if [ -z "$1" ]
then
	echo "Usage: $0 <database>"
	exit 1
fi

db=$1

# NOTE in first versions of uniprot there are duplicate uniprot ids, e.g. Q9WHW0 in version 1.0
#      because of that we need to create the table with a primary key in uniparc id so that on loading the duplicates will be eliminated
#      thus we have to remove from here the primary key on id and the index on uniparc_id
mysql $db <<EOF
-- ALTER TABLE uniprot ADD PRIMARY KEY (id);
-- ALTER TABLE uniprot_clusters ADD PRIMARY KEY (member);
CREATE INDEX UNIPROTID_IDX ON uniprot (uniprot_id);
-- CREATE INDEX UNIPARCID_IDX ON uniprot (uniparc_id);
EOF

exit 0


# code to create the taxonomy table index
mysql uniprot_taxonomy <<EOF
ALTER TABLE taxonomy ADD PRIMARY KEY (tax_id);
EOF



