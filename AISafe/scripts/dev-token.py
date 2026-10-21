#!/usr/bin/env python3
"""
DEVELOPMENT ONLY - creates a JWT signed with the dev secret, so a service can be tested
before auth-service exists. auth-service issues the real tokens (POST /auth/login).

    python dev-token.py --user ana --roles BACKOFFICE,ATCC
    python dev-token.py --roles ADMIN --minutes 120

Uses only the Python standard library. The secret must match aisafe.jwt.secret
(environment variable AISAFE_JWT_SECRET, Base64).
"""
import argparse
import base64
import hashlib
import hmac
import json
import os
import time
import uuid

DEV_SECRET = "ZGV2LW9ubHktYWlzYWZlLWp3dC1zZWNyZXQtY2hhbmdlLW1lLTIwMjYh"


def b64url(data: bytes) -> str:
    return base64.urlsafe_b64encode(data).rstrip(b"=").decode()


def main() -> None:
    parser = argparse.ArgumentParser(description="Create a development JWT for AISafe services")
    parser.add_argument("--user", default="dev-user")
    parser.add_argument("--roles", default="ADMIN",
                        help="comma separated: ADMIN,ATCC,BACKOFFICE,MAINTENANCE_TECHNICIAN,MAINTENANCE_SUPERVISOR")
    parser.add_argument("--minutes", type=int, default=60)
    args = parser.parse_args()

    key = base64.b64decode(os.environ.get("AISAFE_JWT_SECRET", DEV_SECRET))
    now = int(time.time())
    header = {"alg": "HS256", "typ": "JWT"}
    payload = {
        "sub": str(uuid.uuid5(uuid.NAMESPACE_DNS, args.user)),
        "username": args.user,
        "roles": [r.strip().upper() for r in args.roles.split(",") if r.strip()],
        "iat": now,
        "exp": now + args.minutes * 60,
    }
    signing_input = b64url(json.dumps(header, separators=(",", ":")).encode()) + "." + \
        b64url(json.dumps(payload, separators=(",", ":")).encode())
    signature = hmac.new(key, signing_input.encode(), hashlib.sha256).digest()
    print(signing_input + "." + b64url(signature))


if __name__ == "__main__":
    main()
