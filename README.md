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

### POST /api/v1/schedule

Creates schedule with provided data.

**Auth required**: YES

**Permissions required**: ROLE_ADMIN

**Request body**:

| Name            | Type    | Constraints                            |
|-----------------|---------|----------------------------------------|
| `type`          | Integer | Must follow the email format.          |
| `plan_polsl_id` | Integer | Must be positive or zero and not null. |
| `semester`      | Integer | Must be positive and not null.         |
| `name`          | String  | Must not be blank.                     |
| `group_number`  | Integer | Must be positive and not null.         |


**Success response**:

Code: `201`

```json
{
  "id": 1,
  "semester": 1,
  "name": "schedule-name",
  "group_number": 1
}
```

**Error response**:

(1)
If provided `name` is unavailable:

Code: `409`

---

### DELETE /api/v1/schedule/{id}

Deletes schedule with provided `id`.

**Auth required**: YES

**Permissions required**: ROLE_ADMIN

**Path variables**:

| Name | Type | Required |
|------|------|----------|
| `id` | Long | True     |

**Success response**:

Code: `200`

```json
{
  "message": "success"
}
```

**Error response**:

(1)
If schedule with provided `id` was not found:

Code: `404`

---

### GET /api/v1/schedule

Gets all courses and sorts them according to the following rule: `semesters` ASC, then `group_number` ASC

**Auth required**: NO

**Permissions required**: NONE

**Success response**:

Code: `200`

```json
{
  "semesters": {
    "1": [
      {
        "id": 1,
        "name": "Inf 1/2",
        "group_number": 1
      },
      {
        "id": 2,
        "name": "Inf 2/4",
        "group_number": 2
      }
    ],
    "3": [
      {
        "id": 3,
        "name": "Inf 1/1",
        "group_number": 1
      }
    ]
  }
}
```

---

### GET /api/v1/schedule/{1}

Gets schedule with provided `id` and all courses related to it. Courses are sorted according to the following rule: `day_of_week` ASC, then `starts_at` ASC

**Auth required**: NO

**Permissions required**: NONE

**Path variables**:

| Name | Type | Required |
|------|------|----------|
| `id` | Long | True     |

**Success response**:

Code: `200`

```json
{
  "id": 1,
  "semester": 1,
  "name": "schedule-name",
  "group_number": 1,
  "courses": {
    "MONDAY": [
      {
        "description": "Course 1",
        "starts_at": "08:30:00",
        "ends_at": "10:00:00",
        "weeks": "EVERY"
      },
      {
        "description": "Course 2",
        "starts_at": "10:15:00",
        "ends_at": "11:45:00",
        "weeks": "EVERY"
      }
    ],
    "TUESDAY": [
      {
        "description": "Course 3",
        "starts_at": "08:30:00",
        "ends_at": "10:00:00",
        "weeks": "EVEN"
      }
    ],
    "FRIDAY": [
      {
        "description": "Course 4",
        "starts_at": "15:30:00",
        "ends_at": "17:00:00",
        "weeks": "EVERY"
      }
    ]
  }
}
```

**Error response**:

(1)
If schedule with provided `id` was not found:

Code: `404`

---

### POST /api/v1/discord-webhook

Creates discord webhook with provided data. After creating discord webhook then welcome is send via created webhook.

**Auth required**: YES

**Permissions required**: NONE

**Request body**:

| Name            | Type         | Constraints                                 |
|-----------------|--------------|---------------------------------------------|
| `url`           | String       | Must follow the discord webhook url format. |
| `schedules_ids` | Set of longs | Must not be empty                           |

**Success response**:

Code: `201`

```json
{
  "id": 1,
  "url": "url",
  "schedules": [
    {
      "id": 1,
      "semester": 1,
      "name": "schedule-1",
      "group_number": 1
    },
    {
      "id": 2,
      "semester": 1,
      "name": "schedule-2",
      "group_number": 2
    }
  ],
  "added_by": {
    "email": "email@email.pl"
  }
}
```

**Error response**:

(1)
If provided data is not valid

Code: `400`

(2)
If `url` is unavailable

Code: `409`

---

### DELETE /api/v1/discord-webhook/{id}

Deletes webhook with provided `id`.

**Auth required**: NO

**Permissions required**: NONE

**Path variables**:

| Name | Type | Required |
|------|------|----------|
| `id` | Long | True     |

**Success response**:

Code: `200`

```json
{
  "message": "success"
}
```

**Error response**:

(1)
If webhook with provided `id` was not found

Code: `404`

---