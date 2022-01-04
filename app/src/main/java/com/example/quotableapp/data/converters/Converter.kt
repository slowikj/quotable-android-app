package com.example.quotableapp.data.converters

interface Converter<Source, Destination> {
    operator fun invoke(source: Source): Destination
}