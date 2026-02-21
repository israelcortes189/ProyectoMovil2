package com.example.marsphotos.utils

import android.util.Base64
import java.security.MessageDigest

fun md5Base64(input: String): String {
    val md = MessageDigest.getInstance("MD5")
    val digest = md.digest(input.toByteArray(Charsets.UTF_8))
    return Base64.encodeToString(digest, Base64.NO_WRAP)
}

