package com.example.quotableapp.data.db.entities.tag

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "tag_origins"
)
data class TagOriginEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: TagOriginType,
    val lastUpdatedMillis: Long
) {

}

enum class TagOriginType {
    ALL,
    DASHBOARD_EXEMPLARY;
}
