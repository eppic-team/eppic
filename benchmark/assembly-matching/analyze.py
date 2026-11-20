#!/usr/bin/env python3
"""Analyses the biounit matching benchmark results.
Usage: analyze.py <results.tsv> [disagreements.tsv]
Prints markdown tables to stdout; optionally writes the list of biounits where the matchers disagree."""
import csv, sys
from collections import Counter, defaultdict

MATCH = {"EXACT", "EXTRA_EDGES", "SUBASSEMBLY", "PARENT"}


def load(path):
    rows = list(csv.DictReader(open(path), delimiter="\t"))
    entries = defaultdict(set)
    failed = []
    pairs = {}
    for r in rows:
        if r["type"].startswith("FAILED") or r["type"] in ("ERROR", "NO_BIOUNITS"):
            failed.append(r)
            continue
        key = (r["pdbId"], r["biounit"])
        pairs.setdefault(key, {})[r["matcher"]] = r
        entries[r["stratum"]].add(r["pdbId"])
    pairs = {k: v for k, v in pairs.items() if "OPERATORS" in v and "GRAPH" in v}
    return rows, pairs, failed


def table(header, rows):
    out = ["| " + " | ".join(header) + " |", "|" + "---|" * len(header)]
    out += ["| " + " | ".join(str(c) for c in r) + " |" for r in rows]
    return "\n".join(out)


def outcome(r):
    """Outcome of a matcher row: skipped, none, match-ok (consistent size), match-bad-size"""
    t = r["type"]
    if t == "SKIPPED":
        return "skipped"
    if t == "NONE":
        return "none"
    return "match" if size_ok(r) else "match_wrong_size"


def size_ok(r):
    """Strict size consistency: the biounit's entity counts equal those of one of the matched assembly's distinct
    sub-assemblies (recomputed here from the stoichiometry columns)"""
    return r["buStoich"] in r["assemblyStoich"].split("|")


def main():
    rows, pairs, failed = load(sys.argv[1])
    strata = sorted({r["stratum"] for r in rows})

    print("## Runs\n")
    c = Counter(r["stratum"] for r in rows if r["matcher"] == "OPERATORS")
    fails = Counter(r["stratum"] for r in failed)
    ents = defaultdict(set)
    for (p, b), v in pairs.items():
        ents[v["GRAPH"]["stratum"]].add(p)
    print(table(["stratum", "entries ok", "biounits", "entries failed/no biounits"],
                [[s, len(ents[s]), c[s], fails[s]] for s in strata]))
    print()
    if failed:
        print("Failures: " + ", ".join(f"{r['pdbId']} ({r['type']}{' ' + r.get('reason', '')[:90] if r.get('reason') else ''})" for r in failed))
        print()

    for matcher in ("OPERATORS", "GRAPH"):
        print(f"## Match types: {matcher}\n")
        types = ["EXACT", "EXTRA_EDGES", "SUBASSEMBLY", "PARENT", "NONE", "SKIPPED"]
        trs = []
        for s in strata + ["ALL"]:
            cnt = Counter(v[matcher]["type"] for v in pairs.values() if s == "ALL" or v[matcher]["stratum"] == s)
            trs.append([s] + [cnt[t] for t in types])
        print(table(["stratum"] + types, trs))
        print()

    print("## Outcomes (match with consistent size / match with wrong size / none / skipped)\n")
    outs = ["match", "match_wrong_size", "none", "skipped"]
    trs = []
    for s in strata + ["ALL"]:
        sel = [v for v in pairs.values() if s == "ALL" or v["GRAPH"]["stratum"] == s]
        o = Counter(outcome(v["OPERATORS"]) for v in sel)
        g = Counter(outcome(v["GRAPH"]) for v in sel)
        trs.append([s, len(sel)] + [f"{o[x]} / {g[x]}" for x in outs])
    print(table(["stratum", "biounits"] + [x + " (ops / graph)" for x in outs], trs))
    print()

    print("## Agreement matrix (rows: OPERATORS outcome, columns: GRAPH outcome)\n")
    m = Counter((outcome(v["OPERATORS"]), outcome(v["GRAPH"])) for v in pairs.values())
    print(table(["ops \\ graph"] + outs, [[a] + [m[(a, b)] for b in outs] for a in outs]))
    print()
    same = sum(1 for v in pairs.values() if v["OPERATORS"]["type"] in MATCH and v["GRAPH"]["type"] in MATCH
               and v["OPERATORS"]["assemblyId"] == v["GRAPH"]["assemblyId"])
    both = sum(1 for v in pairs.values() if v["OPERATORS"]["type"] in MATCH and v["GRAPH"]["type"] in MATCH)
    print(f"Both matched: {both} biounits; same assembly in {same}.\n")

    print("## Symmetry cross-check (matched biounits with a BioJava symmetry)\n")
    trs = []
    for matcher in ("OPERATORS", "GRAPH"):
        sel = [v[matcher] for v in pairs.values() if v[matcher]["type"] in MATCH and v[matcher]["buSymmetry"] not in ("", "NA")]
        ok = sum(1 for r in sel if r["symmetryOk"] == "true")
        trs.append([matcher, len(sel), ok, len(sel) - ok])
    print(table(["matcher", "matched", "symmetry agrees", "symmetry differs"], trs))
    print()

    print("## Cost of the graph matcher (ms per biounit, by biounit size)\n")
    bins = [(1, 4), (5, 12), (13, 24), (25, 60), (61, 200), (201, 100000)]
    trs = []
    for lo, hi in bins:
        for matcher in ("OPERATORS", "GRAPH"):
            ts = sorted(int(v[matcher]["timeMs"]) for v in pairs.values()
                        if v[matcher]["timeMs"] and lo <= int(v[matcher]["buChains"] or 0) <= hi)
            if not ts:
                continue
            trs.append([f"{lo}-{hi}", matcher, len(ts), ts[len(ts) // 2], ts[int(len(ts) * 0.9)], ts[-1]])
    print(table(["chains", "matcher", "n", "median", "p90", "max"], trs))
    print()

    dis = []
    for (p, b), v in sorted(pairs.items()):
        o, g = v["OPERATORS"], v["GRAPH"]
        if outcome(o) != outcome(g) or (o["type"] in MATCH and g["type"] in MATCH and o["assemblyId"] != g["assemblyId"]):
            dis.append((p, b, o, g))
    print(f"## Disagreements: {len(dis)}\n")
    trs = [[p, b, g["stratum"], g["buChains"], g["buStoich"], g["buSymmetry"],
            f"{o['type']} {o['assemblyClusters']} {o['assemblySize']} {o['assemblySymmetry']}",
            f"{g['type']} {g['assemblyClusters']} {g['assemblySize']} {g['assemblySymmetry']}",
            o["buClusters"], g["buClusters"], f"{g['labelledEdges']}/{g['unlabelledEdges']}/{g['removedEdges']}"]
           for p, b, o, g in dis]
    print(table(["pdb", "bu", "stratum", "chains", "stoich", "bu sym", "operators", "graph",
                 "ops clusters", "graph clusters", "graph edges lab/unlab/removed"], trs))
    print()

    if len(sys.argv) > 2:
        with open(sys.argv[2], "w") as f:
            w = csv.writer(f, delimiter="\t")
            w.writerow(["pdbId", "biounit", "stratum", "buChains", "buStoich", "buSymmetry",
                        "opsType", "opsAssembly", "opsSize", "opsSizeOk", "opsClusters",
                        "graphType", "graphAssembly", "graphSize", "graphSizeOk", "graphClusters",
                        "labelled", "unlabelled", "minor", "removed", "numValidAssemblies", "ncs", "xtal"])
            for p, b, o, g in dis:
                w.writerow([p, b, g["stratum"], g["buChains"], g["buStoich"], g["buSymmetry"],
                            o["type"], o["assemblyClusters"], o["assemblySize"], size_ok(o), o["buClusters"],
                            g["type"], g["assemblyClusters"], g["assemblySize"], size_ok(g), g["buClusters"],
                            g["labelledEdges"], g["unlabelledEdges"], g["minorContacts"], g["removedEdges"],
                            g["numValidAssemblies"], g["ncs"], g["xtal"]])


if __name__ == "__main__":
    main()
