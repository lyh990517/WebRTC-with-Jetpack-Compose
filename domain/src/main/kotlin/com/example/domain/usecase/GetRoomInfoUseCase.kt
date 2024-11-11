package com.example.domain.usecase

import com.example.domain.repository.FireStoreRepository
import javax.inject.Inject

class GetRoomInfoUseCase @Inject constructor(private val fireStoreRepository: FireStoreRepository) {

    suspend operator fun invoke(roomId: String) = fireStoreRepository.getRoomInfo(roomId)
}