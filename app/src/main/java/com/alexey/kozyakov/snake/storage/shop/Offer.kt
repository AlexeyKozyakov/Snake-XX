package com.alexey.kozyakov.snake.storage.shop

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.alexey.kozyakov.R
import com.alexey.kozyakov.snake.storage.boosters.SnakeBooster
import com.alexey.kozyakov.snake.storage.skins.SnakeSkin
import com.alexey.kozyakov.snake.storage.upgrade.SnakeUpgrade

enum class OfferType(@StringRes val groupNameResId: Int) {
    SKIN(groupNameResId = R.string.offer_group_skins),
    UPGRADE(groupNameResId = R.string.offer_group_upgrades),
    BOOSTER(groupNameResId = R.string.offer_group_boosters)
}

enum class Offer(
    val type: OfferType,
    val price: Int,
    val productId: Int,
    @DrawableRes val iconResId: Int,
    @StringRes val nameResId: Int,
    @StringRes val descriptionResId: Int?
) {
    UPGRADE_COINS_FOR_APPLES(
        type = OfferType.UPGRADE,
        price = 25,
        productId = SnakeUpgrade.COINS_FOR_GOLDEN_APPLES.ordinal,
        iconResId = R.drawable.upgrade_coins_for_apples,
        nameResId = R.string.upgrade_name_coins_for_apples,
        descriptionResId = R.string.upgrade_description_coins_for_apples
    ),
    UPGRADE_COINS_FOR_LEVELS(
        type = OfferType.UPGRADE,
        price = 150,
        productId = SnakeUpgrade.COINS_FOR_LEVELS.ordinal,
        iconResId = R.drawable.upgrade_coins_for_levels,
        nameResId = R.string.upgrade_name_coins_for_levels,
        descriptionResId = R.string.upgrade_description_coins_for_levels
    ),
    SKIN_DEFAULT(
        type = OfferType.SKIN,
        price = 0,
        productId = SnakeSkin.DEFAULT.ordinal,
        iconResId = SnakeSkin.DEFAULT.headResId,
        nameResId = R.string.skin_default,
        descriptionResId = null,
    ),
    SKIN_SLIME(
        type = OfferType.SKIN,
        price = 50,
        productId = SnakeSkin.SLIME.ordinal,
        iconResId = SnakeSkin.SLIME.headResId,
        nameResId = R.string.skin_slime,
        descriptionResId = null,
    ),
    SKIN_ICE(
        type = OfferType.SKIN,
        price = 100,
        productId = SnakeSkin.ICE.ordinal,
        iconResId = SnakeSkin.ICE.headResId,
        nameResId = R.string.skin_ice,
        descriptionResId = null
    ),
    SKIN_MAGMA(
        type = OfferType.SKIN,
        price = 200,
        productId = SnakeSkin.MAGMA.ordinal,
        iconResId = SnakeSkin.MAGMA.headResId,
        nameResId = R.string.skin_magma,
        descriptionResId = null
    ),
    SKIN_ANDROID(
        type = OfferType.SKIN,
        price = 500,
        productId = SnakeSkin.ANDROID.ordinal,
        iconResId = SnakeSkin.ANDROID.headResId,
        nameResId = R.string.skin_android,
        descriptionResId = null
    ),
    SKIN_KING(
        type = OfferType.SKIN,
        price = 1000,
        productId = SnakeSkin.KING.ordinal,
        iconResId = SnakeSkin.KING.headResId,
        nameResId = R.string.skin_king,
        descriptionResId = null
    ),
    BOOSTER_WALLS_EATING(
        type = OfferType.BOOSTER,
        price = 25,
        productId = SnakeBooster.WALLS_EATING.ordinal,
        iconResId = R.drawable.booster_eat_walls,
        nameResId = R.string.booster_name_walls_eating,
        descriptionResId = R.string.booster_description_walls_eating
    ),
    BOOSTER_SNAKE_EATING(
        type = OfferType.BOOSTER,
        price = 50,
        productId = SnakeBooster.SNAKE_EATING.ordinal,
        iconResId = R.drawable.booster_eat_snake,
        nameResId = R.string.booster_name_snake_eating,
        descriptionResId = R.string.booster_description_snake_eating
    )
}
