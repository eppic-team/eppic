#!/bin/sh

if [ -z "$3" ]
then
	echo "Usage: $0 <eppic jar file> <uniprot local db> <output blast dir>"
	exit 1
fi

jar=$1
db=$2
outdir=$3
outfile=$outdir/uniref100.fasta


cmd="java -cp $jar owl.core.connections.UniprotLocalConnection"

echo "Dumping..."
$cmd $db $outfile

# and then formatdb
echo "Formatdbing..."
cd $outdir
formatdb -p T -o T -l formatdb.log -i uniref100.fasta

echo "Removing fasta..."
rm uniref100.fasta

# finally we write a reldate.txt file
echo "Writing reldate.txt"
ver=`echo $db | sed "s/uniprot_\(.*\)/\1/"`
length=`expr length $ver`
if [ "$length" -lt "7" ]
then
	ver=`echo $ver | sed "s/_/./"`
fi
echo "UniProt Knowledgebase Release $ver consists of:" > reldate.txt

