import json

versions = json.load(open("client/versions.json"))
parent = {v["project"]: v.get("parent") for v in versions}
parents = {p for p in parent.values() if p}

def chain(project):
    out, cur = [], project
    while cur:
        out.append(cur)
        cur = parent.get(cur)
    return out

chains = {leaf: chain(leaf) for leaf in parent if leaf not in parents}

assigned, matrix = set(), []
for leaf in sorted(chains, key=lambda l: (-len(chains[l]), l)):
    owned = [v for v in chains[leaf] if v not in assigned]
    assigned.update(owned)
    matrix.append({"leaf": leaf, "tasks": " ".join(f":client:{v}:build" for v in owned)})

unbuilt = set(parent) - assigned
if unbuilt:
    raise SystemExit(f"client versions not covered by any chain: {sorted(unbuilt)}")

print(json.dumps(matrix, separators=(",", ":")))
