package com.codewithpk.palmpay.di

import android.content.Context
import androidx.room.Room
import com.codewithpk.palmpay.data.local.AppDatabase
import com.codewithpk.palmpay.data.local.EnrolledPalmDao
import com.codewithpk.palmpay.data.local.PalmScanDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context, AppDatabase::class.java, "palmpay_db" // Name of your database file
        ).fallbackToDestructiveMigration().build()
    }

    @Singleton
    @Provides
    fun providePalmScanDao(db: AppDatabase): PalmScanDao {
        return db.palmScanDao()
    }

    @Singleton
    @Provides
    fun provideEnrolledPalmDao(db: AppDatabase): EnrolledPalmDao {
        return db.enrolledPalmDao()
    }
}