from typing import Any

from fastapi import APIRouter, Depends, HTTPException, status

from app.core.security import get_current_user
from app.schemas.auth import AuthSyncResponse
from app.schemas.user import UserProfileResponse, UserProfileUpdate
from app.services.auth_service import AuthService
from app.services.user_service import UserService


router = APIRouter(
    prefix="/auth",
    tags=["Authentication"],
)


@router.post(
    "/sync",
    response_model=AuthSyncResponse,
    status_code=status.HTTP_200_OK,
    summary="Synchronize Firebase user profile",
)
def sync_user(
    current_user: dict[str, Any] = Depends(get_current_user),
):
    """
    Xác minh danh tính Firebase và đồng bộ hồ sơ Firestore.

    UID được lấy từ Firebase ID Token đã xác minh.
    """

    try:
        profile = AuthService().sync_user(current_user)

        return {
            "user": profile,
        }

    except (ValueError, RuntimeError):
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Không thể đồng bộ hồ sơ người dùng.",
        )


@router.get(
    "/me",
    response_model=UserProfileResponse,
    summary="Get current user profile",
)
def get_my_profile(
    current_user: dict[str, Any] = Depends(get_current_user),
):
    """
    Lấy hồ sơ của người đang đăng nhập.
    """

    uid = current_user["uid"]

    try:
        return UserService().get_profile(uid)

    except LookupError:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Không tìm thấy hồ sơ người dùng.",
        )


@router.patch(
    "/me",
    response_model=UserProfileResponse,
    summary="Update current user profile",
)
def update_my_profile(
    updates: UserProfileUpdate,
    current_user: dict[str, Any] = Depends(get_current_user),
):
    """
    Cập nhật hồ sơ của người đang đăng nhập.

    Chỉ cập nhật các trường được gửi lên.
    """

    uid = current_user["uid"]

    update_data = updates.model_dump(
        by_alias=True,
        exclude_unset=True,
    )

    try:
        return UserService().update_profile(
            uid=uid,
            updates=update_data,
        )

    except ValueError as error:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(error),
        )

    except LookupError:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Không tìm thấy hồ sơ người dùng.",
        )
