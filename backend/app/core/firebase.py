import firebase_admin

from firebase_admin import credentials, firestore

from app.core.config import (
    FIREBASE_CREDENTIALS_PATH,
    FIREBASE_PROJECT_ID,
)


def initialize_firebase():
    """
    Khởi tạo Firebase Admin SDK.
    Nếu Firebase đã được khởi tạo thì sử dụng lại instance hiện tại.
    """

    try:
        return firebase_admin.get_app()
    except ValueError:
        pass

    if not FIREBASE_CREDENTIALS_PATH.exists():
        raise FileNotFoundError(
            f"Không tìm thấy Firebase credentials: "
            f"{FIREBASE_CREDENTIALS_PATH}"
        )

    credential = credentials.Certificate(
        str(FIREBASE_CREDENTIALS_PATH)
    )

    return firebase_admin.initialize_app(
        credential,
        {
            "projectId": FIREBASE_PROJECT_ID
        }
    )


def get_firestore_client():
    """
    Trả về Firestore client của Firebase project.
    """

    app = initialize_firebase()

    return firestore.client(app)
