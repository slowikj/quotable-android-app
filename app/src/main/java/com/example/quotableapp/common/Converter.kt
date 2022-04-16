package com.example.quotableapp.common

interface Converter<Source, Destination> {
    operator fun invoke(source: Source): Destination
}