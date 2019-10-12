# Authentication

## OAuth 2 / OpenID: Connect

missing multiplatform libraries

1. User clicks Login Button
  `GET /api/login/provider` -> `state` & `redirect_url`
2. Server generates `state` & `redirect_url`for user
 remembers `state` & `session`
3. User opens external page of authorization provider
```
https://authorization-server.com/auth?response_type=code&client_id=CLIENT_ID&redirect_uri=REDIRECT_URI&scope=photos&state=1234zyx
```
4. authorization provider POSTS to `redirect_url`
5. Server obtains user data and identifies, authenticates `session`

## Custom Account System

1. User clicks `Login` or `Register`
1.1. User gets redirected to website for registration
1.2 User enters credentials in fields for login
2. Client receives session token
3. User joins game

## Automatic Custom Account System

1. Temporary "session" Accounts are created automatically on join
2. users can then save their accounts and set password

enter jsut the username on join, 
password field will popup when account requires authentication
