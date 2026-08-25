#!/usr/bin/env python3
"""Builds the static tutorial site (in _site/) from docs/*.md.

The docs/*.md chapters remain the single source of truth for content;
this script converts them into a styled, navigable HTML site meant to be
published on GitHub Pages. It is re-run on every deploy (see
.github/workflows/pages.yml), so editing a chapter's markdown is enough —
no need to hand-edit generated HTML.

Usage:
    pip install markdown
    python scripts/build_site.py
"""
import re
import shutil
from pathlib import Path

import markdown as md

ROOT = Path(__file__).resolve().parent.parent
DOCS = ROOT / "docs"
ASSETS = ROOT / "assets"
OUT = ROOT / "_site"

REPO_URL = "https://github.com/trishala23/java-unit-testing-guide"
REPO_BLOB = REPO_URL + "/blob/main"

SITE_NAME = "Java Unit Testing Guide"
SITE_TAGLINE = "A friendly, hands-on path from your first @Test to advanced Mockito mocking, verification and spies."

SECTION_LABELS = {
    "junit": "Getting Started",
    "mockito": "Mocking with Mockito",
    "workflow": "Practices & Workflow",
}

CHAPTERS = [
    dict(slug="01-beginner-junit5", section="junit", num=1,
         title="Getting started with JUnit 5",
         summary="@Test, lifecycle hooks, @Nested, @DisplayName, and how to run a test.",
         test="src/test/java/com/example/guide/beginner/CalculatorTest.java"),
    dict(slug="02-assertions-assertj", section="junit", num=2,
         title="Assertions with AssertJ",
         summary="Fluent, readable assertions for values, collections and exceptions.",
         test="src/test/java/com/example/guide/beginner/CalculatorTest.java"),
    dict(slug="03-junit5-parameterized", section="junit", num=3,
         title="Parameterized & advanced JUnit 5",
         summary="@ParameterizedTest, tags, assumptions, and timeouts.",
         test="src/test/java/com/example/guide/beginner/JUnit5FeaturesTest.java"),
    dict(slug="04-mockito-basics", section="mockito", num=4,
         title="Mockito basics: your first mock",
         summary="Why we mock at all, @Mock, and stubbing with when().",
         test="src/test/java/com/example/guide/mockito/UserServiceMockitoBasicsTest.java"),
    dict(slug="05-mocking-styles", section="mockito", num=5,
         title="Different ways of mocking",
         summary="Mockito.mock(), @Mock, @InjectMocks, deep stubs, and lenient mode.",
         test="src/test/java/com/example/guide/mockito/MockingStylesTest.java"),
    dict(slug="06-verification", section="mockito", num=6,
         title="Verification",
         summary="verify(), times(), InOrder, ArgumentCaptor, verifyNoInteractions.",
         test="src/test/java/com/example/guide/mockito/VerificationTest.java"),
    dict(slug="07-spies", section="mockito", num=7,
         title="Spies (partial mocking)",
         summary="@Spy, the doReturn().when() gotcha, and mock vs. spy.",
         test="src/test/java/com/example/guide/mockito/SpyTest.java"),
    dict(slug="08-advanced-mockito", section="mockito", num=8,
         title="Advanced Mockito",
         summary="Exceptions, consecutive returns, custom Answer, BDD style, static mocking.",
         test="src/test/java/com/example/guide/advanced/AdvancedMockingTest.java"),
    dict(slug="09-best-practices", section="workflow", num=9,
         title="Best practices & anti-patterns",
         summary="FIRST principles, what (not) to mock, naming, coverage traps.",
         test=None),
    dict(slug="10-git-workflow", section="workflow", num=10,
         title="Git workflow for test changes",
         summary="Branch-per-change, commit hygiene, keeping branches current.",
         test=None),
    dict(slug="11-pr-guide", section="workflow", num=11,
         title="Pull request guide",
         summary="A PR checklist, review etiquette, and CI expectations.",
         test=None),
]
BY_SLUG = {c["slug"]: c for c in CHAPTERS}


# ---------------------------------------------------------------------------
# Markdown -> HTML
# ---------------------------------------------------------------------------

def rewrite_link(match: re.Match) -> str:
    target = match.group(1)
    if target.startswith(("http://", "https://", "#", "mailto:")):
        return match.group(0)
    if target == "../README.md":
        return "](index.html)"
    if re.fullmatch(r"\d\d-[\w-]+\.md", target):
        return "](" + target[:-3] + ".html)"
    if target.startswith("../"):
        return "](" + REPO_BLOB + "/" + target[3:] + ")"
    return match.group(0)


def preprocess(text: str) -> str:
    text = re.sub(r"\]\(([^)]+)\)", rewrite_link, text)
    text = re.sub(r"^- \[ \] ", '- <input type="checkbox" disabled> ', text, flags=re.M)
    text = re.sub(r"^- \[x\] ", '- <input type="checkbox" checked disabled> ', text, flags=re.M)
    return text


def render_markdown(text: str) -> str:
    text = preprocess(text)
    body = md.markdown(
        text,
        extensions=["fenced_code", "tables", "sane_lists", "toc"],
        extension_configs={"toc": {"permalink": False, "title": ""}},
    )
    # External links open in a new tab.
    body = re.sub(
        r'<a href="(https?://[^"]+)"',
        r'<a href="\1" target="_blank" rel="noopener noreferrer"',
        body,
    )
    return body


def extract_toc(body_html: str):
    return [
        (heading_id, re.sub(r"<[^>]+>", "", text).strip())
        for heading_id, text in re.findall(r'<h2 id="([^"]+)">(.*?)</h2>', body_html, flags=re.S)
    ]


# ---------------------------------------------------------------------------
# Templates
# ---------------------------------------------------------------------------

def sidebar_html(active_slug: str | None) -> str:
    sections = {}
    for c in CHAPTERS:
        sections.setdefault(c["section"], []).append(c)

    parts = []
    for key in ("junit", "mockito", "workflow"):
        parts.append(f'<p class="sidebar-heading">{SECTION_LABELS[key]}</p>')
        parts.append("<ol>")
        for c in sections[key]:
            active = ' class="active"' if c["slug"] == active_slug else ""
            parts.append(
                f'<li{active}><a href="{c["slug"]}.html">'
                f'<span class="index-num">{c["num"]:02d}</span>{c["title"]}</a></li>'
            )
        parts.append("</ol>")
    return "\n".join(parts)


def toc_rail_html(toc_items) -> str:
    if not toc_items:
        return ""
    links = "\n".join(f'<li><a href="#{hid}">{text}</a></li>' for hid, text in toc_items)
    return f'''<aside class="toc-rail">
  <p class="toc-title">On this page</p>
  <ul>{links}</ul>
</aside>'''


def page_shell(*, title: str, description: str, active_slug: str | None, main_html: str, with_toc: bool) -> str:
    layout_class = "layout with-toc" if with_toc else "layout"
    return f'''<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<title>{title} · {SITE_NAME}</title>
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="description" content="{description}">
<link rel="icon" href="assets/favicon.svg" type="image/svg+xml">
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;500;600&display=swap" rel="stylesheet">
<link id="hljs-theme" rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/styles/github.min.css">
<link rel="stylesheet" href="assets/style.css">
</head>
<body>
<a class="skip-link" href="#main">Skip to content</a>
<header class="topbar">
  <button class="nav-toggle" aria-label="Toggle navigation" aria-expanded="false">☰</button>
  <a class="brand" href="index.html"><span class="logo-mark">J</span><span>Java Testing Guide</span></a>
  <nav class="top-links">
    <a href="index.html#chapters"><span class="label">Chapters</span></a>
    <a href="{REPO_URL}" target="_blank" rel="noopener noreferrer"><span class="label">GitHub ↗</span></a>
    <button id="theme-toggle" aria-label="Toggle color theme">🌙</button>
  </nav>
</header>
<div class="sidebar-scrim"></div>
<div class="{layout_class}">
  <aside class="sidebar" id="sidebar">
    <nav aria-label="Chapters">
      {sidebar_html(active_slug)}
    </nav>
  </aside>
  {main_html}
</div>
<footer class="site-footer">
  <p>Java Unit Testing Guide — <a href="{REPO_URL}" target="_blank" rel="noopener noreferrer">source on GitHub</a>.
  Built from the same <code>docs/*.md</code> chapters that ship with the repo.</p>
</footer>
<script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/highlight.min.js"></script>
<script src="assets/main.js"></script>
</body>
</html>
'''


def chapter_page(chapter: dict) -> str:
    idx = CHAPTERS.index(chapter)
    prev_c = CHAPTERS[idx - 1] if idx > 0 else None
    next_c = CHAPTERS[idx + 1] if idx < len(CHAPTERS) - 1 else None

    source_md = (DOCS / f"{chapter['slug']}.md").read_text(encoding="utf-8")
    # Drop the leading "# N. Title" — the page template renders its own H1 —
    # and the "Companion code:" line, which is replaced by the source-link banner.
    lines = source_md.splitlines()
    body_lines = []
    skipped_h1 = False
    for line in lines:
        if not skipped_h1 and line.startswith("# "):
            skipped_h1 = True
            continue
        if line.startswith("Companion code:"):
            continue
        # The trailing "## Next" / "## Back to the start" section is
        # replaced on the site by the generated pager below.
        if line.strip() in ("## Next", "## Back to the start"):
            break
        body_lines.append(line)
    body_md = "\n".join(body_lines).strip()

    body_html = render_markdown(body_md)
    toc_items = extract_toc(body_html)

    source_banner = ""
    if chapter["test"]:
        source_banner = (
            f'<a class="source-link" href="{REPO_BLOB}/{chapter["test"]}" target="_blank" rel="noopener noreferrer">'
            f'▶ Run the companion tests: <code>{chapter["test"].split("/")[-1]}</code></a>'
        )

    def pager_link(c, cls, label):
        if not c:
            return '<span></span>'
        return (
            f'<a class="pager-link {cls}" href="{c["slug"]}.html">'
            f'<span class="pager-label">{label}</span>'
            f'<span class="pager-title">{c["title"]}</span></a>'
        )

    main_html = f'''<main id="main">
    <div class="chapter-header">
      <p class="chapter-kicker">Chapter {chapter["num"]:02d} of {len(CHAPTERS)}</p>
      <h1>{chapter["title"]}</h1>
    </div>
    {source_banner}
    <article class="prose">
      {body_html}
    </article>
    <nav class="pager">
      {pager_link(prev_c, "prev", "← Previous")}
      {pager_link(next_c, "next", "Next →")}
    </nav>
  </main>
  {toc_rail_html(toc_items)}'''

    return page_shell(
        title=chapter["title"],
        description=chapter["summary"],
        active_slug=chapter["slug"],
        main_html=main_html,
        with_toc=bool(toc_items),
    )


# ---------------------------------------------------------------------------
# Landing page
# ---------------------------------------------------------------------------

FEATURES = [
    ("🧭", "Structured, not scattered", "Eleven short chapters, beginner to advanced, each building on the last — not a wall of unrelated snippets."),
    ("✅", "Every example runs", "Every code sample has a matching test class in the repo. Clone it, run <code>mvn test</code>, and see all 48 tests pass."),
    ("🎭", "Mocking, demystified", "Five different ways to create a mock, real verification patterns, and the #1 spy gotcha that trips everyone up once."),
    ("🔀", "Workflow included", "Not just testing — a branch-per-change git workflow and a PR checklist, so the tests you write actually ship cleanly."),
]


def landing_page() -> str:
    section_order = ["junit", "mockito", "workflow"]
    section_badge = {"junit": "junit", "mockito": "mockito", "workflow": "workflow"}

    tracks_html = []
    for key in section_order:
        chapters = [c for c in CHAPTERS if c["section"] == key]
        cards = "\n".join(
            f'''<a class="chapter-card" href="{c['slug']}.html">
      <span class="chapter-num">Chapter {c['num']:02d}</span>
      <h3>{c['title']}</h3>
      <p>{c['summary']}</p>
    </a>'''
            for c in chapters
        )
        tracks_html.append(f'''<div class="track">
    <h3 class="track-title">{SECTION_LABELS[key]}
      <span class="track-badge {section_badge[key]}">{len(chapters)} chapters</span>
    </h3>
    <div class="chapter-grid">
      {cards}
    </div>
  </div>''')

    feature_cards = "\n".join(
        f'''<div class="feature-card">
      <span class="icon">{icon}</span>
      <h3>{title}</h3>
      <p>{desc}</p>
    </div>'''
        for icon, title, desc in FEATURES
    )

    main_html = f'''<main id="main" style="padding-top:0;">
    <section class="hero">
      <span class="eyebrow">☕ JUnit 5 · Mockito · AssertJ</span>
      <h1>Learn Java testing, <span class="accent">from your first assert to your last mock</span></h1>
      <p class="lead">{SITE_TAGLINE}</p>
      <div class="hero-actions">
        <a class="btn btn-primary" href="01-beginner-junit5.html">Start the tutorial →</a>
        <a class="btn btn-secondary" href="{REPO_URL}" target="_blank" rel="noopener noreferrer">View source on GitHub</a>
      </div>
      <div class="quickstart">
<pre><code class="language-bash">git clone {REPO_URL}.git
cd java-unit-testing-guide
mvn test   # 48 tests, all passing — read the code alongside the guide</code></pre>
      </div>
    </section>

    <section class="section">
      <div class="section-heading">
        <h2>Why this guide</h2>
        <p>Built to be read chapter by chapter, or jumped into for one specific technique.</p>
      </div>
      <div class="feature-grid">
        {feature_cards}
      </div>
    </section>

    <section class="section" id="chapters">
      <div class="section-heading">
        <h2>All 11 chapters</h2>
        <p>Grouped the way you'll actually use them.</p>
      </div>
      {"".join(tracks_html)}
    </section>

    <section class="cta-band">
      <h2>Ready to write your first mock?</h2>
      <p>Chapter 4 walks through exactly why we mock and how, using a real UserService/UserRepository pair.</p>
      <a class="btn btn-primary" href="04-mockito-basics.html">Jump to Mockito basics →</a>
    </section>
  </main>'''

    return page_shell(
        title="Home",
        description=SITE_TAGLINE,
        active_slug=None,
        main_html=main_html,
        with_toc=False,
    )


# ---------------------------------------------------------------------------
# Build
# ---------------------------------------------------------------------------

def main():
    if OUT.exists():
        shutil.rmtree(OUT)
    OUT.mkdir(parents=True)

    (OUT / "index.html").write_text(landing_page(), encoding="utf-8")

    for chapter in CHAPTERS:
        (OUT / f"{chapter['slug']}.html").write_text(chapter_page(chapter), encoding="utf-8")

    shutil.copytree(ASSETS, OUT / "assets")
    (OUT / ".nojekyll").write_text("", encoding="utf-8")

    print(f"Built {len(CHAPTERS) + 1} pages into {OUT.relative_to(ROOT)}/")


if __name__ == "__main__":
    main()
