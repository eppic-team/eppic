#!/usr/bin/env python3
"""Builds the stratified entry list for the biounit matching benchmark from the RCSB search API.
Usage: make_entries.py <out file>
Writes: pdbId<TAB>stratum per line. Sampling is reproducible (fixed seed)."""
import json, random, sys, urllib.request

URL = "https://search.rcsb.org/rcsbsearch/v2/query"

def attr(a, op, v, neg=False):
    p = {"attribute": a, "operator": op, "value": v}
    if neg: p["negation"] = True
    return {"type": "terminal", "service": "text", "parameters": p}

def query(nodes, return_type="entry"):
    q = {"query": {"type": "group", "logical_operator": "and", "nodes": nodes},
         "return_type": return_type, "request_options": {"return_all_hits": True}}
    req = urllib.request.Request(URL, data=json.dumps(q).encode(), headers={"Content-Type": "application/json"})
    with urllib.request.urlopen(req, timeout=300) as r:
        res = json.load(r)
    return sorted({h["identifier"].split("-")[0].lower() for h in res.get("result_set", [])})

XRAY = attr("exptl.method", "exact_match", "X-RAY DIFFRACTION")
RES = attr("rcsb_entry_info.resolution_combined", "less_or_equal", 3.0)
# keep EPPIC runs tractable
SMALL_AU = attr("rcsb_entry_info.deposited_polymer_entity_instance_count", "less_or_equal", 12)
PROT = attr("rcsb_entry_info.polymer_entity_count_nucleic_acid", "equals", 0)

STRATA = [
    # name, nodes, return type, sample size
    ("homomer", [XRAY, RES, SMALL_AU, PROT, attr("rcsb_entry_info.polymer_entity_count_protein", "equals", 1)], "entry", 80),
    ("heteromer", [XRAY, RES, SMALL_AU, PROT, attr("rcsb_entry_info.polymer_entity_count_protein", "range",
        {"from": 2, "to": 4, "include_lower": True, "include_upper": True})], "entry", 60),
    ("large_biounit", [XRAY, attr("rcsb_assembly_info.polymer_entity_instance_count", "greater_or_equal", 60),
        attr("rcsb_entry_info.deposited_polymer_entity_instance_count", "less_or_equal", 30)], "assembly", 30),
    ("helical", [XRAY, attr("rcsb_struct_symmetry.symbol", "exact_match", "H"), SMALL_AU], "assembly", 25),
    ("asymmetric_homomer", [XRAY, RES, SMALL_AU, PROT, attr("rcsb_struct_symmetry.symbol", "exact_match", "C1"),
        attr("rcsb_struct_symmetry.kind", "exact_match", "Global Symmetry"),
        attr("rcsb_assembly_info.polymer_entity_count", "equals", 1),
        attr("rcsb_assembly_info.polymer_entity_instance_count", "greater_or_equal", 2)], "assembly", 30),
    ("pseudosymmetric", [XRAY, RES, SMALL_AU, PROT, attr("rcsb_struct_symmetry.kind", "exact_match", "Pseudo Symmetry")], "assembly", 25),
    ("many_chains_au", [XRAY, RES, attr("rcsb_entry_info.deposited_polymer_entity_instance_count", "range",
        {"from": 20, "to": 40, "include_lower": True, "include_upper": True})], "entry", 25),
    ("em", [attr("exptl.method", "exact_match", "ELECTRON MICROSCOPY"),
        attr("rcsb_entry_info.deposited_polymer_entity_instance_count", "range",
        {"from": 2, "to": 24, "include_lower": True, "include_upper": True})], "entry", 30),
    ("nmr", [attr("exptl.method", "exact_match", "SOLUTION NMR"),
        attr("rcsb_entry_info.deposited_polymer_entity_instance_count", "greater_or_equal", 2)], "entry", 20),
]

REGRESSION = ["3gzh", "3p3f", "3cfh", "2a7n", "1ae9", "11bg"]

def main():
    rnd = random.Random(42)
    seen = set(REGRESSION)
    out = [(p, "regression") for p in REGRESSION]
    for name, nodes, rt, n in STRATA:
        ids = [i for i in query(nodes, rt) if i not in seen]
        sample = rnd.sample(ids, min(n, len(ids)))
        print(f"{name}: {len(ids)} hits, sampled {len(sample)}", file=sys.stderr)
        seen.update(sample)
        out += [(p, name) for p in sample]
    with open(sys.argv[1], "w") as f:
        for p, s in out:
            f.write(f"{p}\t{s}\n")

if __name__ == "__main__":
    main()
