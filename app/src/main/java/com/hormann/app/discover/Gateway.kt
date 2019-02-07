package com.hormann.app.discover

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Gateway(
        @ColumnInfo(name = "receiver") var receiver: String?,
        @ColumnInfo(name = "host") var host: String?,
        @ColumnInfo(name = "port") var port: Int
) {
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0
}