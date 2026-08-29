package com.alexey.kozyakov.snake.storage.skins

import com.alexey.kozyakov.R

enum class SnakeSkin(
    val headResId: Int,
    val headXXResId: Int,
    val bodyResId: Int,
) {
    DEFAULT(
        headResId = R.drawable.snake_head_default,
        headXXResId = R.drawable.snake_head_xx_default,
        bodyResId = R.drawable.snake_body_default
    ),
    SLIME(
        headResId = R.drawable.snake_head_slime,
        headXXResId = R.drawable.snake_head_xx_slime,
        bodyResId = R.drawable.snake_body_slime
    ),
    ICE(
        headResId = R.drawable.snake_head_ice,
        headXXResId = R.drawable.snake_head_xx_ice,
        bodyResId = R.drawable.snake_body_ice
    ),
    MAGMA(
        headResId = R.drawable.snake_head_magma,
        headXXResId = R.drawable.snake_head_xx_magma,
        bodyResId = R.drawable.snake_body_magma
    ),
    ANDROID(
        headResId = R.drawable.snake_head_android,
        headXXResId = R.drawable.snake_head_xx_android,
        bodyResId = R.drawable.snake_body_android
    ),
    KING(
        headResId = R.drawable.snake_head_king,
        headXXResId = R.drawable.snake_head_xx_king,
        bodyResId = R.drawable.snake_body_king
    )
}
