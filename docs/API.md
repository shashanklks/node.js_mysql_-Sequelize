# API reference

Base URL: `http://localhost:3002` (see `PORT` in `.env`).

Every response uses the same envelope:

```json
{ "statusCode": 200, "success": true, "message": "Success", "data": { } }
```

Failures carry `success: false` and a human readable `message` that the app
shows directly, so error text lives on the server.

All `/api/*` routes except the two auth routes need a bearer token:

```
Authorization: Bearer <token>
```

## Auth

### POST /api/auth/send-otp

```json
{ "phone": "9876543210" }
```

Returns `{ "phone", "expiresInSeconds", "otp" }`. The `otp` field is present
only when `NODE_ENV` is not `production`. Requesting a new code invalidates any
previous one for that number.

### POST /api/auth/verify-otp

```json
{ "phone": "9876543210", "otp": "418302" }
```

Returns `{ "token", "isNewUser", "user" }`. A first-time number is registered on
the spot, which is why there is no separate signup call. Five wrong attempts
burn the code.

## Profile

| Method | Path | Notes |
| --- | --- | --- |
| GET | `/api/profile` | Current user |
| PUT | `/api/profile` | `{ name?, businessName?, language? }` |

## Parties

A party is a customer or a supplier. Balances are computed from the ledger, not
stored.

### GET /api/parties

Query: `type=CUSTOMER|SUPPLIER`, `search=`, `sort=recent|name|highest|lowest`.

```json
{
  "parties": [
    {
      "id": 4,
      "name": "Suresh Traders",
      "phone": "9812345678",
      "type": "CUSTOMER",
      "gave": 1250.75,
      "got": 400,
      "balance": 850.75,
      "lastEntryDate": "2026-08-11"
    }
  ],
  "summary": { "youWillGet": 850.75, "youWillGive": 0 },
  "count": 1
}
```

| Method | Path | Notes |
| --- | --- | --- |
| POST | `/api/parties` | `{ name, phone?, address?, type }`, 409 on a duplicate name in the same book |
| GET | `/api/parties/:id` | One party with its balance |
| PUT | `/api/parties/:id` | Any subset of the create fields |
| DELETE | `/api/parties/:id` | Removes the party and its entries |

## Entries

### GET /api/parties/:partyId/entries

Oldest first, each row carrying the balance after it:

```json
{
  "party": { "id": 4, "name": "Suresh Traders", "type": "CUSTOMER" },
  "entries": [
    { "id": 9, "amount": 1000, "type": "GAVE", "note": "10 bags", "entryDate": "2026-08-01", "runningBalance": 1000 },
    { "id": 12, "amount": 400, "type": "GOT", "note": null, "entryDate": "2026-08-11", "runningBalance": 600 }
  ],
  "summary": { "gave": 1000, "got": 400, "balance": 600 }
}
```

| Method | Path | Notes |
| --- | --- | --- |
| POST | `/api/parties/:partyId/entries` | `{ amount, type: "GAVE"\|"GOT", note?, entryDate? }`, date defaults to today |
| PUT | `/api/entries/:id` | Any subset of those fields |
| DELETE | `/api/entries/:id` | |

Amounts must be positive; direction is carried by `type`, never by the sign.

## Reports

### GET /api/reports/summary

Totals for the whole book, split by tab:

```json
{
  "customers": { "youWillGet": 850.75, "youWillGive": 0, "count": 3 },
  "suppliers": { "youWillGet": 0, "youWillGive": 3000, "count": 1 },
  "overall": { "youWillGet": 850.75, "youWillGive": 3000, "count": 4 }
}
```

### GET /api/reports/transactions

Query: `from=YYYY-MM-DD`, `to=YYYY-MM-DD`, `type=GAVE|GOT`,
`partyType=CUSTOMER|SUPPLIER`, `limit` (max 500, default 200).

Newest first, each row naming its party, with `summary.gave`, `summary.got` and
`summary.net` for the period.

## Legacy endpoints

The original email and password routes still work: `POST /register`,
`POST /login`, `GET /user-details`. They share the `User` table and issue the
same tokens as the OTP flow.
