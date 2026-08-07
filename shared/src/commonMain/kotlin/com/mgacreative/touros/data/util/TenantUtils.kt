package com.mgacreative.touros.data.util

private val UUID_REGEX = Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")

fun String?.isValidUuid(): Boolean {
    if (this.isNullOrBlank()) return false
    return UUID_REGEX.matches(this.trim())
}
