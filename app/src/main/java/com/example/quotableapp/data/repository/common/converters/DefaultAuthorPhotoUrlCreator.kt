package com.example.quotableapp.data.repository.common.converters

class DefaultAuthorPhotoUrlCreator: AuthorPhotoUrlCreator {
    override fun create(authorSlug: String): String {
        val size = 200
        return "https://images.quotable.dev/profile/$size/$authorSlug.jpg"
    }
}