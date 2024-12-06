package com.piseysen.todtoapp.domain

import org.mongodb.kbson.ObjectId
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class ToDoTask: RealmObject{

    @PrimaryKey
    var _id: ObjectId = ObjectId()
    var title: String = ""
    var description: String = ""
    var favorite: Boolean = false
    var completed: Boolean = false
}