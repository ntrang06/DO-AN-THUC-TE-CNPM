from datetime import datetime

from pydantic import BaseModel, ConfigDict, Field


class UserProfileResponse(BaseModel):
    """
    Dữ liệu hồ sơ người dùng trả về từ Backend.
    """

    model_config = ConfigDict(
        populate_by_name=True,
        extra="ignore",
    )

    uid: str

    display_name: str = Field(alias="displayName")
    email: str

    usage_mode: str | None = Field(
        default=None,
        alias="usageMode",
    )

    avatar_url: str | None = Field(
        default=None,
        alias="avatarUrl",
    )

    created_at: datetime | None = Field(
        default=None,
        alias="createdAt",
    )

    updated_at: datetime | None = Field(
        default=None,
        alias="updatedAt",
    )


class UserProfileUpdate(BaseModel):
    """
    Dữ liệu cho phép Android cập nhật hồ sơ.

    Chỉ cho phép cập nhật displayName, usageMode, avatarUrl.
    """

    model_config = ConfigDict(
        populate_by_name=True,
        extra="forbid",
    )

    display_name: str | None = Field(
        default=None,
        alias="displayName",
        min_length=1,
        max_length=100,
    )

    usage_mode: str | None = Field(
        default=None,
        alias="usageMode",
        max_length=50,
    )

    avatar_url: str | None = Field(
        default=None,
        alias="avatarUrl",
        max_length=2048,
    )
