package com.onlyapp.cooltime.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.onlyapp.cooltime.utils.StringListConverter
import com.onlyapp.cooltime.data.entity.Alarm
import com.onlyapp.cooltime.data.entity.ExceptApp
import com.onlyapp.cooltime.data.entity.PhoneLock

@Database(entities = [Alarm::class, PhoneLock::class, ExceptApp::class], version = 8)
@TypeConverters(StringListConverter::class)
abstract class UserDatabase : RoomDatabase(){
    abstract fun alarmDao() : AlarmDAO  //알람 객체 DAO(Data Access Object)
    abstract fun phoneLockDao() : LockDAO  //잠금 설정 객체 DAO

    abstract fun exceptAppsDao() : ExceptAppDAO    //예외 앱 객체 DAO
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