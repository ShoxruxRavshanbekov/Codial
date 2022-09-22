package uz.codial6.codial.models

data class Questions(
    var courseId: String = "",
    var listAnswer: ArrayList<Answer> = ArrayList(),
    var question: String = "",
    var uid: String = "",
)

data class Answer(
    var answer: String = "",
    var correct: Boolean = false,
)
