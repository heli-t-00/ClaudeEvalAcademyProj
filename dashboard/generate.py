#!/usr/bin/env python3
"""
Dashboard generator for ClaudeEvalAcademyProj.
Reads cucumber JSON report + config.yml and produces docs/index.html.
Run by GitHub Actions after every test run.
"""

import json
import yaml
import sys
import os
from datetime import datetime, timezone
from pathlib import Path

# ─── Paths ────────────────────────────────────────────────────────────────────
ROOT        = Path(__file__).parent.parent
CONFIG_FILE = ROOT / "dashboard" / "config.yml"
JSON_REPORT = ROOT / "target" / "cucumber-reports" / "report.json"
HISTORY_FILE= ROOT / "dashboard" / "history.json"
OUTPUT_DIR  = ROOT / "docs"
OUTPUT_FILE = OUTPUT_DIR / "index.html"

OUTPUT_DIR.mkdir(exist_ok=True)

# ─── Load config ──────────────────────────────────────────────────────────────
with open(CONFIG_FILE) as f:
    cfg = yaml.safe_load(f)

# ─── Load Cucumber JSON report ────────────────────────────────────────────────
if not JSON_REPORT.exists():
    print(f"ERROR: {JSON_REPORT} not found")
    sys.exit(1)

with open(JSON_REPORT) as f:
    report = json.load(f)

# ─── Parse results ────────────────────────────────────────────────────────────
now_utc = datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M UTC")
run_id  = os.environ.get("GITHUB_RUN_ID", "local")
run_num = os.environ.get("GITHUB_RUN_NUMBER", "?")
sha     = os.environ.get("GITHUB_SHA", "")[:7] or "local"

features = []
total_pass = total_fail = total_skip = 0
total_duration = 0.0
scenario_results = []

for feature in report:
    f_pass = f_fail = f_skip = 0
    f_scenarios = []
    for element in feature.get("elements", []):
        steps     = element.get("steps", [])
        afters    = element.get("after", [])
        all_steps = steps + afters
        statuses  = [s.get("result", {}).get("status", "skipped") for s in steps]
        duration  = sum(s.get("result", {}).get("duration", 0) for s in all_steps) / 1e9

        if "failed" in statuses:
            status = "failed"
            f_fail += 1
            total_fail += 1
        elif "skipped" in statuses or not statuses:
            status = "skipped"
            f_skip += 1
            total_skip += 1
        else:
            status = "passed"
            f_pass += 1
            total_pass += 1

        total_duration += duration
        name = element.get("name", "")

        # Find failure message
        fail_msg = ""
        for step in steps:
            r = step.get("result", {})
            if r.get("status") == "failed":
                fail_msg = r.get("error_message", "")[:200].replace("\n", " ")
                break

        is_flaky = name in cfg["stability"]["flaky_scenarios"]
        f_scenarios.append({"name": name, "status": status,
                             "duration": round(duration, 2),
                             "flaky": is_flaky, "fail_msg": fail_msg})
        scenario_results.append({"feature": feature.get("name",""), "name": name,
                                  "status": status, "flaky": is_flaky})

    features.append({
        "name": feature.get("name", ""),
        "pass": f_pass, "fail": f_fail, "skip": f_skip,
        "total": f_pass + f_fail + f_skip,
        "risk": cfg["feature_risk"].get(feature.get("name",""), "UNKNOWN"),
        "suggestions": cfg["ai_suggestions"].get(feature.get("name",""), []),
        "scenarios": f_scenarios,
    })

total_scenarios = total_pass + total_fail + total_skip
pass_rate = round(total_pass / total_scenarios * 100, 1) if total_scenarios else 0

# ─── Stability Score (AI metric) ──────────────────────────────────────────────
flaky_names   = set(cfg["stability"]["flaky_scenarios"])
flaky_passed  = sum(1 for s in scenario_results if s["name"] in flaky_names and s["status"] == "passed")
flaky_total   = len(flaky_names)
flaky_rate    = flaky_passed / flaky_total if flaky_total else 1.0
stability     = round(
    pass_rate * cfg["stability"]["pass_weight"] +
    flaky_rate * 100 * cfg["stability"]["consistency_weight"], 1
)
flaky_failing = [s["name"] for s in scenario_results if s["name"] in flaky_names and s["status"] == "failed"]

# ─── Load / update run history ────────────────────────────────────────────────
history = []
if HISTORY_FILE.exists():
    with open(HISTORY_FILE) as f:
        history = json.load(f)

history.append({
    "run_id": run_id, "run_num": run_num, "sha": sha,
    "timestamp": now_utc,
    "pass": total_pass, "fail": total_fail,
    "pass_rate": pass_rate, "stability": stability,
    "duration": round(total_duration, 1)
})
history = history[-20:]  # keep last 20 runs

with open(HISTORY_FILE, "w") as f:
    json.dump(history, f, indent=2)

# ─── Threshold colours ────────────────────────────────────────────────────────
def colour(value, metric="pass_rate"):
    t = cfg["thresholds"][metric]
    if value >= t["green"]: return "#27AE60"
    if value >= t["amber"]: return "#E67E22"
    return "#C0392B"

def badge_colour(risk):
    return {"CRITICAL": "#C0392B", "MEDIUM": "#E67E22",
            "STABLE": "#27AE60", "LOW": "#2980B9"}.get(risk, "#7F8C8D")

# ─── Chart data ───────────────────────────────────────────────────────────────
feature_labels   = json.dumps([f["name"] for f in features])
feature_pass     = json.dumps([f["pass"] for f in features])
feature_fail     = json.dumps([f["fail"] for f in features])
history_labels   = json.dumps([h["run_num"] for h in history])
history_pass_rate= json.dumps([h["pass_rate"] for h in history])
history_stability= json.dumps([h["stability"] for h in history])

# ─── Build bug rows ───────────────────────────────────────────────────────────
bugs = cfg.get("bugs", [])

def bug_priority_colour(priority):
    return {"High": "#C0392B", "Medium": "#E67E22", "Low": "#2980B9"}.get(priority, "#7F8C8D")

def bug_status_colour(status):
    return {"To Do": "#7F8C8D", "In Progress": "#2980B9",
            "Done": "#27AE60", "Closed": "#27AE60"}.get(status, "#7F8C8D")

bug_rows = ""
for b in bugs:
    pc  = bug_priority_colour(b["priority"])
    sc  = bug_status_colour(b["status"])
    bug_rows += f"""
    <tr>
      <td><a href="{b['url']}" target="_blank" style="color:#2980B9;font-weight:700">{b['key']}</a></td>
      <td>{b['summary']}</td>
      <td><span class='badge' style='background:{badge_colour(cfg["feature_risk"].get(b["feature"],"UNKNOWN"))}'>{b['feature']}</span></td>
      <td><span class='badge' style='background:{pc}'>{b['priority']}</span></td>
      <td><span class='badge' style='background:{sc}'>{b['status']}</span></td>
      <td style='font-size:0.78rem;color:#555'>{b['root_cause']}</td>
      <td style='font-size:0.78rem;color:#27AE60'>{b['fix_applied']}</td>
    </tr>"""

open_bugs   = sum(1 for b in bugs if b["status"] not in ("Done", "Closed"))
closed_bugs = sum(1 for b in bugs if b["status"] in ("Done", "Closed"))

# ─── Load AI test proposals ───────────────────────────────────────────────────
PROPOSALS_FILE = ROOT / "dashboard" / "ai_test_proposals.json"
proposals = []
if PROPOSALS_FILE.exists():
    with open(PROPOSALS_FILE) as f:
        proposals_data = json.load(f)
        proposals = proposals_data.get("proposals", [])

def priority_colour(p):
    return {"HIGH": "#C0392B", "MEDIUM": "#E67E22", "LOW": "#2980B9"}.get(p, "#7F8C8D")

def category_icon(cat):
    if "Regression" in cat:   return "🔁"
    if "Flaky"     in cat:    return "⚠️"
    if "Security"  in cat:    return "🔒"
    if "Uncovered" in cat:    return "📋"
    if "Persona"   in cat:    return "👤"
    if "Post"      in cat:    return "🏁"
    return "🔍"

proposal_rows = ""
for p in proposals:
    cat_icon = category_icon(p["category"])
    source_short = p["source"][:80] + "…" if len(p["source"]) > 80 else p["source"]
    proposal_rows += f"""
    <tr>
      <td style='font-weight:700;color:#2980B9'>{p['id']}</td>
      <td><span class='badge' style='background:{priority_colour(p["priority"])}'>{p['priority']}</span></td>
      <td><span class='badge' style='background:{badge_colour(cfg["feature_risk"].get(p["feature"],"UNKNOWN"))}'>{p['feature']}</span></td>
      <td>{cat_icon} {p['category']}</td>
      <td style='font-weight:600'>{p['title']}</td>
      <td style='font-size:0.78rem;color:#555'>{source_short}</td>
    </tr>"""

proposals_by_priority = {"HIGH": 0, "MEDIUM": 0, "LOW": 0}
for p in proposals:
    proposals_by_priority[p.get("priority","LOW")] += 1

# ─── Build scenario rows ──────────────────────────────────────────────────────
scenario_rows = ""
for feat in features:
    for s in feat["scenarios"]:
        icon    = "✅" if s["status"] == "passed" else ("❌" if s["status"] == "failed" else "⏭")
        flaky   = " <span class='badge badge-flaky'>FLAKY</span>" if s["flaky"] else ""
        fail_td = f"<td class='fail-msg'>{s['fail_msg']}</td>" if s["status"] == "failed" else "<td>—</td>"
        scenario_rows += f"""
        <tr class='row-{s["status"]}'>
          <td>{feat['name']}</td>
          <td>{icon} {s['name']}{flaky}</td>
          <td class='center'>{s['duration']}s</td>
          {fail_td}
        </tr>"""

# ─── Build failed scenario summary rows ───────────────────────────────────────
failed_rows = ""
for feat in features:
    for s in feat["scenarios"]:
        if s["status"] == "failed":
            flaky = " <span class='badge badge-flaky'>FLAKY</span>" if s["flaky"] else ""
            failed_rows += f"""
            <tr class='row-failed'>
              <td>{feat['name']}</td>
              <td>❌ {s['name']}{flaky}</td>
              <td class='center'>{s['duration']}s</td>
              <td class='fail-msg'>{s['fail_msg']}</td>
            </tr>"""

# ─── Build run history table rows ─────────────────────────────────────────────
history_rows = ""
for h in reversed(history):
    rate_col = colour(h["pass_rate"])
    stab_col = colour(h["stability"], "stability_score")
    verdict  = "✅ PASS" if h["fail"] == 0 else f"❌ {h['fail']} FAILED"
    verdict_col = "#27AE60" if h["fail"] == 0 else "#C0392B"
    history_rows += f"""
    <tr>
      <td class='center'>#{h['run_num']}</td>
      <td class='center'>{h['sha']}</td>
      <td>{h['timestamp']}</td>
      <td class='center'><b style='color:{verdict_col}'>{verdict}</b></td>
      <td class='center'>{h['pass']} / {h['pass'] + h['fail']}</td>
      <td class='center' style='color:{rate_col}'><b>{h['pass_rate']}%</b></td>
      <td class='center' style='color:{stab_col}'>{h['stability']}</td>
      <td class='center'>{h['duration']}s</td>
    </tr>"""

# ─── Overall verdict for this run ─────────────────────────────────────────────
verdict_text  = "ALL TESTS PASSED" if total_fail == 0 else f"{total_fail} TEST{'S' if total_fail > 1 else ''} FAILED"
verdict_bg    = "#1E8449" if total_fail == 0 else "#922B21"
verdict_icon  = "✅" if total_fail == 0 else "❌"

# ─── AI suggestions section ───────────────────────────────────────────────────
suggestion_html = ""
for feat in features:
    if feat["suggestions"]:
        items = "".join(f"<li>{s}</li>" for s in feat["suggestions"])
        suggestion_html += f"""
        <div class='suggestion-block'>
          <div class='suggestion-title'>
            <span class='badge' style='background:{badge_colour(feat["risk"])}'>{feat["risk"]}</span>
            {feat["name"]}
          </div>
          <ul>{items}</ul>
        </div>"""

# ─── HTML ─────────────────────────────────────────────────────────────────────
html = f"""<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta http-equiv="refresh" content="900">
<title>{cfg['project']['name']} — Test Dashboard</title>
<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
<style>
  :root {{
    --dark: #1C2833; --green: #27AE60; --orange: #E67E22;
    --red: #C0392B; --blue: #2980B9; --light: #F2F3F4;
    --white: #fff; --mid: #BDC3C7; --text: #2C3E50;
  }}
  * {{ box-sizing: border-box; margin: 0; padding: 0; }}
  body {{ font-family: 'Segoe UI', Arial, sans-serif; background: #EAECEE; color: var(--text); }}

  /* ── Header ── */
  .header {{ background: var(--dark); color: white; padding: 20px 32px; display:flex; justify-content:space-between; align-items:center; }}
  .header h1 {{ font-size: 1.4rem; font-weight: 700; }}
  .header .meta {{ font-size: 0.8rem; opacity: 0.7; text-align:right; }}
  .header a {{ color: #85C1E9; text-decoration:none; }}

  /* ── Layout ── */
  .container {{ max-width: 1400px; margin: 0 auto; padding: 24px; }}
  .grid-5 {{ display:grid; grid-template-columns: repeat(5,1fr); gap:16px; margin-bottom:24px; }}
  .grid-4 {{ display:grid; grid-template-columns: repeat(4,1fr); gap:16px; margin-bottom:24px; }}
  .grid-2 {{ display:grid; grid-template-columns: 1fr 1fr; gap:16px; margin-bottom:24px; }}
  .grid-1 {{ margin-bottom:24px; }}
  @media(max-width:900px) {{ .grid-5,.grid-4,.grid-2 {{ grid-template-columns:1fr 1fr; }} }}
  @media(max-width:500px) {{ .grid-5,.grid-4,.grid-2 {{ grid-template-columns:1fr; }} }}

  /* ── Cards ── */
  .card {{ background:white; border-radius:10px; padding:20px; box-shadow:0 2px 6px rgba(0,0,0,0.07); }}
  .card h2 {{ font-size:0.85rem; text-transform:uppercase; letter-spacing:.08em; color:#7F8C8D; margin-bottom:12px; }}

  /* ── KPI tiles ── */
  .kpi-value {{ font-size:2.4rem; font-weight:800; line-height:1; }}
  .kpi-label {{ font-size:0.8rem; color:#7F8C8D; margin-top:4px; }}

  /* ── Status bar ── */
  .status-bar {{ height:10px; border-radius:5px; background:var(--light); overflow:hidden; margin-top:8px; }}
  .status-bar-fill {{ height:100%; border-radius:5px; transition:width .4s; }}

  /* ── Badges ── */
  .badge {{ display:inline-block; padding:2px 8px; border-radius:4px; font-size:0.72rem;
            font-weight:700; color:white; margin-right:4px; }}
  .badge-flaky {{ background:#8E44AD; }}

  /* ── Feature cards ── */
  .feature-grid {{ display:grid; grid-template-columns:repeat(auto-fill,minmax(240px,1fr)); gap:12px; }}
  .feat-card {{ border-radius:8px; padding:14px; background:var(--light); border-left:4px solid var(--mid); }}
  .feat-card .feat-name {{ font-weight:700; font-size:0.9rem; margin-bottom:6px; }}
  .feat-card .feat-stats {{ font-size:0.8rem; color:#555; }}
  .feat-card .feat-bar {{ display:flex; height:6px; border-radius:3px; overflow:hidden; margin-top:8px; }}
  .feat-card .bar-pass {{ background:var(--green); }}
  .feat-card .bar-fail {{ background:var(--red); }}

  /* ── Table ── */
  .table-wrap {{ overflow-x:auto; }}
  table {{ width:100%; border-collapse:collapse; font-size:0.82rem; }}
  th {{ background:var(--dark); color:white; padding:10px 12px; text-align:left; position:sticky; top:0; }}
  td {{ padding:8px 12px; border-bottom:1px solid #EAECEE; vertical-align:top; }}
  tr:hover td {{ background:#F8F9FA; }}
  .row-failed td {{ background:#FEF0F0; }}
  .fail-msg {{ color:var(--red); font-size:0.78rem; max-width:400px; }}
  .center {{ text-align:center; }}

  /* ── Suggestions ── */
  .suggestion-block {{ margin-bottom:14px; padding:14px; background:var(--light); border-radius:8px; }}
  .suggestion-title {{ font-weight:700; margin-bottom:8px; display:flex; align-items:center; gap:8px; }}
  .suggestion-block ul {{ padding-left:20px; }}
  .suggestion-block li {{ font-size:0.85rem; margin-bottom:4px; color:#555; }}

  /* ── Trend chip ── */
  .trend {{ font-size:0.78rem; padding:3px 8px; border-radius:12px; color:white; margin-left:8px; }}

  /* ── Footer ── */
  .footer {{ text-align:center; font-size:0.75rem; color:#999; padding:20px; }}
  .refresh-note {{ font-size:0.72rem; color:#999; text-align:right; margin-bottom:8px; }}
</style>
</head>
<body>

<div class="header">
  <div>
    <h1>🧪 {cfg['project']['name']} — Test Dashboard</h1>
    <div style="font-size:0.8rem;opacity:0.7;margin-top:4px;">
      <a href="{cfg['project']['app_url']}" target="_blank">{cfg['project']['app_url']}</a>
      &nbsp;|&nbsp;
      <a href="{cfg['project']['repo_url']}/actions" target="_blank">GitHub Actions</a>
    </div>
  </div>
  <div class="meta">
    Run #{run_num} &nbsp;|&nbsp; {sha}<br>
    {now_utc}<br>
    <span style="opacity:0.5">Auto-refreshes every 15 min</span>
  </div>
</div>

<div class="container">

  <div class="refresh-note">⏱ Page auto-refreshes every 15 minutes &nbsp;|&nbsp;
    <a href="{cfg['project']['repo_url']}/actions" target="_blank">View CI runs →</a>
  </div>

  <!-- ── Execution Summary Banner ── -->
  <div class="card grid-1 summary-banner" style="background:{verdict_bg};color:white;margin-bottom:16px;">
    <div style="display:flex;justify-content:space-between;align-items:center;flex-wrap:wrap;gap:12px;">
      <div>
        <div style="font-size:1.6rem;font-weight:800;">{verdict_icon} {verdict_text}</div>
        <div style="font-size:0.85rem;opacity:0.85;margin-top:4px;">
          Run #{run_num} &nbsp;·&nbsp; Commit {sha} &nbsp;·&nbsp; {now_utc}
        </div>
      </div>
      <div style="display:flex;gap:24px;text-align:center;">
        <div><div style="font-size:1.8rem;font-weight:800;">{total_scenarios}</div><div style="font-size:0.75rem;opacity:0.8;">TOTAL</div></div>
        <div><div style="font-size:1.8rem;font-weight:800;color:#ABEBC6;">{total_pass}</div><div style="font-size:0.75rem;opacity:0.8;">PASSED</div></div>
        <div><div style="font-size:1.8rem;font-weight:800;color:#F1948A;">{total_fail}</div><div style="font-size:0.75rem;opacity:0.8;">FAILED</div></div>
        <div><div style="font-size:1.8rem;font-weight:800;">{pass_rate}%</div><div style="font-size:0.75rem;opacity:0.8;">PASS RATE</div></div>
        <div><div style="font-size:1.8rem;font-weight:800;">{round(total_duration)}s</div><div style="font-size:0.75rem;opacity:0.8;">DURATION</div></div>
      </div>
    </div>
  </div>

  <!-- ── KPI Row ── -->
  <div class="grid-5">
    <div class="card">
      <h2>Total Scenarios</h2>
      <div class="kpi-value" style="color:var(--blue)">{total_scenarios}</div>
      <div class="kpi-label">{len(features)} features</div>
    </div>
    <div class="card">
      <h2>Pass Rate</h2>
      <div class="kpi-value" style="color:{colour(pass_rate)}">{pass_rate}%</div>
      <div class="status-bar">
        <div class="status-bar-fill" style="width:{pass_rate}%;background:{colour(pass_rate)}"></div>
      </div>
      <div class="kpi-label">{total_pass} passed / {total_fail} failed</div>
    </div>
    <div class="card">
      <h2>⭐ Stability Score <span title="AI metric: weighted pass rate accounting for historically flaky tests" style="cursor:help;font-size:0.8rem">ℹ️</span></h2>
      <div class="kpi-value" style="color:{colour(stability,'stability_score')}">{stability}</div>
      <div class="status-bar">
        <div class="status-bar-fill" style="width:{stability}%;background:{colour(stability,'stability_score')}"></div>
      </div>
      <div class="kpi-label">Flaky scenarios passing: {flaky_passed}/{flaky_total}</div>
    </div>
    <div class="card">
      <h2>Execution Time</h2>
      <div class="kpi-value" style="color:var(--text)">{round(total_duration)}s</div>
      <div class="kpi-label">{round(total_duration/60,1)} minutes total</div>
    </div>
    <div class="card">
      <h2>🐛 Open Bugs</h2>
      <div class="kpi-value" style="color:{'#C0392B' if open_bugs > 0 else '#27AE60'}">{open_bugs}</div>
      <div class="kpi-label">{closed_bugs} resolved &nbsp;|&nbsp; {len(bugs)} total</div>
    </div>
  </div>

  <!-- ── Charts Row ── -->
  <div class="grid-2">
    <div class="card">
      <h2>Pass / Fail by Feature</h2>
      <canvas id="barChart" height="200"></canvas>
    </div>
    <div class="card">
      <h2>Pass Rate Trend (last {len(history)} runs)</h2>
      <canvas id="trendChart" height="200"></canvas>
    </div>
  </div>

  <!-- ── Feature Coverage ── -->
  <div class="card grid-1">
    <h2>Feature Coverage</h2>
    <div class="feature-grid" style="margin-top:12px">
      {''.join(f"""
      <div class="feat-card" style="border-left-color:{badge_colour(f['risk'])}">
        <div class="feat-name">
          {f['name']}
          <span class="badge" style="background:{badge_colour(f['risk'])}">{f['risk']}</span>
        </div>
        <div class="feat-stats">
          ✅ {f['pass']} passed &nbsp; {'❌ ' + str(f['fail']) + ' failed' if f['fail'] else '🎉 all passed'}
          &nbsp;|&nbsp; {f['total']} total
        </div>
        <div class="feat-bar">
          <div class="bar-pass" style="width:{round(f['pass']/f['total']*100) if f['total'] else 0}%"></div>
          <div class="bar-fail" style="width:{round(f['fail']/f['total']*100) if f['total'] else 0}%"></div>
        </div>
      </div>""" for f in features)}
    </div>
  </div>

  <!-- ── Scenario Table ── -->
  <div class="card grid-1">
    <h2>Scenario Results</h2>
    <div class="table-wrap" style="margin-top:12px;max-height:500px;overflow-y:auto">
      <table>
        <thead>
          <tr>
            <th>Feature</th><th>Scenario</th><th>Duration</th><th>Failure Detail</th>
          </tr>
        </thead>
        <tbody>
          {scenario_rows}
        </tbody>
      </table>
    </div>
  </div>

  <!-- ── Failed Scenarios (only shown if there are failures) ── -->
  {'<div class="card grid-1" style="border-left:4px solid #C0392B;"><h2 style="color:#C0392B;">❌ Failed Scenarios — This Run</h2><div class="table-wrap" style="margin-top:12px"><table><thead><tr><th>Feature</th><th>Scenario</th><th>Duration</th><th>Failure Detail</th></tr></thead><tbody>' + failed_rows + '</tbody></table></div></div>' if total_fail > 0 else '<div class="card grid-1" style="border-left:4px solid #27AE60;"><h2 style="color:#27AE60;">✅ No Failures — All Scenarios Passed This Run</h2></div>'}

  <!-- ── Bug Tracker Panel ── -->
  <div class="card grid-1" style="border-left:4px solid #C0392B;">
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px;">
      <h2 style="margin-bottom:0">🐛 Defect Tracker — Jira Bugs</h2>
      <div style="font-size:0.8rem;color:#7F8C8D;">
        <span style="background:#C0392B;color:white;padding:2px 8px;border-radius:4px;font-weight:700;margin-right:6px">{open_bugs} OPEN</span>
        <span style="background:#27AE60;color:white;padding:2px 8px;border-radius:4px;font-weight:700">{closed_bugs} RESOLVED</span>
        &nbsp;&nbsp;<a href="https://testingclaude.atlassian.net/issues?jql=project%3DSCRUM%20AND%20issuetype%3DBug" target="_blank" style="color:#2980B9;font-size:0.8rem">View all in Jira →</a>
      </div>
    </div>
    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th style="width:90px">Ticket</th>
            <th>Summary</th>
            <th style="width:130px">Feature</th>
            <th style="width:80px">Priority</th>
            <th style="width:90px">Status</th>
            <th>Root Cause</th>
            <th>Fix Applied</th>
          </tr>
        </thead>
        <tbody>
          {bug_rows}
        </tbody>
      </table>
    </div>
  </div>

  <!-- ── Run History Table ── -->
  <div class="card grid-1">
    <h2>📋 Execution History (last {len(history)} runs)</h2>
    <div class="table-wrap" style="margin-top:12px;max-height:400px;overflow-y:auto">
      <table>
        <thead>
          <tr>
            <th>Run</th><th>Commit</th><th>Timestamp</th><th>Result</th>
            <th>Passed/Total</th><th>Pass Rate</th><th>Stability</th><th>Duration</th>
          </tr>
        </thead>
        <tbody>
          {history_rows}
        </tbody>
      </table>
    </div>
  </div>

  <!-- ── AI Test Proposals ── -->
  <div class="card grid-1" style="border-left:4px solid #8E44AD;">
    <div style="display:flex;justify-content:space-between;align-items:center;flex-wrap:wrap;gap:8px;margin-bottom:12px;">
      <h2 style="margin-bottom:0">🧠 AI-Proposed New Test Cases</h2>
      <div style="font-size:0.8rem;color:#7F8C8D">
        <span style="background:#C0392B;color:white;padding:2px 8px;border-radius:4px;font-weight:700;margin-right:4px">{proposals_by_priority['HIGH']} HIGH</span>
        <span style="background:#E67E22;color:white;padding:2px 8px;border-radius:4px;font-weight:700;margin-right:4px">{proposals_by_priority['MEDIUM']} MEDIUM</span>
        <span style="background:#2980B9;color:white;padding:2px 8px;border-radius:4px;font-weight:700;margin-right:8px">{proposals_by_priority['LOW']} LOW</span>
        {len(proposals)} proposals &nbsp;|&nbsp;
        <a href="{cfg['project']['repo_url']}/blob/main/dashboard/ai_test_proposals.json" target="_blank" style="color:#2980B9">View full JSON →</a>
      </div>
    </div>
    <div style="font-size:0.82rem;color:#555;margin-bottom:10px;padding:8px;background:#F8F9FA;border-radius:6px">
      Generated from: bug ticket analysis (SCRUM-91/92/93) · flaky scenario history · uncovered user stories (SCRUM-89/90) · gap analysis of existing 44 scenarios
    </div>
    <div class="table-wrap" style="max-height:420px;overflow-y:auto">
      <table>
        <thead>
          <tr>
            <th style="width:100px">ID</th>
            <th style="width:75px">Priority</th>
            <th style="width:140px">Feature</th>
            <th style="width:200px">Category</th>
            <th>Proposed Test Title</th>
            <th>Source / Basis</th>
          </tr>
        </thead>
        <tbody>
          {proposal_rows}
        </tbody>
      </table>
    </div>
  </div>

  <!-- ── AI Suggestions ── -->
  <div class="card grid-1">
    <h2>🤖 AI-Suggested Improvements</h2>
    <div style="margin-top:12px">
      {suggestion_html}
    </div>
  </div>

</div><!-- /container -->

<div class="footer">
  Generated by Claude Code (claude-sonnet-4-6) &nbsp;|&nbsp;
  {cfg['project']['name']} &nbsp;|&nbsp; {now_utc}
</div>

<script>
// ── Bar chart ──
new Chart(document.getElementById('barChart'), {{
  type: 'bar',
  data: {{
    labels: {feature_labels},
    datasets: [
      {{ label: 'Passed', data: {feature_pass}, backgroundColor: '#27AE60' }},
      {{ label: 'Failed', data: {feature_fail}, backgroundColor: '#C0392B' }}
    ]
  }},
  options: {{
    responsive: true, plugins: {{ legend: {{ position: 'top' }} }},
    scales: {{ x: {{ stacked: true }}, y: {{ stacked: true, beginAtZero: true }} }}
  }}
}});

// ── Trend chart ──
new Chart(document.getElementById('trendChart'), {{
  type: 'line',
  data: {{
    labels: {history_labels},
    datasets: [
      {{
        label: 'Pass Rate %', data: {history_pass_rate},
        borderColor: '#27AE60', backgroundColor: 'rgba(39,174,96,0.1)',
        fill: true, tension: 0.3
      }},
      {{
        label: 'Stability Score', data: {history_stability},
        borderColor: '#2980B9', backgroundColor: 'rgba(41,128,185,0.05)',
        fill: false, tension: 0.3, borderDash: [5,5]
      }}
    ]
  }},
  options: {{
    responsive: true,
    plugins: {{ legend: {{ position: 'top' }} }},
    scales: {{ y: {{ min: 0, max: 100, beginAtZero: true }} }}
  }}
}});
</script>
</body>
</html>"""

with open(OUTPUT_FILE, "w") as f:
    f.write(html)

print(f"Dashboard generated: {OUTPUT_FILE}")
print(f"Total: {total_scenarios} | Passed: {total_pass} | Failed: {total_fail} | Pass Rate: {pass_rate}% | Stability: {stability}")
