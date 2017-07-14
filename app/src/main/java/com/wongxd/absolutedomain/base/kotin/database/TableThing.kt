package com.wongxd.wthing_kotlin.database

import org.jetbrains.anko.db.MapRowParser
import org.jetbrains.anko.db.RowParser

/**
 * Created by wxd1 on 2017/6/28.
 */

/**
 * table 结构
 * 单张图
 */
object GirlTable {
    val TABLE_NAME = "girl"
    val ID = "_id"
    val NAME = "name"
    val ADDRESS = "address"
    val IMGPATH = "imgPath"
}


/**
 * table 对象
 * 单张图
 */
data class Girl(val map: MutableMap<String, Any?>) {
    var _id: Long by map
    var name: String by map
    var address: String by map
    var imgPath: String by map


    constructor() : this(HashMap())

    constructor(id: Long, name: String, address: String, imgPath: String) : this(HashMap()) {
        this._id = id
        this.name = name
        this.address = address
        this.imgPath = imgPath
    }
}


/**
 * 图集
 */
object TuTable {
    val TABLE_NAME = "tu"
    val ID = "_id"
    val NAME = "name"
    val ADDRESS = "address"
    val IMGPATH = "imgPath"
}


/**
 * 图集
 */
data class Tu(val map: MutableMap<String, Any?>) {
    var _id: Long by map
    var name: String by map
    var address: String by map
    var imgPath: String by map

    constructor() : this(HashMap())

    constructor(id: Long, name: String, address: String, imgPath: String) : this(HashMap()) {
        this._id = id
        this.name = name
        this.address = address
        this.imgPath = imgPath
    }
}


/**
 *table 对象 对应 的 rowparser
 */
class CompanyRowParser : RowParser<Girl> {
    override fun parseRow(columns: Array<Any?>): Girl {
        return Girl(columns[0] as Long, columns[1] as String, columns[2] as String, columns[3] as String)
    }
}


/**
 *table 对象 对应 的 maprowparser
 */
class CompanyMapRowParser : MapRowParser<Girl> {
    override fun parseRow(columns: Map<String, Any?>): Girl {
        return Girl(columns[GirlTable.ID] as Long, columns[GirlTable.NAME] as String,
                columns[GirlTable.ADDRESS] as String, columns[GirlTable.ADDRESS] as String)
    }
}