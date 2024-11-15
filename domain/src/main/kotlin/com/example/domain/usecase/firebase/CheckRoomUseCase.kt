package com.example.domain.usecase.firebase

import com.google.firebase.firestore.CollectionReference
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CheckRoomUseCase @Inject constructor(private val  callCollection: CollectionReference) {
    operator fun invoke(roomId: String) = flow {
        if (roomId.isEmpty()) {
            emit(CheckRookResultType.NotInputAccessCode)
            return@flow
        }
        if (callCollection.document(roomId).get().await()["type"] == "END_CALL") {
            emit(CheckRookResultType.AlreadyExistRoom)
        } else {
            emit(CheckRookResultType.NotExistRoom)
        }
    }.catch {
        emit(CheckRookResultType.Error)
    }
}

sealed interface CheckRookResultType {
    data object NotInputAccessCode : CheckRookResultType
    data object AlreadyExistRoom : CheckRookResultType
    data object NotExistRoom : CheckRookResultType
    data object Error : CheckRookResultType
}