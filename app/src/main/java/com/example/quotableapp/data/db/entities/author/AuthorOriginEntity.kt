package com.example.quotableapp.data.db.entities.author

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "author_origins",
    indices = [Index(value = ["type", "searchPhrase"], unique = true)]
)
data class AuthorOriginEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @Embedded val originParams: AuthorOriginParams
)

data class AuthorOriginParams(
    val type: Type,
    val searchPhrase: String = ""
) {

    enum class Type(private val str: String) {
        ALL("all"),
        EXAMPLE_FROM_DASHBOARD("example_from_dashboard");

        override fun toString(): String {
            return str
        }
    }
}
