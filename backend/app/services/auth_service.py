from typing import Any

from app.repositories.user_repository import UserRepository


class AuthService:
    """
    Xử lý đồng bộ danh tính Firebase Authentication
    với hồ sơ người dùng trong Cloud Firestore.
    """

    def __init__(self, repository: UserRepository | None = None):
        self.repository = repository or UserRepository()

    def sync_user(self, decoded_token: dict[str, Any]) -> dict[str, Any]:
        """
        Tìm hồ sơ theo UID lấy từ Firebase ID Token đã xác minh.

        Nếu chưa có hồ sơ thì tạo mới.
        Nếu đã có thì giữ nguyên dữ liệu hiện tại.
        """

        uid = decoded_token.get("uid")

        if not isinstance(uid, str) or not uid.strip():
            raise ValueError("Firebase ID Token không chứa UID hợp lệ.")

        email = decoded_token.get("email") or ""

        if not isinstance(email, str):
            email = ""

        profile = self.repository.get_by_uid(uid)

        if profile is not None:
            return profile

        display_name = decoded_token.get("name")

        if not isinstance(display_name, str) or not display_name.strip():
            display_name = email.split("@", 1)[0] if email else "User"

        return self.repository.create_if_not_exists(
            uid=uid,
            display_name=display_name,
            email=email,
        )
