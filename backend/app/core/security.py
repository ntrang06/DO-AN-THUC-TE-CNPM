from typing import Any

import firebase_admin
from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from firebase_admin import auth
from firebase_admin.exceptions import FirebaseError

from app.core.firebase import initialize_firebase


bearer_scheme = HTTPBearer(
    auto_error=False,
    scheme_name="FirebaseBearerAuth",
)


def verify_firebase_token(token: str) -> dict[str, Any]:
    """
    Xác minh Firebase ID Token bằng Firebase Admin SDK.

    Trả về decoded token nếu hợp lệ.
    """

    if not token or not token.strip():
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Firebase ID Token không được để trống.",
            headers={"WWW-Authenticate": "Bearer"},
        )

    try:
        app = initialize_firebase()

        decoded_token = auth.verify_id_token(
            token,
            app=app,
            check_revoked=True,
        )

        return decoded_token

    except auth.RevokedIdTokenError:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Firebase ID Token đã bị thu hồi.",
            headers={"WWW-Authenticate": "Bearer"},
        )

    except auth.ExpiredIdTokenError:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Firebase ID Token đã hết hạn.",
            headers={"WWW-Authenticate": "Bearer"},
        )

    except auth.InvalidIdTokenError:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Firebase ID Token không hợp lệ.",
            headers={"WWW-Authenticate": "Bearer"},
        )

    except FirebaseError:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Không thể xác minh Firebase ID Token lúc này.",
        )

    except (ValueError, KeyError):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Không thể xác minh danh tính người dùng.",
            headers={"WWW-Authenticate": "Bearer"},
        )


def get_current_user(
    credentials: HTTPAuthorizationCredentials | None = Depends(
        bearer_scheme
    ),
) -> dict[str, Any]:
    """
    Dependency dùng trong các API cần xác thực.

    Lấy Firebase ID Token từ Authorization: Bearer.
    UID chỉ được lấy từ token đã xác minh.
    """

    if credentials is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Vui lòng cung cấp Firebase ID Token.",
            headers={"WWW-Authenticate": "Bearer"},
        )

    if credentials.scheme.lower() != "bearer":
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Phương thức xác thực không được hỗ trợ.",
            headers={"WWW-Authenticate": "Bearer"},
        )

    decoded_token = verify_firebase_token(credentials.credentials)

    uid = decoded_token.get("uid")

    if not uid:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Token không chứa UID hợp lệ.",
            headers={"WWW-Authenticate": "Bearer"},
        )

    return decoded_token
