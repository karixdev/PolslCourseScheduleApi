# PolslCourseScheduleApi

## 1. Available endpoints

### POST /api/v1/auth/register

Creates new, disabled user based on provided credentials and sends an email with email verification token.

**Auth required**: NO

**Permissions required**: NONE

**Request body**:

| Name       | Type   | Constraints                                           |
|------------|--------|-------------------------------------------------------|
| `email`    | String | Must follow the email format.                         |
| `password` | String | Length must be at least 8 and at most 255 characters. |

**Success response**:

Code: `201`

```json
{
  "message": "success"
}
```

**Error response**:

(1)
If provided request body is invalid

Code: `400`

(2)
If email is already taken

Code: `409`

---

### POST /api/v1/auth/sign-in

Signs in enabled user. Response includes `JWT` and user details such as: `email`, `user_role`, `is_enabled`.

**Auth required**: NO

**Permissions required**: NONE

**Request body**:

| Name       | Type   | Constraints                                           |
|------------|--------|-------------------------------------------------------|
| `email`    | String | Must follow the email format.                         |
| `password` | String | Length must be at least 8 and at most 255 characters. |

**Success response**:

Code: `200`

```json
{
  "access_token": "...",
  "user": {
    "email": "email@email.com",
    "user_role": "ROLE_USER or ROLE_ADMIN",
    "is_enabled": "true"
  }
}
```

**Error response**:

(1)
If provided request body is invalid

Code: `400`

(2)
If user with provided credentials could not be found or could not be authenticated

Code: `401`

---

### POST /api/v1/email-verification/{token}

Verifies token sent to user after registration (or after requesting new token). If provided token in path is
valid then enables user owning the token.

**Auth required**: NO

**Permissions required**: NONE

**Path variables**:

| Name    | Type   | Required |
|---------|--------|----------|
| `token` | String | True     |

**Success response**:

Code: `200`

```json
{
  "message": "success"
}
```

**Error response**:

(1)
If `token` was not found

Code: `404`

(2)
If user is already enabled or `token` has expired:

Code: `400`

---

### POST /api/v1/email-verification/resend

Resends an email with email verification token. There is a limit which states how many tokens can user request per hour,
you can specify it in `application.yaml` under `email-verification.max-number-of-mails-per-hour` (the default value is
5).

**Auth required**: NO

**Permissions required**: NONE

**Request body**:

| Name    | Type   | Constraints                   |
|---------|--------|-------------------------------|
| `email` | String | Must follow the email format. |

**Success response**:

Code: `200`

```json
{
  "message": "success"
}
```

**Error response**:

(1)
If user with provided `email` was not found

Code: `404`

(2)
If user is already enabled or requested too many tokens in one hour:

Code: `400`

---