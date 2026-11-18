package com.example.trackermoney.ui

import androidx.annotation.StringRes
import androidx.annotation.ColorRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.trackermoney.R

@StringRes
fun categoryNameResource(category: String): Int = when (category) {
    "Продукты" -> R.string.category_food
    "Транспорт" -> R.string.category_transport
    "Покупки" -> R.string.category_shopping
    "Развлечения" -> R.string.category_entertainment
    "Жильё" -> R.string.category_housing
    "Здоровье" -> R.string.category_health
    "Подписки" -> R.string.category_subscriptions
    "Топливо" -> R.string.category_fuel
    "Красота" -> R.string.category_beauty
    "Путешествия" -> R.string.category_travel
    "Образование" -> R.string.category_education
    "Другое" -> R.string.category_other_legacy
    else -> R.string.category_other_legacy
}

@Composable
fun localizedCategoryName(category: String): String =
    stringResource(categoryNameResource(category))

@ColorRes
fun categoryColorResource(category: String): Int = when (category) {
    "Продукты" -> R.color.category_food
    "Транспорт" -> R.color.category_transport
    "Покупки" -> R.color.category_shopping
    "Развлечения" -> R.color.category_entertainment
    "Жильё" -> R.color.category_housing
    "Здоровье" -> R.color.category_health
    "Подписки" -> R.color.category_subscriptions
    "Топливо" -> R.color.category_fuel
    "Красота" -> R.color.category_beauty
    "Путешествия" -> R.color.category_travel
    "Образование" -> R.color.category_education
    else -> R.color.brand_accent
}

fun categoryIcon(category: String): String = when (category) {
    "Продукты" -> "🍎"
    "Транспорт" -> "🚌"
    "Покупки" -> "🛍️"
    "Развлечения" -> "🎬"
    "Жильё" -> "🏠"
    "Здоровье" -> "💊"
    "Подписки" -> "📱"
    "Топливо" -> "⛽"
    "Красота" -> "💅"
    "Путешествия" -> "✈️"
    "Образование" -> "📚"
    else -> "✦"
}
