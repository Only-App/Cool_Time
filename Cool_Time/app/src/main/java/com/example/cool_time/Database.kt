package com.example.cool_time

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.cool_time.model.Alarm

@Database(entities = [Alarm::class], version = 1)
abstract class UserDatabase : RoomDatabase(){
    abstract fun alarmDao() : AlarmDAO  //알람 객체 DAO(Data Access Object)

    companion object{   //Database는 싱글톤 객체로
        private var instance : UserDatabase?= null

        @Synchronized
        fun getInstance(context : Context) : UserDatabase? {
            if(instance == null){   //instance 생성이 안되어 있으면 생성
               synchronized(Database::class){
                   instance =  Room.databaseBuilder(
                       context.applicationContext,
                       UserDatabase::class.java,
                       "db"
                   ).fallbackToDestructiveMigration()
                       .build()
               }
            }
            return instance
        }
    }
}