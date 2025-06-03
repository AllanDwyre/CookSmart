package com.example.feedup.utils

fun lerp(start: Float, stop: Float, fraction: Float) =
    start + (stop - start) * fraction
