package com.valencia.demo.features.jsonplaceholder.data.datasources.remote.mapper

import com.valencia.demo.features.jsonplaceholder.data.datasources.remote.models.PostsDto
import com.valencia.demo.features.jsonplaceholder.domain.entities.Posts


fun PostsDto.toDomain(): Posts {
    return Posts(
        id = this.id,
        title = this.title,
        body = this.body
    )
}