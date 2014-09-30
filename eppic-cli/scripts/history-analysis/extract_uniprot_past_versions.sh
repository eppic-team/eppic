#!/bin/sh


rootdir=/home/duarte_j/uniprot-archive

for dir in `find $rootdir -mindepth 1 -maxdepth 1 -type d`
do
	echo $dir
	cd $dir
	ver=`basename $dir`
	tar -zxf uniref$ver.tar.gz
	rm uniref50.tar* uniref90.tar*
	#ext=`basename uniref100* .gz`
	ext=`ls uniref100* | awk -F. '{print $NF}'`
	if [ "$ext" = "gz" ]
	then
		tar -zxf uniref100.tar.gz
		rm unref100.tar.gz
	else
		tar -xf uniref100.tar
		rm uniref100.tar
	fi
	rm README 
	
done


