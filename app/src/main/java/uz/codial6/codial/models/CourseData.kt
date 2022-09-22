package uz.codial6.codial.models

class CourseData {
    var about: String? = null
    var imageId: String? = null
    var name: String? = null
    var imageLink: String? = null
    var uid: String? = null

    constructor()

    constructor(about: String?, imageId: String?, name: String?, imageLink: String?, uid: String?) {
        this.about = about
        this.imageId = imageId
        this.name = name
        this.imageLink = imageLink
        this.uid = uid
    }

    constructor(about: String?, name: String?, imageLink: String?) {
        this.about = about
        this.name = name
        this.imageLink = imageLink
    }

}
