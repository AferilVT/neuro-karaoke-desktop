package com.soul.neurokaraoke.data.model

data class User(
    val id: String,
    val username: String,
    val discriminator: String,
    val avatar: String?,
    val accessToken: String? = null
) {
    val displayName: String
        get() = if (discriminator != "0") "$username#$discriminator" else username

    val avatarUrl: String
        get() = avatar?.let {
            "https://cdn.discordapp.com/avatars/$id/$avatar.png"
        } ?: "https://cdn.discordapp.com/embed/avatars/${(id.toLongOrNull() ?: 0) % 5}.png"
}
