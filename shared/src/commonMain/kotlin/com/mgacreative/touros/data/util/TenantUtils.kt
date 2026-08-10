package com.mgacreative.touros.data.util

private val UUID_REGEX = Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")

fun String?.isValidUuid(): Boolean {
    if (this.isNullOrBlank()) return false
    return UUID_REGEX.matches(this.trim())
}

fun generateUuid(): String {
    val chars = "0123456789abcdef"
    fun randomHex(length: Int): String = (1..length).map { chars.random() }.joinToString("")
    val variantChars = listOf("8", "9", "a", "b")
    return "${randomHex(8)}-${randomHex(4)}-4${randomHex(3)}-${variantChars.random()}${randomHex(3)}-${randomHex(12)}"
}
