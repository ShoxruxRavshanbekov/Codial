package uz.codial6.codial.models

class DataOfTheUserWhoWantsToJoinNewGroups {
    var id: String? = null
    var name: String? = null
    var surname: String? = null
    var course: String? = null


    constructor()

    constructor(id: String?, name: String?, surname: String?, course: String?) {
        this.id = id
        this.name = name
        this.surname = surname
        this.course = course
    }
}
