package com.example.quotableapp.data.converters.author

class DefaultAuthorPhotoUrlCreator : AuthorPhotoUrlCreator {
    override fun create(authorSlug: String): String {
        val size = 200
        return "https://images.quotable.dev/profile/$size/$authorSlug.jpg"
    }
}