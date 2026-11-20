#!/bin/bash
# Runs the biounit matching benchmark: one JVM per entry (with a time limit), N in parallel.
# Usage: run_benchmark.sh <entries.tsv> <results dir> <work dir> [parallel jobs] [timeout s] [Xmx]
# Needs the eppic-cli uber jar built (mvn -pl eppic-cli -am install -DskipTests).
# PDB_DIR may be set to an mmCIF cache directory.
set -u
ENTRIES=$1; RESULTS=$2; WORK=$3; JOBS=${4:-4}; TIMEOUT=${5:-1800}; XMX=${6:-5g}
JAR=$(cd "$(dirname "$0")/../.." && pwd)/eppic-cli/target/uber-eppic-cli-3.5.3-SNAPSHOT.jar
mkdir -p "$RESULTS" "$WORK"
export JAR RESULTS WORK TIMEOUT XMX

run_one() {
  local pdb=$1 stratum=$2
  local out="$RESULTS/$pdb.tsv"
  [ -s "$out" ] && return 0
  local in="$WORK/$pdb.in"
  printf "%s\t%s\n" "$pdb" "$stratum" > "$in"
  local start=$(date +%s)
  perl -e 'alarm shift; exec @ARGV' "$TIMEOUT" java -Xmx"$XMX" -cp "$JAR" eppic.tools.CompareBiounitMatchers "$in" "$out.part" "$WORK" > "$RESULTS/$pdb.log" 2>&1
  local rc=$?
  local secs=$(( $(date +%s) - start ))
  if [ $rc -ne 0 ] || [ ! -s "$out.part" ]; then
    printf "pdbId\tstratum\tbiounit\tbuChains\tbuStoich\tbuSymmetry\tmatcher\ttype\n%s\t%s\t\t\t\t\t\tFAILED_rc%s_%ss\n" "$pdb" "$stratum" "$rc" "$secs" > "$out.part"
  fi
  echo "$secs" > "$RESULTS/$pdb.secs"
  mv "$out.part" "$out"
  rm -rf "$in" "$WORK/$pdb"
  echo "$pdb $stratum rc=$rc ${secs}s"
}
export -f run_one

tr '\t' ' ' < "$ENTRIES" | xargs -P "$JOBS" -L 1 bash -c 'run_one "$0" "$1"'
