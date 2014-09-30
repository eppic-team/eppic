#!/bin/sh

if [ -z "$2" ]
then
	echo "Usage $0 <root dir where downloaded data goes> <uniprot versions file>"
	echo "  e.g. $o /home/duarte_j/uniprot-archive /home/duarte_j/uniprot-archive/uniprot.past.versions"
	exit 1
fi

rootdir=$1
pastVersionsFile=$2

url=ftp://ftp.uniprot.org/pub/databases/uniprot/previous_releases


for release in `cat $rootdir/uniprot.past.versions | grep -v "^#"`
do
	cd $rootdir
	mkdir $release
	cd $release
	wget $url/release$release/uniref/uniref$release.tar.gz
	# for new style versions there's a hyphen! we try both, couldn't bother to find how to do it properly on bash
	wget $url/release$release/uniref/uniref-$release.tar.gz
done
