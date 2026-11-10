# Pack Bank on Cloudflare Workers (future option)

**Status: not built.** This is the upgrade path from today's public static file
([pack-bank.md](pack-bank.md)) to downloads that only paying users can get. Nothing in the app
depends on it yet.

## Why

Today `packs.json` is public: anyone with the URL can read the premium prompts. That's an accepted
trade-off for now. If it stops being acceptable, the server has to check the purchase before
handing out premium content. A static host can't do that, so it needs a small piece of code. A
Cloudflare Worker can run that code on the free plan with no credit card.

## How it would work

```
App ──POST /v1/packs { purchaseToken }──▶ Worker
                                           │ 1. token seen recently? (KV cache) ──yes──▶ step 4
                                           │ 2. call Google Play Developer API:
                                           │    purchases.products.get(package, "remove_ads", token)
                                           │ 3. purchaseState == 0 (purchased) and not refunded?
                                           │    no ──▶ 403
                                           │ 4. return packs.json (from KV / R2 / bundled in the Worker)
App ◀────────── 200 packs.json / 403 ──────┘
```

- **Getting the token.** `BillingManager` already holds the owned `Purchase`. Expose its
  `purchaseToken` (e.g. `PremiumBilling.premiumPurchaseToken: StateFlow<String?>`).
- **Free packs stay public.** If free content ever moves to the bank too, split it into a public
  `free.json` (any static host) and the gated premium file.
- **Client change** is limited to `PackBankClient`: POST the token instead of a plain GET, and
  treat `403` as "not entitled" (clear premium content, as a refund already does). Cache, parser,
  repository and the UI states stay as they are.

## Setting it up

1. **Google Cloud:** create a project, enable the **Google Play Android Developer API**, and create
   a **service account** with a JSON key.
2. **Play Console:** Users and permissions → invite the service account's email → grant
   **View financial data, orders and cancellation survey responses** for PartyPuzl. It can take up
   to 24 hours to take effect.
3. **Cloudflare:** `npm create cloudflare@latest partypuzl-packs-worker`, then:
   - `wrangler secret put GOOGLE_SERVICE_ACCOUNT_JSON` (paste the key). Never commit it.
   - `wrangler kv namespace create VERIFIED_TOKENS` for the token cache, and either a second KV
     key or an R2 bucket for `packs.json`.
4. **Worker code** (outline):
   - Sign a JWT with the service-account key (Web Crypto `RSASSA-PKCS1-v1_5`, scope
     `https://www.googleapis.com/auth/androidpublisher`) and exchange it at
     `https://oauth2.googleapis.com/token` for an access token. Cache that token for ~50 minutes.
   - `GET https://androidpublisher.googleapis.com/androidpublisher/v3/applications/com.restrusher.partypuzl/purchases/products/remove_ads/tokens/{token}`.
   - Accept when `purchaseState == 0`. Cache the verdict in KV with a TTL (e.g. 24 h) so repeat
     launches don't hit Google.
   - Respond with `packs.json` and an `ETag`, and honour `If-None-Match`.
5. **App:** point `PACK_BANK_URL` at the Worker route and ship the client change above.

## Free-plan limits (check current numbers before relying on them)

- Workers: about 100,000 requests/day and 10 ms CPU per request. JWT signing fits once tokens are
  cached; verification is mostly waiting on network, which doesn't count as CPU.
- KV: about 100,000 reads/day and 1,000 writes/day. One write per newly verified token is well
  within that at this app's scale.
- Play Developer API: a default daily quota far above what this app needs.

## Store and privacy implications

- The purchase token leaves the device and goes to your server. In Play Console's **Data safety**
  form, declare **Purchase history** as *collected* (not shared; used for app functionality).
- The privacy policy (`partypuzl-legal`) must say that purchase tokens are sent to verify premium
  access, and name Cloudflare as a processor.

## Refunds and revocation

The Worker sees refunds on its next verification (after the KV TTL expires). For immediate
revocation, add **Real-time developer notifications**: Play publishes to a Pub/Sub topic, and a
push subscription to a second Worker route deletes the token from KV.

## What it still can't stop

A patched APK can still skip the local `isAdFree` check. But it can't get the premium content
without a real purchase token, and a token from one purchase reused across many devices is
visible in the Worker logs, where it can be rate-limited.
