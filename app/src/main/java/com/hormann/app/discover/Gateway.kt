package com.hormann.app.discover

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Gateway(
        @PrimaryKey var mac: String,
        @ColumnInfo(name = "hwVersion") var hwVersion: String?,
        @ColumnInfo(name = "protocol") var protocol: String?,
        @ColumnInfo(name = "sourceAddress") var sourceAddress: String?,
        @ColumnInfo(name = "swVersion") var swVersion: String?
) {
    override fun toString(): String {
        return "$sourceAddress ($mac)"
    }
}