#!/bin/sh

#$ -N JOBNAME
#$ -q all.q
#$ -e LOGDIR
#$ -o LOGDIR
#$ -t 1-MAXTASK
#$ -l ram=MAXRAM
#$ -l s_rt=TIMEMIN,h_rt=TIMEMAX

pdb=`grep -v "^#"  INPUTLIST | sed "s/\(....\).*/\1/" | sed "${SGE_TASK_ID}q;d"`

# Cut the middle letters of pdb code for making directory in divided
mid_pdb=`echo $pdb | awk -F "" '{print $2$3}'`

# Check is directory is not present
if [ ! -d OUTFOLDER/data/divided/$mid_pdb ]; then mkdir -p OUTFOLDER/data/divided/$mid_pdb; fi
if [ ! -d OUTFOLDER/data/divided/$mid_pdb/$pdb ]; then mkdir -p OUTFOLDER/data/divided/$mid_pdb/$pdb; fi
cd OUTFOLDER/data/all/
ln -s ../divided/$mid_pdb/$pdb $pdb

EPPIC -i $pdb -a 1 -s -o OUTFOLDER/data/divided/$mid_pdb/$pdb -l -g CONF
cp OUTFOLDER/logs/JOBNAME.e${JOB_ID}.${SGE_TASK_ID} OUTFOLDER/data/divided/$mid_pdb/$pdb/$pdb.e
cp OUTFOLDER/logs/JOBNAME.o${JOB_ID}.${SGE_TASK_ID} OUTFOLDER/data/divided/$mid_pdb/$pdb/$pdb.o

