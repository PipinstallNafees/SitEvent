package com.example.sitevent.di

import android.app.Application
import android.content.Context
import com.example.sitevent.Notification.FirebaseMessaging.FcmApi
import com.example.sitevent.data.repository.Inteface.AuthRepository
import com.example.sitevent.data.repository.Implementation.AuthRepositoryImpl
import com.example.sitevent.data.repository.Inteface.ClubCategoryRepository
import com.example.sitevent.data.repository.Implementation.ClubCategoryRepositoryImpl
import com.example.sitevent.data.repository.Inteface.ClubRepository
import com.example.sitevent.data.repository.Implementation.ClubRepositoryImpl
import com.example.sitevent.data.repository.Inteface.EventRepository
import com.example.sitevent.data.repository.Implementation.EventRepositoryImpl
import com.example.sitevent.data.repository.Implementation.FcmRepositoryImpl
import com.example.sitevent.data.repository.Implementation.SubEventRepositoryImpl
import com.example.sitevent.data.repository.Implementation.TicketRepositoryImpl
import com.example.sitevent.data.repository.Inteface.UserRepository
import com.example.sitevent.data.repository.Implementation.UserRepositoryImpl
import com.example.sitevent.data.repository.Inteface.FcmRepository
import com.example.sitevent.data.repository.Inteface.SubEventRepository
import com.example.sitevent.data.repository.Inteface.TicketRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.messaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore =
        FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth =
        FirebaseAuth.getInstance()

    @Provides
    fun provideStorage (): FirebaseStorage = Firebase.storage

    @Provides
    fun provideFirebaseMessaging (): FirebaseMessaging = Firebase.messaging

    @Provides
    fun provideContext(application: Application): Context {
        return application.applicationContext
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): AuthRepository{
        return AuthRepositoryImpl(firebaseAuth, firestore)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth
    ): UserRepository {
        return UserRepositoryImpl(firestore, firebaseAuth)
    }

    @Provides
    @Singleton
    fun provideClubCategoryRepository(
        firestore: FirebaseFirestore
    ): ClubCategoryRepository {
        return ClubCategoryRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideClubRepository(
        firestore: FirebaseFirestore
    ): ClubRepository {
        return ClubRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideEventRepository(
        firestore: FirebaseFirestore
    ): EventRepository {
        return EventRepositoryImpl(firestore)
    }


    @Provides
    @Singleton
    fun provideTicketRepository(
        firestore: FirebaseFirestore
    ): TicketRepository {
        return TicketRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideSubEventRepository(
        firestore: FirebaseFirestore
    ): SubEventRepository {
        return SubEventRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideFcmApi(): FcmApi = Retrofit.Builder()
        .baseUrl("https://siteventnotificationserver.onrender.com")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(FcmApi::class.java)

    @Provides
    @Singleton
    fun provideFcmRepository(api: FcmApi): FcmRepository = FcmRepositoryImpl(api)


}