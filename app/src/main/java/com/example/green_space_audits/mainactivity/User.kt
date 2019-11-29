package com.example.green_space_audits.mainactivity

data class User (
    val userName: String = "",
    val userEmail: String = "",
    val userPassword: String = "",
    val userIsAdmin: Boolean = false,
    val uPoints: Int = 0,
    val uGreenSpaces: ArrayList<String> = arrayListOf<String>(),
    val uBadges: ArrayList<String> = arrayListOf<String>(),
    val uFavorites: ArrayList<String> = arrayListOf<String>(),
    val uCheckins: MutableMap<String, ArrayList<String>> = mutableMapOf<String, ArrayList<String>>(),
    val uComments: MutableMap<String, Comment> = mutableMapOf<String, Comment>()
)
