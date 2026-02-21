package com.avinashpatil.app.admindisha.di

import android.content.Context
import androidx.room.Room
import com.avinashpatil.app.admindisha.data.local.AppDatabase
import com.avinashpatil.app.admindisha.data.local.dao.HomeDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "disha_admin_db"
        ).fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideHomeDao(database: AppDatabase): HomeDao {
        return database.homeDao()
    }
}
