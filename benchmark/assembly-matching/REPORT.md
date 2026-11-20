# PDB biounit matching: operators vs. graph matcher

Benchmark of the new graph-based matcher (`BiounitGraphMatcher`, `--biounit-matcher GRAPH`) against the original
operator-based matcher (`OPERATORS`, still the default) for mapping PDB-annotated biological assemblies to EPPIC's
valid assemblies.

## Summary

- On 613 biounits from 324 entries, the two matchers **never pick different assemblies when both give a
  size-consistent match**: 441 biounits agree.
- The old matcher reports **85 matches whose size or stoichiometry disagrees with the PDB biounit**. The new one
  reports 36, and 34 of those are explicitly typed `SUBASSEMBLY`: the PDB annotation is a part of the EPPIC assembly,
  e.g. a virus pentamer or a dimer that is half of an EPPIC tetramer. The old matcher reports these as plain matches.
- The new matcher **finds 10 size-consistent matches the old one missed** (`NONE`). All are NCS virus capsids (4iv3,
  4n53, 4wm8), including the full 240-mers. It also corrects 22 old wrong-size matches into right-size ones, e.g.
  icosahedral 60-mers that the old matcher mapped to a monomer, or 1-chain biounits mapped to a 60-mer.
- The old **parent fallback (issue #47) is the main source of false positives**: 39 of its 47 matches have the wrong
  size (heterodimers mapped to monomers, a D4 octamer to a dimer, A2B trimers to dimers). For 26 of those biounits
  the new matcher says `NONE`, because EPPIC has no valid assembly for them; most of the rest become size-consistent
  matches or `SUBASSEMBLY`.
- Only one biounit goes from a size-consistent old match to new `NONE` (5wor: EPPIC's closed C20 ring vs. the PDB's
  open helical 20-mer).
- Cost: the new matcher is about 10× slower for small biounits (median 11 ms vs 1 ms) and similar for the largest
  ones (≤17 s for 960 chains). In this benchmark it is small next to the EPPIC run itself (median 3 s per entry with
  100 ASA sphere points; production uses more).

**Recommendation:** switch the default to `GRAPH`. Before that, decide how `SUBASSEMBLY` results should be stored
(see Open questions).

## Method

### Matchers

- **OPERATORS** (original, `DataModelAdaptor.matchBioUnitByOperators`): it enumerates every pair of biounit operators
  and matches them to EPPIC interfaces by exact equality of the operator matrices (1e-5). The set of engaged clusters
  generates an assembly. It then looks for an assembly equal to that one (`EXACT`), or else a "parent" whose
  first-entity count agrees (`PARENT`).
- **GRAPH** (new, `eppic.analysis.compare.BiounitGraphMatcher`):
  1. Build the biounit with BioJava `BiologicalAssemblyBuilder.rebuildQuaternaryStructure` and find its real
     chain–chain contacts with `InterfaceFinder` (5.5 Å, the same cutoff as EPPIC).
  2. Label each contact with an EPPIC interface cluster using `StructureInterface.getContactOverlapScore`, with the
     cutoff EPPIC uses for clustering (0.3). The labelling is done once per distinct residue contact set. Unlabelled
     contacts of at least 35 Å² (EPPIC's minimum area) become unlabelled edges; smaller ones are ignored, as EPPIC
     ignores them.
  3. For each valid assembly, drop the biounit edges whose cluster is not engaged in it. This is allowed only if it
     doesn't change the biounit's connected components. Then require every component to be VF2-isomorphic, with
     entity and cluster labels, to one of the assembly's sub-assemblies (`EXACT`, or `EXTRA_EDGES` if edges were
     dropped), or to an induced subgraph of one (`SUBASSEMBLY`).
  4. Pick the best candidate by: fewest dropped edges, full before partial, fewest engaged clusters unused by the
     biounit.

### Harness

`eppic.tools.CompareBiounitMatchers` runs EPPIC once per entry, with no evolutionary scoring and 100 ASA sphere
points, then runs both matchers on the same in-memory result, so only the matching step differs.
`run_benchmark.sh` runs one JVM per entry, 5 in parallel, with a 30 min limit (no entry hit it).

### Dataset

`entries.tsv` (331 entries) is built by `make_entries.py` from the RCSB search API, sampled with seed 42:

| stratum | definition | entries |
|---|---|---|
| homomer | X-ray ≤3 Å, 1 protein entity, ≤12 chains in the asymmetric unit (AU) | 80 |
| heteromer | X-ray ≤3 Å, 2–4 protein entities, ≤12 chains in the AU | 60 |
| large_biounit | X-ray, an assembly with ≥60 chains, ≤30 chains in the AU (mostly NCS virus capsids) | 30 |
| helical | X-ray, assembly symmetry H | 25 |
| asymmetric_homomer | X-ray, homomeric assembly of ≥2 chains with global symmetry C1 | 30 |
| pseudosymmetric | X-ray, assembly with pseudo symmetry | 25 |
| many_chains_au | X-ray, 20–40 chains in the AU | 25 |
| em | EM, 2–24 chains | 30 |
| nmr | solution NMR, ≥2 chains | 20 |
| regression | 3gzh, 3p3f, 3cfh, 2a7n, 1ae9, 11bg | 6 |

Seven large_biounit entries failed inside EPPIC before matching: 1aq4, 1x9p, 2bq5 and 2w0c with "too many clashes",
and 1hrv, 1qju and 2rm2 with an NPE in `TextOutputWriter.getChainCluster`. That NPE is an existing EPPIC bug and
unrelated to this work. That leaves 324 entries and 613 biounits.

### Automatic checks

- **Size consistency:** a match is size-consistent if the biounit's entity counts equal those of one of the matched
  assembly's distinct sub-assemblies.
- **Symmetry:** the matched assembly's EPPIC symmetry is compared with BioJava's quaternary symmetry of the biounit.

These checks are programmatic only: **no case was reviewed visually**.

## Results

### Outcomes per stratum (OPERATORS / GRAPH)

| stratum | biounits | size-consistent match | wrong-size match | none | skipped |
|---|---|---|---|---|---|
| asymmetric_homomer | 52 | 16 / 16 | 13 / 11 | 23 / 25 | 0 / 0 |
| em | 30 | 24 / 24 | 4 / 0 | 2 / 6 | 0 / 0 |
| helical | 38 | 24 / 23 | 1 / 0 | 13 / 15 | 0 / 0 |
| heteromer | 109 | 94 / 94 | 11 / 1 | 4 / 14 | 0 / 0 |
| homomer | 126 | 121 / 121 | 1 / 0 | 4 / 5 | 0 / 0 |
| large_biounit | 107 | 24 / 56 | 52 / 22 | 30 / 29 | 1 / 0 |
| many_chains_au | 88 | 81 / 81 | 2 / 2 | 5 / 5 | 0 / 0 |
| nmr | 20 | 19 / 19 | 0 / 0 | 1 / 1 | 0 / 0 |
| pseudosymmetric | 33 | 30 / 30 | 1 / 0 | 2 / 3 | 0 / 0 |
| regression | 10 | 9 / 9 | 0 / 0 | 1 / 1 | 0 / 0 |
| **all** | **613** | **442 / 473** | **85 / 36** | **85 / 104** | **1 / 0** |

Match types, all strata:

| matcher | EXACT | EXTRA_EDGES | SUBASSEMBLY | PARENT | NONE | SKIPPED |
|---|---|---|---|---|---|---|
| OPERATORS | 480 | – | – | 47 | 85 | 1 |
| GRAPH | 462 | 13 | 34 | – | 104 | 0 |

### Agreement matrix (rows OPERATORS, columns GRAPH)

| | match | wrong-size match | none | skipped |
|---|---|---|---|---|
| **match** | 441 | 0 | 1 | 0 |
| **wrong-size match** | 22 | 30 | 33 | 0 |
| **none** | 10 | 6 | 69 | 0 |
| **skipped** | 0 | 0 | 1 | 0 |

Both matched in 493 biounits; they picked the same assembly in 466 of them. All 27 that differ involve a wrong-size
old match.

### Symmetry cross-check (matched biounits that have a BioJava symmetry)

| matcher | matched | agrees | differs |
|---|---|---|---|
| OPERATORS | 520 | 387 (74%) | 133 |
| GRAPH | 502 | 416 (83%) | 86 |

Most of the remaining differences are expected: `SUBASSEMBLY` matches, where the PDB biounit is smaller than the
assembly, and biounits made of disconnected copies.

### The 78 disagreements, by category

The full list is in `disagreements.tsv`.

| OPERATORS → GRAPH | biounits | entries | assessment |
|---|---|---|---|
| PARENT, wrong size → NONE | 26 | 2iaa 3v4k 4fts 4j1v 4nwv 5gog 5mfd 5txj 6chg 6eyd 6q41 6w6v 7fi5 8dg8 8k9i 8uh7 | graph right: EPPIC has no valid assembly of the biounit's composition (e.g. 5mfd: valid assemblies are only {}, {1} and {3}, while the PDB AB dimer uses cluster 2) |
| EXACT, wrong size → EXACT | 14 | 1p5y 1vak 3ra4 5cw0 5yl1 | graph right: NCS capsids, e.g. 1-chain and pentamer biounits that the old matcher mapped to the 60-mer, and 5cw0's 60-mer mapped to a monomer |
| EXACT, wrong size → NONE | 7 | 1ijs 5mqc | graph right: icosahedral 180-mers whose capsid contacts are absent from EPPIC's crystal (hundreds of unlabelled edges); the old "match" was a 3-mer |
| NONE → EXACT | 7 | 4iv3 4n53 4wm8 | graph right: NCS capsids and their pieces that the old matcher missed (operator equality fails) |
| NONE → SUBASSEMBLY | 7 | 4iv3 4n53 4wm8 | graph right: PDB pieces (protomer, D2 80-mer) of the capsid |
| EXACT, wrong size → SUBASSEMBLY | 4 | 1a6c 5cw0 | graph right: capsid pieces that the old matcher mapped to a monomer |
| PARENT, wrong size → EXACT | 4 | 1x35 4nwv | graph right: trimers that the old matcher mapped to the 180-mer |
| EXACT, wrong size → EXTRA_EDGES | 2 | 1a6c | graph right: 60-mer, old mapped it to a monomer |
| PARENT, wrong size → EXTRA_EDGES | 2 | 1nov | graph right: hexamer, old mapped it to a 360-mer |
| NONE → EXTRA_EDGES | 2 | 4n53 4wm8 | graph found the 240-mer, after dropping 300–420 edges of non-engaged clusters |
| SKIPPED → NONE | 1 | 1nov | old exceeds its 10k-pair cap on the 720-mer; graph completes in 5 s |
| PARENT, wrong size → EXACT, wrong size | 1 | 4nwv | the biounit is 5 non-touching copies of an EPPIC trimer; graph says so |
| PARENT → NONE | 1 | 5wor | definitional: EPPIC's {1} is a closed C20 ring, the PDB biounit an open helical 20-mer with lateral contacts; the graphs are not isomorphic |

There are also 30 biounits where both matchers give a wrong-size match (same outcome class, so they are not in the
list above). In 27 of them the old matcher says `EXACT` or `PARENT` and the new one `SUBASSEMBLY`, e.g. asymmetric
homomers such as 3o2o (PDB dimer, EPPIC octamer) and 2v90 (trimer vs hexamer), and virus pieces. In the other 3 (2a3w
×2, 4nwv) the biounit is several copies of an EPPIC assembly with no contacts between them, and both agree.

### Cost

Matching time per biounit (ms), excluding the EPPIC run:

| biounit chains | n | OPERATORS median / p90 / max | GRAPH median / p90 / max |
|---|---|---|---|
| 1–4 | 454 | 1 / 3 / 83 | 11 / 40 / 156 |
| 5–12 | 69 | 2 / 20 / 116 | 40 / 103 / 168 |
| 13–24 | 38 | 10 / 81 / 504 | 90 / 288 / 1581 |
| 25–60 | 31 | 66 / 221 / 506 | 216 / 533 / 1432 |
| 61–200 | 11 | 283 / 855 / 862 | 1295 / 1877 / 2180 |
| >200 | 10 | 1583 / 16891 / 16891 | 2269 / 17145 / 17145 |

Before contact labelling was cached by residue contact set, the largest case (1uf2, 960 chains) took 544 s; it now
takes 18 s. Peak memory was not measured; all runs fit in a 5 GB heap. Biounits with more than 1000 polymer chains
are skipped (`BiounitGraphMatcher.DEFAULT_MAX_CHAINS`).

## Limitations

- No visual review. The assessments above come from sizes, stoichiometries, cluster ids and edge counts.
- The size check is a heuristic. It counts biounits made of disconnected copies of an assembly as wrong size, and
  `SUBASSEMBLY` results are wrong size by definition.
- It's one sample of 331 entries. The common cases (homomers, heteromers, EM, NMR) are almost all agreements, so the
  differences are concentrated in large/NCS, heteromer and asymmetric entries.
- EPPIC ran with 100 ASA sphere points. Interface areas near the 35 Å² cutoff can differ from production runs, but
  this affects both matchers in the same way.
- `EXTRA_EDGES` with many dropped edges (4n53, 4wm8, 1nov: 300–480) relies on the connectivity rule. It would be
  worth checking visually whether those non-engaged clusters really are redundant.
- The 7 entries that failed in EPPIC aren't covered.

## Open questions before switching the default

1. **What to store for `SUBASSEMBLY`.** `DataModelAdaptor` currently records a `SUBASSEMBLY` result like any match:
   the larger EPPIC assembly gets the BIO call for `pdbN`, which is what the old matcher did for those cases, without
   saying so. An alternative is to treat it like `NONE` (a new topologically invalid assembly), or to store the match
   type in the call reason.
2. **`EXTRA_EDGES` with many dropped edges.** Should there be a cap, e.g. a fraction of edges, beyond which it
   becomes `NONE`?

## Files

- `make_entries.py`: builds `entries.tsv` from the RCSB search API.
- `run_benchmark.sh`: runs the harness per entry, in parallel.
- `analyze.py`: produces the tables above and `disagreements.tsv` from `results.tsv`.
- `results.tsv`: raw results, one row per biounit per matcher.
- `disagreements.tsv`: biounits where the matchers' outcomes differ.

To reproduce:

```
mvn -pl eppic-cli -am install -DskipTests
python3 benchmark/assembly-matching/make_entries.py entries.tsv
PDB_DIR=<mmCIF cache> benchmark/assembly-matching/run_benchmark.sh entries.tsv <results dir> <work dir> 5
(head -1 <results dir>/3gzh.tsv; for f in <results dir>/*.tsv; do tail -n +2 $f; done) > results.tsv
python3 benchmark/assembly-matching/analyze.py results.tsv disagreements.tsv
```
