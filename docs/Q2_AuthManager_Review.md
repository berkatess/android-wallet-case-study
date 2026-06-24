# Q2 — Security-Aware Code Review: `AuthManager.kt`

This document covers the three required parts of Q2:
1. Manual review (my own findings)
2. AI-assisted review (prompt used + AI output)
3. Comparison analysis (human vs AI)

The corrected, production-ready implementation lives in a separate file (see Section 4).

---

## 1. Manual Review

I reviewed `AuthManager.kt`. Below are the issues I found, why each is a problem, and the impact it would have in a fintech application.

1. **`currentToken` and `walletPin` are stored with SharedPreferences.** Credentials this sensitive should not be kept as plain text. If they must be stored on the device, at the very least `EncryptedSharedPreferences` should be used. As it stands, these values can be read on a rooted device or extracted from a backup.

2. **BiometricPrompt is wired up as nothing more than a UI lock.** The biometric authentication is not bound to a Keystore key (`CryptoObject`), so it doesn't actually protect anything cryptographically. For sensitive operations the biometric should be tied to Android Keystore keys. Also, fingerprint verification should use `BIOMETRIC_STRONG` instead of `BIOMETRIC_WEAK`.

3. **The token and PIN are printed in plain text inside `Log.d`.** Sensitive data like this should never end up in the logs; it can leak out through logcat or via crash reports.

4. **`logout()` does not actually clear the data.** Only the in-memory variables are set to null, but the token and PIN still remain inside SharedPreferences. After logging out, this data stays on disk and can be read again later.

5. **Sensitive data and operations are handled entirely on the client side.** I would prefer a hybrid structure that works together with a backend. Having a server-side verification layer in addition to biometric authentication would create a more secure design.

6. **The PIN should not really be stored on the device at all.** Even encrypted, there isn't much justification for keeping a PIN and loading it back into memory. A PIN is a verification secret; ideally it should be kept hashed on the server, with the client only using it at the moment of verification.

7. **The wallet screen is unprotected against screenshots.** Screens that display sensitive information like the balance and PIN should set `FLAG_SECURE`. Otherwise screenshots can be taken, and when the app is sent to the background the sensitive data shows up in the recent-apps preview.

8. **The token has no lifecycle.** There is no mechanism for the stored token's expiry or refresh. A token that never expires stays valid forever if it's stolen; there should be a validity period and a refresh flow.

9. **Error cases are not differentiated.** All errors are collapsed into a single `false`. But cases like "locked out due to too many attempts" and "user cancelled" need to be handled separately. The wrong-fingerprint case (`onAuthenticationFailed`) is also not handled at all.

10. **The singleton is not thread-safe and leaks memory.** There is a `@Volatile` annotation but no `synchronized` block, so two separate instances could be created at once. It also holds a `Context` directly — if an Activity context is passed in, it will leak; `applicationContext` should be used instead.

---

## 2. AI-Assisted Review

### 2.1 Tool used
Claude Code (Claude Opus 4.8), run against the file in the repository.

### 2.2 Exact prompt

```
You are reviewing AuthManager.kt, a class that handles authentication token
and wallet PIN storage after biometric login in a fintech Android app (minSdk 24).

Review this code for:
1. Security vulnerabilities (storage, cryptography, key management)
2. Android Keystore and BiometricPrompt best practice violations
3. Architectural/design problems
4. Missing functionality critical for a fintech context

For each issue: reference the exact method/line, explain why it's a problem,
and describe the real-world impact in a financial app.
Categorize by severity: Critical / High / Medium / Low.
```

**Why I wrote the prompt this way:** I gave the model the same framing a senior reviewer would have — the domain (fintech wallet), the platform constraint (minSdk 24), and four explicit lenses (storage/crypto, Keystore/BiometricPrompt, architecture, missing functionality). I asked for line references, real-world impact, and a severity grade so the output would be directly usable in a PR. I deliberately kept it conversational rather than a rigid checklist, so the model would reason about impact instead of pattern-matching.

### 2.3 AI output

#### 🔴 CRITICAL

**1. Credentials stored in plaintext — `prefs` (line 22–23, 25–33)**
Token and wallet PIN are written to a plaintext XML file. `MODE_PRIVATE` only
isolates from *other apps on a non-rooted device* — on a rooted device, via
`adb backup`, or through a device-level compromise, `auth_prefs.xml` is fully
readable.
**Fintech impact:** Direct theft of a user's session token and wallet PIN → full
account takeover and unauthorized money movement.
**Fix:** `EncryptedSharedPreferences` (or DataStore) backed by an Android Keystore key.

**2. Logging raw token and PIN — line 32**
Secrets written to logcat. Logcat is reachable via ADB, captured by crash/analytics
tooling, and persists in bug reports. `Log.d` is **not** stripped from release builds
unless ProGuard rules are added.
**Fintech impact:** Credentials leak into logs, crash reports, and support bundles.
**Fix:** Never log credentials. Remove entirely.

**3. `logout()` does not clear persisted storage — line 81–84**
Only the in-memory fields are nulled. The token and PIN **remain on disk**. After
"logout," anyone can call `loadCredentials()` and recover them.
**Fintech impact:** Logout provides a false sense of security; credentials remain recoverable.
**Fix:** `prefs.edit().clear().apply()` plus invalidate/delete the Keystore key.

**4. Biometric auth is not cryptographically bound — line 46–79**
`authenticate()` is called **without a `CryptoObject`**. The biometric check is a
pure boolean gate — nothing is decrypted by the authentication. An attacker using
instrumentation (or simply calling `completion(true)`) bypasses it entirely.
**Fintech impact:** The "biometric lock" is decorative and bypassable.
**Fix:** Keystore key with `setUserAuthenticationRequired(true)`, wrap a `Cipher` in a
`CryptoObject`, decrypt only inside `onAuthenticationSucceeded`.

#### 🟠 HIGH

**5. `BIOMETRIC_WEAK` used for financial auth — line 72–74**
Class 2 is spoofable on some devices and **cannot be used with a `CryptoObject`**.
**Fix:** `BIOMETRIC_STRONG`.

**6. Missing key invalidation on new biometric enrollment**
`setInvalidatedByBiometricEnrollment(true)` is absent — a newly enrolled biometric
would gain access.

**7. Secrets held as public mutable `String` — line 19–20**
`String` can't be zeroed and lingers in heap dumps; the public `var` lets any caller
read or overwrite them.

#### 🟡 MEDIUM

**8. Broken double-checked locking — line 11–16** — `@Volatile` without `synchronized`
is not thread-safe.
**9. Activity-`Context` leak risk — line 8, 14** — should use `applicationContext`.
**10. Executor leaked per call — line 50** — new thread pool each call, never shut down.
**11. `.commit()` on the calling thread — line 29** — synchronous I/O, ANR risk.

#### 🟢 LOW
- `onAuthenticationFailed()` not overridden.
- No interface/DI — untestable singleton mixing storage + biometric + state.
- `loadCredentials()` mixes query and command (returns Boolean + mutates global state).

#### AI summary table

| # | Issue | Severity |
|---|-------|----------|
| 1 | Plaintext credential storage | Critical |
| 2 | Logging token + PIN | Critical |
| 3 | `logout()` leaves data on disk | Critical |
| 4 | No `CryptoObject` — biometric unbound | Critical |
| 5 | `BIOMETRIC_WEAK` | High |
| 6 | No enrollment invalidation | High |
| 7 | Secrets as public mutable `String` | High |
| 8 | Broken singleton DCL | Medium |
| 9 | Context leak | Medium |
| 10 | Executor leak | Medium |
| 11 | `.commit()` blocking I/O | Medium |

**AI verdict: Request changes.**

---

## 3. Comparison Analysis — Human vs AI

### 3.1 Caught by both
The AI caught the same core security flaws I found in my manual review: plaintext
storage (1), CryptoObject/Keystore binding and `BIOMETRIC_STRONG` (2), the `Log.d`
leak (3), `logout()` cleanup (4), and the thread-safety and context leak in the
singleton (10). On the critical and high-severity implementation bugs, human and AI
largely overlapped.

### 3.2 What the AI caught that I missed
The AI surfaced several implementation-level details that were not in my own list.

- **The secrets are exposed in memory, not just on disk (AI #7).** My item (1) focused on
  *disk* storage (SharedPreferences → Encrypted). The AI additionally pointed out that
  `currentToken` and `walletPin` are `public var` — any caller can read or overwrite them
  — and that, being `String`, they cannot be zeroed and linger in heap dumps. They should
  be private and held as `CharArray`/`ByteArray` that is wiped after use.
- **Executor leak.** Each call to `authenticateWithBiometrics` spins up a new
  `Executors.newSingleThreadExecutor()` that is never shut down — a thread leak over time.
- **`.commit()` blocks the calling thread.** The synchronous SharedPreferences write
  creates an ANR risk if called on the main thread; `.apply()` should be used instead.
- **Key invalidation on new biometric enrollment.** `setInvalidatedByBiometricEnrollment(true)`
  is absent — a newly enrolled fingerprint or face would gain access to the existing key.
- **`loadCredentials()` mixes query and command.** The method returns a `Boolean` while
  also mutating global state as a side effect — a code smell I didn't flag.

These are line-level details where the AI's exhaustive scan adds genuine value on top of
human review.

### 3.3 What the AI missed (and I caught)
The AI output stayed **inside** the file — it reviewed what the code does and how to
fix it. By contrast, the following items from my review did not appear in the AI output
at all:

- **Backend / hybrid verification (5):** Handling sensitive operations entirely on the
  client is an architectural problem in itself. The AI asked "how do I fix this code";
  it didn't ask "should this responsibility live on the client at all."
- **The PIN shouldn't be stored at all (6):** The AI said "encrypt the PIN"; I stepped
  back and asked "why is the PIN being stored in the first place."
- **`FLAG_SECURE` / screenshot protection (7)**
- **Token lifecycle — expiry/refresh (8)**
- **Differentiating error cases (9)**

What these share is that they are all decisions at the **system and threat-model level**
that aren't visible in the file. The AI anchors on the artifact placed in front of it and
rarely asks, on its own, "should this file even exist in this form?"

### 3.4 Where the AI added value
The AI scanned the code line by line, quickly and exhaustively, and was especially good
at surfacing low-level implementation details: thread leaks, synchronous I/O, key
invalidation parameters, and memory exposure — details that are easy to overlook in a
manual pass. Where a human reviewer can miss these under time pressure, the AI is
consistently thorough.

### 3.5 Did the AI say anything incorrect or misleading?
I verified the technical claims in the AI output (especially the `BiometricPrompt` and
Keystore API behaviors) against the current AndroidX documentation and did not find an
outright error. Even so, output like this **should not be accepted without verification**
— in a fintech context, a wrong API assumption is costly.

### 3.6 Conclusion
The split was consistent: the AI was thorough on everything visible inside the file —
storage, crypto, leaks, threading — while the points it missed all lived outside the
file, at the system and threat-model level (where the PIN should live, whether sensitive
operations belong on the client, token lifecycle, screenshot protection). A sharper
prompt could have nudged it toward some of those, but knowing which questions to ask is
itself the engineering judgment, and that part came from me.

> **In short:** the AI is strong at exhaustive, line-level coverage of the code in front
> of it. Stepping back to question whether the design itself belongs on the client —
> and the fintech-specific calls that follow from that — came from the human review.
> Both layers were needed, and they complemented each other rather than overlapping.

---

## 4. Corrected Implementation

The production-ready rewrite lives in [`docs/AuthManager.kt`](./AuthManager.kt). It is a
reference implementation kept outside the Gradle source sets (so it is not part of the app
build and does not pull `androidx.biometric` / `androidx.security:security-crypto` into the
project), mirroring how the original `AuthManager.kt` was supplied for review.

How it maps back to the review findings:

| Review item | Fix in the rewrite |
|-------------|--------------------|
| 1 — plaintext storage | Token encrypted with a Keystore AES/GCM key; ciphertext held in `EncryptedSharedPreferences` |
| 2 — biometric not crypto-bound | `BIOMETRIC_STRONG` + `CryptoObject(cipher)`; the token can only be decrypted after a successful biometric |
| 3 — credential logging | No sensitive value is ever logged |
| 4 — `logout()` leaves data | `logout()` wipes the in-memory copy, the encrypted disk entry, **and** the Keystore key |
| 5 / 6 — client-side PIN | The PIN is **not** persisted; only the session token is stored (documented at the top of the class) |
| 8 — token lifecycle | Key invalidation handled explicitly via `KeyPermanentlyInvalidatedException` → `AuthError.KeyInvalidated` |
| 9 — undifferentiated errors | `sealed interface AuthError` (UserCancelled / LockedOut / BiometricUnavailable / KeyInvalidated / NoSession) |
| 10 — singleton thread-safety / context leak | Plain class behind an `interface`, DI-provided with `applicationContext` — no hand-rolled DCL |

It also folds in the line-level points the AI surfaced: a shared
`ContextCompat.getMainExecutor` instead of a leaked per-call executor, `.apply()` instead of
the blocking `.commit()`, `setInvalidatedByBiometricEnrollment(true)` on the key, and an
`onAuthenticationFailed()` override. The key-auth parameters are split for API 30+ vs. 24–29
so the implementation stays correct on `minSdk 24`.
