package com.group.libraryapp.dto.book.request

import com.group.libraryapp.domain.book.BookType

data class BookCreateRequest(
    val name: String,
    val type: BookType,
)
