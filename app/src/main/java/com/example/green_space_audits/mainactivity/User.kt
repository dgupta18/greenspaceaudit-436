package com.example.green_space_audits.mainactivity

data class User (
    val userName: String = "",
    val userEmail: String = "",
    val userPassword: String = "",
    val userIsAdmin: Boolean = false,
    val userPoints: Int = 0,
    val userGreenSpaces: MutableList<String> = mutableListOf<String>(),
    val userBadges: MutableList<String> = mutableListOf<String>(),
    val userFavorites: MutableMap<String, String> = mutableMapOf<String,String>(), // id to name
    val userCheckins: MutableMap<String, ArrayList<String>> = mutableMapOf<String, ArrayList<String>>(),
    val userComments: MutableMap<String, Comment> = mutableMapOf<String, Comment>()
)

enum class Badge(val displayStr: String) {
    ADD("Added a new green space"),
    COMMENT("Added a comment"),
    CHECKIN("Checked in to a green space"),
    FAVORITE("Favorited a green space"),
    PHOTO("Uploaded a picture")
}