package com.prasoon.apodkotlinrefactored.core.common

enum class ScreenPreference(
    val title: String,
    val value: Int,
) {
    HOME_SCREEN("home screen", 1),
    LOCK_SCREEN("lock screen", 2),
    BOTH_SCREENS("both screens", 3);

    companion object{
        fun getTitle(value: Int): String {
            for (i in ScreenPreference.values()) {
                if (i.value == value) {
                    return i.title
                }
            }
            return ""
        }
    }
}