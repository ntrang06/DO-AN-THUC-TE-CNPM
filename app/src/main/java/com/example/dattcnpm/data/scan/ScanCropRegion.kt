package com.example.dattcnpm.data.scan

data class ScanCropRegion(
    val left: Float = 0f,
    val top: Float = 0f,
    val right: Float = 1f,
    val bottom: Float = 1f
) {
    fun normalized(): ScanCropRegion {
        if (listOf(left, top, right, bottom).any { !it.isFinite() }) {
            return Full
        }

        val l = left.coerceIn(0f, 1f - MIN_SIZE)
        val t = top.coerceIn(0f, 1f - MIN_SIZE)

        return ScanCropRegion(
            left = l,
            top = t,
            right = right.coerceIn(l + MIN_SIZE, 1f),
            bottom = bottom.coerceIn(t + MIN_SIZE, 1f)
        )
    }

    fun moved(dx: Float, dy: Float): ScanCropRegion {
        val region = normalized()
        val width = region.right - region.left
        val height = region.bottom - region.top

        val l = (region.left + dx).coerceIn(
            0f,
            (1f - width).coerceAtLeast(0f)
        )

        val t = (region.top + dy).coerceIn(
            0f,
            (1f - height).coerceAtLeast(0f)
        )

        return ScanCropRegion(
            left = l,
            top = t,
            right = l + width,
            bottom = t + height
        ).normalized()
    }

    // 0: trên trái, 1: trên phải, 2: dưới phải, 3: dưới trái.
    fun resized(
        corner: Int,
        dx: Float,
        dy: Float
    ): ScanCropRegion {
        val region = normalized()

        return when (corner) {
            0 -> region.copy(
                left = (region.left + dx)
                    .coerceIn(0f, region.right - MIN_SIZE),
                top = (region.top + dy)
                    .coerceIn(0f, region.bottom - MIN_SIZE)
            )

            1 -> region.copy(
                right = (region.right + dx)
                    .coerceIn(region.left + MIN_SIZE, 1f),
                top = (region.top + dy)
                    .coerceIn(0f, region.bottom - MIN_SIZE)
            )

            2 -> region.copy(
                right = (region.right + dx)
                    .coerceIn(region.left + MIN_SIZE, 1f),
                bottom = (region.bottom + dy)
                    .coerceIn(region.top + MIN_SIZE, 1f)
            )

            3 -> region.copy(
                left = (region.left + dx)
                    .coerceIn(0f, region.right - MIN_SIZE),
                bottom = (region.bottom + dy)
                    .coerceIn(region.top + MIN_SIZE, 1f)
            )

            else -> region
        }.normalized()
    }

    companion object {
        val Full = ScanCropRegion()
        private const val MIN_SIZE = 0.08f
    }
}