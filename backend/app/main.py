from contextlib import asynccontextmanager

from fastapi import FastAPI

from app.api.v1.router import api_router
from app.core.config import API_V1_PREFIX, APP_NAME
from app.core.firebase import initialize_firebase


@asynccontextmanager
async def lifespan(app: FastAPI):
    initialize_firebase()
    yield


app = FastAPI(
    title=APP_NAME,
    description="MMRA Backend API",
    version="1.0.0",
    lifespan=lifespan,
)

app.include_router(
    api_router,
    prefix=API_V1_PREFIX,
)


@app.get("/", tags=["Health"])
def health_check():
    return {
        "message": "MMRA Backend is running"
    }


@app.get("/health", tags=["Health"])
def health():
    return {
        "status": "ok"
    }
