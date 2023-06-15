package com.dviss.tracker_domain.model

sealed class MealType(val name: String) {
    object Breakfast: MealType("breakfast")
    object Lunch: MealType("lunch")
    object Dinner: MealType("dinner")
    object Snack: MealType("snack")

    companion object {
        fun fromString(name: String): MealType {
            return when (name.lowercase()) {
                "breakfast" -> Breakfast
                "lunch" -> Breakfast
                "dinner" -> Breakfast
                "snack" -> Breakfast
                else -> Breakfast
            }
        }
    }
}