from typing import Any

from app.repositories.user_repository import UserRepository


class UserService:
    """
    Xử lý nghiệp vụ hồ sơ người dùng.
    """

    def __init__(self, repository: UserRepository | None = None):
        self.repository = repository or UserRepository()

    def get_profile(self, uid: str) -> dict[str, Any]:
        """
        Lấy hồ sơ theo UID đã được xác minh.
        """

        profile = self.repository.get_by_uid(uid)

        if profile is None:
            raise LookupError("Không tìm thấy hồ sơ người dùng.")

        return profile

    def update_profile(
        self,
        uid: str,
        updates: dict[str, Any],
    ) -> dict[str, Any]:
        """
        Cập nhật các trường hồ sơ được phép.
        """

        if not updates:
            raise ValueError("Không có dữ liệu cần cập nhật.")

        return self.repository.update_profile(uid, updates)
