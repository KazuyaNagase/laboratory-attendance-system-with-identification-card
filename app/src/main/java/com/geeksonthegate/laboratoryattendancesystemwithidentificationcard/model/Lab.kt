package com.geeksonthegate.laboratoryattendancesystemwithidentificationcard.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class Lab(@PrimaryKey
               var labId: String = UUID.randomUUID().toString(),
               var labName: String? = null,
               var coretimeArray: RealmList<CoreTime>? = null) : RealmObject() {


    init {
        labName?.let { if (it.length > 7 || it.isEmpty()) throw Exception("Invalid labName") }
                ?: throw Exception("Invalid LabName")

        coretimeArray?.let { if (it.size == 0) throw Exception("Invalid coretimeArray") }
                ?: throw Exception("Invalid CoreTime")
    }
}
