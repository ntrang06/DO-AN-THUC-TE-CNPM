from pydantic import BaseModel, ConfigDict

from app.schemas.user import UserProfileResponse


class AuthSyncResponse(BaseModel):
    """
    Phản hồi sau khi Backend xác minh Firebase ID Token
    và đồng bộ hồ sơ người dùng.
    """

    model_config = ConfigDict(
        populate_by_name=True,
    )

    user: UserProfileResponse
