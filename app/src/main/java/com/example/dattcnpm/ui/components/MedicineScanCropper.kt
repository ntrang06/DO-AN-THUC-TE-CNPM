package com.example.dattcnpm.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.dattcnpm.data.scan.ScanCropRegion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun MedicineScanCropper(
    source: String?,
    region: ScanCropRegion,
    onRegionChange: (ScanCropRegion) -> Unit,
    modifier: Modifier = Modifier,
    onImageReadyChange: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current.applicationContext
    val currentReadyChange by rememberUpdatedState(onImageReadyChange)

    var bitmap by remember(source) {
        mutableStateOf<Bitmap?>(null)
    }

    var error by remember(source) {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(source) {
        currentReadyChange(false)
        bitmap = null
        error = null

        try {
            require(!source.isNullOrBlank()) {
                "Chưa có ảnh. Vui lòng chụp hoặc chọn ảnh."
            }

            bitmap = loadScanPreviewBitmap(context, source)
            currentReadyChange(true)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (exception: Exception) {
            error = exception.message ?: "Không mở được ảnh."
            currentReadyChange(false)
        }
    }

    val currentRegion by rememberUpdatedState(region)
    val currentChange by rememberUpdatedState(onRegionChange)

    var canvasSize by remember {
        mutableStateOf(IntSize.Zero)
    }

    val touchRadius = with(LocalDensity.current) {
        36.dp.toPx()
    }

    val imageInset = with(LocalDensity.current) {
        16.dp.toPx()
    }

    Box(
        modifier = modifier.background(Color(0xFF142523)),
        contentAlignment = Alignment.Center
    ) {
        val photo = bitmap

        if (photo == null) {
            if (error != null) {
                Text(
                    text = error.orEmpty(),
                    color = Color.White
                )
            } else {
                CircularProgressIndicator(color = Color.White)
            }
        } else {
            val image = remember(photo) {
                photo.asImageBitmap()
            }

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { canvasSize = it }
                    .pointerInput(
                        photo,
                        canvasSize,
                        touchRadius,
                        imageInset
                    ) {
                        var mode = -1
                        var dragging = currentRegion.normalized()

                        detectDragGestures(
                            onDragStart = { position ->
                                val bounds = scanCropImageBounds(
                                    size = canvasSize,
                                    width = photo.width,
                                    height = photo.height,
                                    inset = imageInset
                                )

                                dragging = currentRegion.normalized()
                                val crop = regionRect(bounds, dragging)

                                val corners = listOf(
                                    crop.topLeft,
                                    crop.topRight,
                                    crop.bottomRight,
                                    crop.bottomLeft
                                )

                                val nearest = corners.indices.minByOrNull {
                                    (corners[it] - position).getDistance()
                                } ?: 0

                                mode = when {
                                    (corners[nearest] - position)
                                        .getDistance() <= touchRadius -> nearest

                                    crop.contains(position) -> 4

                                    else -> -1
                                }
                            },
                            onDragEnd = {
                                mode = -1
                            },
                            onDragCancel = {
                                mode = -1
                            },
                            onDrag = { change, amount ->
                                val bounds = scanCropImageBounds(
                                    size = canvasSize,
                                    width = photo.width,
                                    height = photo.height,
                                    inset = imageInset
                                )

                                if (
                                    mode >= 0 &&
                                    bounds.width > 0f &&
                                    bounds.height > 0f
                                ) {
                                    change.consume()

                                    val dx = amount.x / bounds.width
                                    val dy = amount.y / bounds.height

                                    dragging = if (mode == 4) {
                                        dragging.moved(dx, dy)
                                    } else {
                                        dragging.resized(mode, dx, dy)
                                    }

                                    currentChange(dragging)
                                }
                            }
                        )
                    }
            ) {
                val bounds = scanCropImageBounds(
                    size = IntSize(
                        size.width.roundToInt(),
                        size.height.roundToInt()
                    ),
                    width = photo.width,
                    height = photo.height,
                    inset = imageInset
                )

                drawImage(
                    image = image,
                    dstOffset = IntOffset(
                        bounds.left.roundToInt(),
                        bounds.top.roundToInt()
                    ),
                    dstSize = IntSize(
                        bounds.width.roundToInt().coerceAtLeast(1),
                        bounds.height.roundToInt().coerceAtLeast(1)
                    )
                )

                val crop = regionRect(bounds, region.normalized())
                val shade = Color.Black.copy(alpha = 0.6f)

                drawRect(
                    color = shade,
                    topLeft = bounds.topLeft,
                    size = Size(
                        bounds.width,
                        (crop.top - bounds.top).coerceAtLeast(0f)
                    )
                )

                drawRect(
                    color = shade,
                    topLeft = Offset(bounds.left, crop.bottom),
                    size = Size(
                        bounds.width,
                        (bounds.bottom - crop.bottom).coerceAtLeast(0f)
                    )
                )

                drawRect(
                    color = shade,
                    topLeft = Offset(bounds.left, crop.top),
                    size = Size(
                        (crop.left - bounds.left).coerceAtLeast(0f),
                        crop.height
                    )
                )

                drawRect(
                    color = shade,
                    topLeft = Offset(crop.right, crop.top),
                    size = Size(
                        (bounds.right - crop.right).coerceAtLeast(0f),
                        crop.height
                    )
                )

                drawRect(
                    color = Color.White,
                    topLeft = crop.topLeft,
                    size = crop.size,
                    style = Stroke(2.dp.toPx())
                )

                listOf(
                    crop.topLeft,
                    crop.topRight,
                    crop.bottomRight,
                    crop.bottomLeft
                ).forEach { corner ->
                    drawCircle(
                        color = Color(0xFF126B62),
                        radius = 8.dp.toPx(),
                        center = corner
                    )

                    drawCircle(
                        color = Color.White,
                        radius = 8.dp.toPx(),
                        center = corner,
                        style = Stroke(2.dp.toPx())
                    )
                }
            }
        }
    }
}

internal fun scanCropImageBounds(
    size: IntSize,
    width: Int,
    height: Int,
    inset: Float
): Rect {
    if (
        size.width <= 0 ||
        size.height <= 0 ||
        width <= 0 ||
        height <= 0
    ) {
        return Rect.Zero
    }

    val padding = min(
        inset.coerceAtLeast(0f),
        min(size.width, size.height) / 4f
    )

    val scale = min(
        (size.width - padding * 2) / width,
        (size.height - padding * 2) / height
    )

    val w = width * scale
    val h = height * scale
    val x = (size.width - w) / 2f
    val y = (size.height - h) / 2f

    return Rect(x, y, x + w, y + h)
}

private fun regionRect(
    bounds: Rect,
    region: ScanCropRegion
): Rect {
    return Rect(
        left = bounds.left + region.left * bounds.width,
        top = bounds.top + region.top * bounds.height,
        right = bounds.left + region.right * bounds.width,
        bottom = bounds.top + region.bottom * bounds.height
    )
}

private suspend fun loadScanPreviewBitmap(
    context: Context,
    source: String
): Bitmap = withContext(Dispatchers.IO) {
    val uri = Uri.parse(source)

    val input = when (uri.scheme) {
        "content" -> context.contentResolver.openInputStream(uri)

        "file" -> {
            val file = File(requireNotNull(uri.path)).canonicalFile
            val cache = context.cacheDir.canonicalFile

            require(file.path.startsWith(cache.path + File.separator)) {
                "Ảnh chụp không hợp lệ. Vui lòng chụp lại."
            }

            file.inputStream()
        }

        else -> error("Đường dẫn ảnh không hợp lệ.")
    } ?: error("Không mở được ảnh. Vui lòng chọn lại.")

    val bytes = input.use { stream ->
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(8192)

        while (true) {
            val count = stream.read(buffer)
            if (count < 0) break

            require(output.size() + count <= 8 * 1024 * 1024) {
                "Ảnh lớn hơn 8 MB. Vui lòng chọn ảnh nhỏ hơn."
            }

            output.write(buffer, 0, count)
        }

        output.toByteArray()
    }

    val bounds = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }

    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)

    require(bounds.outWidth > 0 && bounds.outHeight > 0) {
        "Không đọc được ảnh. Vui lòng chọn ảnh khác."
    }

    var sample = 1

    while (max(bounds.outWidth, bounds.outHeight) / sample > 2048) {
        sample *= 2
    }

    val bitmap = BitmapFactory.decodeByteArray(
        bytes,
        0,
        bytes.size,
        BitmapFactory.Options().apply {
            inSampleSize = sample
        }
    ) ?: error("Không đọc được ảnh.")

    val orientation = runCatching {
        ByteArrayInputStream(bytes).use {
            ExifInterface(it).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        }
    }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

    val matrix = Matrix()

    when (orientation) {
        2 -> matrix.setScale(-1f, 1f)
        3 -> matrix.setRotate(180f)
        4 -> matrix.setScale(1f, -1f)
        5 -> {
            matrix.setRotate(90f)
            matrix.postScale(-1f, 1f)
        }
        6 -> matrix.setRotate(90f)
        7 -> {
            matrix.setRotate(270f)
            matrix.postScale(-1f, 1f)
        }
        8 -> matrix.setRotate(270f)
    }

    if (matrix.isIdentity) {
        bitmap
    } else {
        val rotated = Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )

        if (rotated !== bitmap) {
            bitmap.recycle()
        }

        rotated
    }
}