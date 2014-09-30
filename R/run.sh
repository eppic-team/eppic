#!/bin/sh
dir=/afs/psi.ch/project/bioinfo2/kaks/jose/plp/crk-r192

for entfile in `ls $dir/????.?.entropies`
do
    base=`basename $entfile .entropies`
    kaksfile=$base.kaks
    out=$base.ps
    R --vanilla --args $entfile $dir/$kaksfile $out <  ~/afshome/workspace/crk/R/profile-plots.R
done