package com.example.quotableapp.data.db.entities

import androidx.room.TypeConverter

class ConverterAdapters {

    @TypeConverter
    fun toString(list: List<String>): String {
        return list.joinToString(separator = " ")
    }

    @TypeConverter
    fun getListFromString(str: String): List<String> {
        return str.split(" ")
    }

    @TypeConverter
    fun fromRemoteKeyType(type: RemoteKeyEntity.Type) = type.ordinal

    @TypeConverter
    fun toRemoteKeyType(ordinal: Int) = enumValues<RemoteKeyEntity.Type>()[ordinal]

}