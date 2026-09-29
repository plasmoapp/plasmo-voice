import re
from dataclasses import dataclass, field


@dataclass
class Bullet:
    text: str
    children: list["Bullet"] = field(default_factory=list)


def read(path):
    with open(path) as f:
        return f.read()


def body_after_rule(text):
    return text.split("\n---\n", 1)[1] if "\n---\n" in text else text


def bullets(text):
    roots, stack = [], []
    for line in text.splitlines():
        stripped = line.lstrip(" ")
        indent = len(line) - len(stripped)
        if stripped.startswith("- "):
            while stack and stack[-1][0] >= indent:
                stack.pop()
            bullet = Bullet(stripped)
            (stack[-1][1].children if stack else roots).append(bullet)
            stack.append((indent, bullet))
        elif stripped and stack:
            stack[-1][1].text += "\n" + line
    return roots


def render(items, depth=0):
    lines = []
    for bullet in items:
        lines.append("  " * depth + bullet.text)
        if bullet.children:
            lines.append(render(bullet.children, depth + 1))
    return "\n".join(lines)


def split_shared(mod, paper):
    paper_left = {b.text: b for b in paper}
    shared, mod_only = [], []

    for bullet in mod:
        match = paper_left.get(bullet.text)
        if match is None:
            mod_only.append(bullet)
            continue

        common, mod_extra, paper_extra = split_shared(bullet.children, match.children)
        if not common and bullet.children and match.children:
            mod_only.append(bullet)
            continue

        shared.append(Bullet(bullet.text, common))
        if mod_extra:
            mod_only.append(Bullet(bullet.text, mod_extra))
        paper_left[bullet.text] = Bullet(bullet.text, paper_extra) if paper_extra else None

    paper_only = [paper_left[b.text] for b in paper if paper_left[b.text] is not None]
    return shared, mod_only, paper_only


def split_headers(text):
    preamble, *parts = re.split(r"^### (Client|Server)$", text, flags=re.M)
    return preamble.strip(), dict(zip(parts[::2], parts[1::2]))


preamble, client = split_headers(read("client/changelog.md"))
client_body = client.get("Client", "")

shared, mod_only, paper_only = split_shared(
    bullets(client.get("Server", "")),
    bullets(body_after_rule(read("server/changelog.md"))),
)

sections = [
    ("Client (Fabric/NeoForge)", client_body.strip()),
    ("Server (all platforms)", render(shared)),
    ("Server (Fabric/NeoForge)", render(mod_only)),
    ("Server (Paper/Folia)", render(paper_only)),
    ("Proxy (Velocity/BungeeCord)", body_after_rule(read("proxy/changelog.md")).strip()),
]

blocks = [preamble] if preamble else []
blocks += [f"### {title}\n{body}" for title, body in sections if body]
print("\n\n".join(blocks))
