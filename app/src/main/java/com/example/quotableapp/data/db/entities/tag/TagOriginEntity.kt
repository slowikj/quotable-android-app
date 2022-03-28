package com.example.quotableapp.data.db.entities.tag

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tag_origins",
    indices = [Index(value = ["type"], unique = true)]
)
data class TagOriginEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @Embedded val originParams: TagOriginParams,
    val lastUpdatedMillis: Long
)

data class TagOriginParams(
    val type: Type
) {

    enum class Type {
        ALL,
        DASHBOARD_EXEMPLARY;
    }
}
