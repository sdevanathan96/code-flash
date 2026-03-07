# code-flash — Usage Guide

A practical walkthrough of how to use the app day-to-day.

---

## First-time setup

### Step 1 — Import your problems

Go to **Import** (`/import`).

**Option A — Grind 175 (recommended starting point)**
Click **Re-seed** to load the curated Grind 175 list. This gives you 175 high-frequency interview problems immediately, no LeetCode account needed.

**Option B — Your LeetCode lists**
If your session cookie is configured, your personal LeetCode lists appear under "Your LeetCode Lists". Click **Import** next to any list.

**Option C — Paste a URL**
Paste any of the following into the URL box and click **Import**:
- A LeetCode list URL: `https://leetcode.com/problem-list/abc123/`
- A study plan URL: `https://leetcode.com/studyplan/leetcode-75/`
- A single problem slug: `two-sum`

> Imports can take 1–2 minutes for large lists. The app fetches metadata for each problem with a 200ms delay to avoid rate limiting.

### Step 2 — Wait for enrichment

After importing, a background job fetches topic tags and company tags for every problem. This runs automatically — check back after ~10 minutes. You'll know it's done when the **Topics** dropdown on the Problems page is fully populated.

To force enrichment immediately:
```bash
psql -U postgres -d codeflash -c "UPDATE app_settings SET value = '2000-01-01T00:00:00' WHERE key = 'last_enriched_at';"
```
Then restart the app.

---

## Daily workflow

### 1. Check the dashboard

The dashboard (`/`) shows:
- **Due Today** — how many problems the SRS engine has scheduled for review
- **Day Streak** — consecutive days with at least one solve
- **Weakest Patterns** — your bottom patterns by mastery score with trend arrows

Start here every session to know what needs attention.

### 2. Start a review

Click **Start Review** on the dashboard or **Review these** on the Problems page.

**Choose a mode:**

| Mode | When to use |
|---|---|
| **Due Only** | Daily maintenance — work through what the SRS scheduled |
| **Sequential** | Learning a new list — go through problems in order |
| **Mixed** | Active study — sequential list with due problems injected randomly |

**During review:**
1. Click **Open on LeetCode** — solve the problem in a new tab
2. Come back and optionally expand **past notes** to compare your approach
3. Write a note — key insight, pattern used, the gotcha that tripped you up
4. Rate your confidence:

| Rating | When |
|---|---|
| **Again** | Completely forgot — couldn't start |
| **Hard** | Got there but struggled significantly |
| **Good** | Solved with some effort |
| **Easy** | Solved immediately, felt trivial |

The SRS engine updates the interval and next due date based on your rating.

---

## Browsing problems

Go to **Problems** (`/problems`).

**Filters:**
- **List** — filter to a specific imported list (e.g. Grind 175)
- **Topic** — filter by pattern (Arrays, Dynamic Programming, etc.)
- **Company** — type a company name to filter by company tag
- **Difficulty** — Easy / Medium / Hard
- **Due only** — show only problems due today or overdue

When a list is selected, a **topic breakdown** appears showing mastery per pattern within that list. Click any topic card to drill into that pattern.

**The clock icon** (🕐) on each row opens the problem detail page — solve history, SRS state, and past notes.

---

## Managing lists

### Rename a list
Go to **Import** (`/import`). Under "Imported Lists", click the pencil icon next to any list name. Type the new name and press Enter or click ✓.

> Renaming updates the list everywhere in the app. The browse link updates automatically without a page reload.

### Re-import a list
Click **Import** next to the list name again. Existing problems are skipped (deduplicated by slug), new problems are added.

### Delete a list
Currently via psql only:
```bash
# If CASCADE is set up:
psql -U postgres -d codeflash -c "DELETE FROM problem_lists WHERE name = 'your list name';"

# If not:
psql -U postgres -d codeflash -c "DELETE FROM problem_list_items WHERE problem_list_id = (SELECT id FROM problem_lists WHERE name = 'your list name');"
psql -U postgres -d codeflash -c "DELETE FROM problem_lists WHERE name = 'your list name';"
```
Problems themselves are not deleted — only the list association.

---

## Understanding SRS scheduling

Each problem has:
- **Interval** — days until next review
- **Ease factor** — multiplier applied to the interval on each successful review (starts at 2.5)
- **Next due date** — computed as `today + interval` after each solve

**Example progression with all GOOD ratings:**
```
Day 0:  Solve → interval = 1  → due Day 1
Day 1:  Solve → interval = 2  → due Day 3
Day 3:  Solve → interval = 5  → due Day 8
Day 8:  Solve → interval = 13 → due Day 21
...
```

**Velocity adjustment** compresses intervals when you're solving frequently. If you solve 6+ problems/day over the last 3 days (vs the baseline of 3/day), intervals shrink so you review more often. At low velocity, SM-2 baseline applies — intervals never expand beyond the algorithm's natural output.

**Due labels on the Problems page:**
- `Overdue` — past due date
- `Today` — due today
- `Tomorrow` — due tomorrow
- `In N days` — scheduled in the future
- `Not started` — never solved, no SRS state yet

---

## Refreshing the LeetCode session cookie

When imports start returning errors or the available lists stop loading:

1. Go to `leetcode.com` and log in
2. Open DevTools (F12) → Application → Cookies → `leetcode.com`
3. Copy the value of `LEETCODE_SESSION` and `csrftoken`
4. Update `application.yml`:
   ```yaml
   leetcode:
     session-cookie: NEW_SESSION_VALUE
     csrf-token: NEW_CSRF_VALUE
   ```
5. Restart the app

---

## Useful psql commands

```bash
# Check problem + list counts
psql -U postgres -d codeflash -c "SELECT pl.name, COUNT(pli.problem_id) FROM problem_lists pl LEFT JOIN problem_list_items pli ON pli.problem_list_id = pl.id GROUP BY pl.name;"

# Check tag type distribution
psql -U postgres -d codeflash -c "SELECT tag_type, COUNT(*) FROM tags GROUP BY tag_type;"

# Check Flyway migration history
psql -U postgres -d codeflash -c "SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;"

# Reset enrichment to re-run immediately
psql -U postgres -d codeflash -c "UPDATE app_settings SET value = '2000-01-01T00:00:00' WHERE key = 'last_enriched_at';"

# Reset last_enriched_at to a specific time
psql -U postgres -d codeflash -c "UPDATE app_settings SET value = '2026-03-06T14:30:57' WHERE key = 'last_enriched_at';"
```

---

## Roadmap

- [ ] React + TypeScript + Tailwind frontend (replacing Thymeleaf)
- [ ] Pattern drill mode — 10 problems of the same tag back to back
- [ ] Time-to-solve tracking per problem
- [ ] Predicted mastery dates per pattern
- [ ] Company tag analytics
- [ ] Timed mock interview mode