package uz.codial6.codial.models

data class UserData(
    var id: String? = null,
    var name: String? = null,
    var surname: String? = null,
    var imageUrl: String? = null,
    var phoneNumber: String? = null,
    var listRating: ArrayList<Rating> = ArrayList(),
)

data class Rating(
    var ball: String? = null,
    var courseId: String? = null,
)