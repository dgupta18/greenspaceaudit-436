package com.example.green_space_audits.mainactivity

data class GreenSpace (val gsName: String = "",
                       val gsCreator: String = "",
                       val gsLat: Float = 0.toFloat(),
                       val gsLong: Float = 0.toFloat(),
                       val gsAcres: Float = 0.toFloat(),
                       val gsAvgQuality: Float = 0.toFloat(),
                       val numRankings: Int = 0,
                       val gsType: Recreation = Recreation.NATUREBASED,
                       val gsComments: MutableMap<String, Comment> = mutableMapOf<String, Comment>(),
                       val gsIsQuiet: Boolean = false,
                       val gsIsNearHazards: Boolean = false)

enum class Quality {
    LOW, MED, HIGH

}

enum class Recreation(val displayStr: String) {
    PEOPLEPOWERED("People-powered"), NATUREBASED("Nature-based")
}