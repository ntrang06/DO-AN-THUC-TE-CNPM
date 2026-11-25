from typing import Any

from firebase_admin import firestore

from app.core.firebase import get_firestore_client


class UserRepository:
    """
    Repository thao tác với hồ sơ người dùng trong Firestore.

    Collection: users
    Document: users/{uid}
    """

    COLLECTION_NAME = "users"

    ALLOWED_PROFILE_FIELDS = {
        "displayName",
        "usageMode",
        "avatarUrl",
    }

    def __init__(self):
        self.db = get_firestore_client()
        self.collection = self.db.collection(self.COLLECTION_NAME)

    def _get_user_document(self, uid: str):
        """
        Lấy document reference của người dùng.
        """

        if not uid or not uid.strip() or "/" in uid:
            raise ValueError("UID không hợp lệ.")

        return self.collection.document(uid)

    def get_by_uid(self, uid: str) -> dict[str, Any] | None:
        """
        Lấy hồ sơ người dùng theo UID.

        Trả về None nếu người dùng chưa có hồ sơ.
        """

        document = self._get_user_document(uid).get()

        if not document.exists:
            return None

        return {
            "uid": document.id,
            **(document.to_dict() or {}),
        }

    def create_if_not_exists(
        self,
        uid: str,
        display_name: str,
        email: str,
    ) -> dict[str, Any]:
        """
        Tạo hồ sơ nếu chưa tồn tại.

        Nếu document đã tồn tại thì giữ nguyên toàn bộ dữ liệu.
        """

        document_ref = self._get_user_document(uid)
        transaction = self.db.transaction()

        @firestore.transactional
        def create_profile(transaction):
            snapshot = document_ref.get(transaction=transaction)

            if not snapshot.exists:
                transaction.set(
                    document_ref,
                    {
                        "displayName": display_name,
                        "email": email,
                        "usageMode": None,
                        "avatarUrl": None,
                        "createdAt": firestore.SERVER_TIMESTAMP,
                        "updatedAt": firestore.SERVER_TIMESTAMP,
                    },
                )

        create_profile(transaction)

        profile = self.get_by_uid(uid)

        if profile is None:
            raise RuntimeError(
                "Không thể tạo hoặc lấy hồ sơ người dùng."
            )

        return profile

    def update_profile(
        self,
        uid: str,
        updates: dict[str, Any],
    ) -> dict[str, Any]:
        """
        Cập nhật các trường hồ sơ được cho phép.

        Không thay đổi createdAt hoặc các trường khác.
        Không tác động đến subcollection devices.
        """

        document_ref = self._get_user_document(uid)

        if not updates:
            raise ValueError("Không có dữ liệu cần cập nhật.")

        invalid_fields = set(updates.keys()) - self.ALLOWED_PROFILE_FIELDS

        if invalid_fields:
            raise ValueError(
                f"Các trường không được phép cập nhật: "
                f"{', '.join(sorted(invalid_fields))}"
            )

        snapshot = document_ref.get()

        if not snapshot.exists:
            raise LookupError("Không tìm thấy hồ sơ người dùng.")

        update_data = {
            **updates,
            "updatedAt": firestore.SERVER_TIMESTAMP,
        }

        document_ref.update(update_data)

        profile = self.get_by_uid(uid)

        if profile is None:
            raise RuntimeError(
                "Không thể lấy hồ sơ sau khi cập nhật."
            )

        return profile
