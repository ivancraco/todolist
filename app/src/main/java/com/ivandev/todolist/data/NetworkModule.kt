package com.ivandev.todolist.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ivandev.todolist.domain.Repository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun getFirebaseFirestore(): FirebaseFirestore {
        return Firebase.firestore
    }

    @Provides
    fun getRepository(firebaseFirestore: FirebaseFirestore): Repository {
        return RepositoryImp(firebaseFirestore)
    }
}