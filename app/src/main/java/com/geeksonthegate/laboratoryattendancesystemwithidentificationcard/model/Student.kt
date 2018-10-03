package com.geeksonthegate.laboratoryattendancesystemwithidentificationcard.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.io.Serializable

open class Student(@PrimaryKey var idm: String? = null,
                   var studentId: String? = null,
                   var name: String? = null,
                   var lab: Lab? = null) : RealmObject(), Serializable {
    init {
        idm ?: throw Exception("Invalid idm")
        lab ?: throw Exception("Invalid Lab")
        studentId?.let { if (it.length != 7) throw Exception("Invalid studentId") }
                ?: throw Exception("Invalid studentId")
        name?.let { if (it.length > 7 || it.isEmpty()) throw Exception("Invalid name") }
                ?: throw Exception("Invalid name")
    }
}