import json
import os
import re
import sys
from pathlib import Path

MC_ARTIFACT = "com.mojang:minecraft:"
SNAPSHOT_ID = re.compile(r"\d+\.\d+(\.\d+)?-(snapshot|pre|rc)-\d+")


def read_properties(path):
    props = {}
    for line in path.read_text().splitlines():
        line = line.strip()
        if line and not line.startswith("#") and "=" in line:
            key, value = line.split("=", 1)
            props[key.strip()] = value.strip()
    return props


root = Path(sys.argv[1])
sha = sys.argv[2][:7]

versions = json.loads((root / "client/versions.json").read_text())
parents = {v.get("parent") for v in versions}

snapshots = {}
for entry in versions:
    project = entry["project"]
    if project in parents:
        continue
    minecraft = read_properties(root / "client" / project / "gradle.properties").get("essential.defaults.loom.minecraft", "")
    if not minecraft.startswith(MC_ARTIFACT):
        continue
    mc = minecraft.removeprefix(MC_ARTIFACT)
    if SNAPSHOT_ID.fullmatch(mc):
        snapshots[project] = mc

if not snapshots:
    raise SystemExit("no leaf client project targets a minecraft snapshot")

mc_versions = set(snapshots.values())
if len(mc_versions) > 1:
    raise SystemExit(f"leaf client projects target different minecraft snapshots, expected exactly one: {snapshots}")

base_version = read_properties(root / "gradle.properties")["version"]
outputs = {
    "mc": mc_versions.pop(),
    "projects": json.dumps(sorted(snapshots), separators=(",", ":")),
    "version": f"{base_version}+{sha}",
}

for key, value in outputs.items():
    print(f"{key}={value}")

if "GITHUB_OUTPUT" in os.environ:
    with open(os.environ["GITHUB_OUTPUT"], "a") as output:
        for key, value in outputs.items():
            output.write(f"{key}={value}\n")
