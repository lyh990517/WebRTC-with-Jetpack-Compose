package com.example.firebase

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    private const val COLLECTION_CALLS = "calls"

    @Provides
    @Singleton
    fun provideFirebase(@ApplicationContext context: Context) =
        FirebaseApp.initializeApp(context)

    @Singleton
    @Provides
    fun providesFireStore() = Firebase.firestore


    @Singleton
    @Provides
    fun provideCallsCollection(
        fireStore: FirebaseFirestore
    ): CollectionReference = fireStore.collection(COLLECTION_CALLS)

}
