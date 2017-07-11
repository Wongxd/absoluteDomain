package com.wongxd.wthing_kotlin.database

import org.jetbrains.anko.db.MapRowParser
import org.jetbrains.anko.db.RowParser

/**
 * Created by wxd1 on 2017/6/28.
 */

/**
 * table 结构
 */
object CompanyTable {
    val TABLE_NAME = "Company"
    val ID = "_id"
    val NAME = "name"
    val ADDRESS = "address"
}


/**
 * table 对象
 */
data class Company(val map: MutableMap<String, Any?>) {
    var _id: Long by map
    var name: String by map
    var address: String by map

    constructor() : this(HashMap())

    constructor(id: Long, name: String, address: String) : this(HashMap()) {
        this._id = id
        this.name = name
        this.address = address
    }
}


/**
 *table 对象 对应 的 rowparser
 */
class CompanyRowParser : RowParser<Company> {
    override fun parseRow(columns: Array<Any?>): Company {
        return Company(columns[0] as Long, columns[1] as String, columns[2] as String)
    }
}


/**
 *table 对象 对应 的 maprowparser
 */
class CompanyMapRowParser : MapRowParser<Company> {
    override fun parseRow(columns: Map<String, Any?>): Company {
        return Company(columns[CompanyTable.ID] as Long, columns[CompanyTable.NAME] as String, columns[CompanyTable.ADDRESS] as String)
    }
}