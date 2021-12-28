package com.example.quotableapp.data.repository.common.converters

interface Converter<Source, Destination> {
    operator fun invoke(source: Source): Destination
}