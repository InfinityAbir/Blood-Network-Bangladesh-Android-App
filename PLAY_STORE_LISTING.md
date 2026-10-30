# Play Console listing — draft copy

Paste these into the Play Console store listing form. Nothing here is submitted automatically —
this is copy for you to review and paste in yourself (submitting needs your Play Console login).

## Store listing

**App name** (30 char max — this is 25):
```
Blood Network Bangladesh
```

**Short description** (80 char max — this is 74):
```
Find compatible blood donors nearby or post a request — fast, free, local.
```

**Full description** (4000 char max):
```
Blood Network Bangladesh connects people who need blood with donors nearby — no phone
tree, no waiting on a Facebook post to spread.

FOR REQUESTERS
• Post a blood request with hospital, blood group, and urgency
• Get matched automatically with compatible, available donors near you
• See distance and availability before you call

FOR DONORS
• Create a donor profile with your blood group and location
• Toggle your availability on or off anytime
• Get notified when a nearby request matches your blood group, and when you become
  available, nearby requesters who match you are notified too
• Check your eligibility to donate before you commit

BUILT FOR TRUST
• Admin-reviewed donor verification
• Report and moderation tools for misuse
• An AI assistant to answer common donation questions
• No ads, no ad trackers, no selling your data

Blood Network Bangladesh is built to make one thing faster: getting a compatible donor
to someone who needs blood, right when it matters.
```

**What's new** (release notes, v1.0.0):
```
First release: donor search, blood requests, donor profiles with availability, AI
eligibility assistant, and realtime notifications when you're matched or a compatible
donor becomes available nearby.
```

**App category:** Medical (or Lifestyle, if Medical requires additional Play review
documentation you'd rather skip for v1 — Medical is the more accurate fit).

**Contact details:** developer email/phone/website are on the in-app About screen
(Settings → About) — pull the current values from there since an admin can edit them.

**Privacy policy URL:** `https://infinityabir.github.io/abir-hasan-portfolio/bloodnetwork-privacy-policy.html`
(source: `public/bloodnetwork-privacy-policy.html` in the `abir-hasan-portfolio` repo, deployed by its GitHub Pages workflow). Review the text before submitting.
The same URL is linked inside the app (Landing, Register, About, Edit Profile).

**Account deletion URL** (Data safety form): `https://infinityabir.github.io/abir-hasan-portfolio/bloodnetwork-delete-account.html`
(source: `public/bloodnetwork-delete-account.html` in the `abir-hasan-portfolio` repo).

---

## Data Safety form — answers

Play's exact category list changes over time; double-check these against the live form,
but this reflects what the app actually collects (see the privacy policy for full detail).

| Data type | Collected? | Shared? | Purpose | Optional? |
|---|---|---|---|---|
| Name | Yes | No | Account functionality | Required |
| Phone number | Yes | Yes (with a matched requester/donor only) | Account functionality, communications | Required |
| Email address | Yes | No | Account functionality | Optional |
| Approximate location (district/upazila) | Yes | Yes (visible in donor search results) | App functionality (matching) | Required for donors |
| Photos (optional profile picture, uploaded by the user) | Yes | Yes (visible in donor search and request cards) | App functionality | Optional |
| Precise location (lat/long) | **No** — the app has no location permission and never sends coordinates | — | — | — |
| Health info (blood group, donation history, availability) | Yes | Yes (blood group + availability visible in search) | App functionality (matching) | Required for donors |
| In-app messages (AI chatbot) | Yes | Yes (sent to Groq to generate a reply) | App functionality | Optional (only if you use the chatbot) |
| App activity / diagnostics (IP, request logs) | Yes | No | Security, abuse prevention, debugging | N/A (automatic) |

**Is all user data encrypted in transit?** Yes — HTTPS only, enforced at the OS level.

**Do you provide a way for users to request data deletion?** Yes. In-app: Edit Profile →
Delete my account (password-confirmed), plus the web page above. **Requires the backend
endpoint `POST /api/auth/delete-account` (body `{"currentPassword": "..."}`) to exist** — until
it does, the app falls back to an email request. Don't ship before the endpoint is live.

**Data collected is NOT used for:** advertising or marketing purposes (there's no ad SDK
in the app), and is not sold to third parties.

---

## Screenshots

Not produced by this pass — this sandbox has no Android SDK/emulator to run the app.
Once you build and run it in Android Studio, Play Console currently wants (check current
requirements at submission time, these change):
- Phone: at least 2 screenshots, 16:9 or 9:16, JPEG/PNG, 320–3840px per side
- A feature graphic: 1024×500 PNG/JPEG

Good candidate screens to capture: Landing, Find Blood (with results), Donor Dashboard,
Request Blood form, Admin Analytics (shows the app has real depth, not just a form app).
