package com.group.libraryapp.dto.user.response

import com.group.libraryapp.domain.user.User

data class UserResponse(
    var id: Long,
    var name: String,
    var age: Int?,
) {
    companion object {
        fun of(user: User) : UserResponse {
            return UserResponse(
                id = user.id!!,
                name = user.name,
                age = user.age
            )
        }
    }

}
