import os
from pathlib import Path

BACKEND_DIR = Path(__file__).resolve().parents[2]

FIREBASE_CREDENTIALS_PATH = Path(
    os.getenv(
        "FIREBASE_CREDENTIALS_PATH",
        str(BACKEND_DIR / "secrets" / "firebase-service-account.json")
    )
)

FIREBASE_PROJECT_ID = os.getenv(
    "FIREBASE_PROJECT_ID",
    "dattcnpm-f9925"
)

API_V1_PREFIX = "/api/v1"
APP_NAME = "MMRA Backend"
