package com.example.webrtc.sdk.signaling

/**
 * This file is not used, but it is included for understanding the structure.
 */

private const val ROOM_COUNT = 1

private val fireStore = Collection(
    name = "calls", // ROOT
    documents = (1..ROOM_COUNT).map { id ->
        Documents(
            name = "room-$id",
            collection = listOf(
                Collection(
                    name = "sdp",
                    documents = listOf(
                        Documents(
                            name = "OFFER"
                        ),
                        Documents(
                            name = "ANSWER"
                        )
                    )
                ),
                Collection(
                    name = "ice",
                    documents = listOf(
                        Documents(
                            name = "OFFER",
                            field = Field(name = "ices")
                        ),
                        Documents(
                            name = "ANSWER",
                            field = Field(name = "ices")
                        )
                    )
                )
            ),
            field = Field(name = "startedAt")
        )
    }
)

private data class Collection(
    val name: String,
    val documents: List<Documents>? = null
)

private data class Documents(
    val name: String,
    val collection: List<Collection>? = null,
    val field: Field? = null
)

private data class Field(
    val name: String,
    val data: Map<String, Any?>? = null
)
