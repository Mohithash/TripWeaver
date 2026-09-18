package com.mohithash.tripweaver

import android.app.Application
import androidx.room.Room
import com.mohithash.tripweaver.ai.AiClient
import com.mohithash.tripweaver.ai.TripAi
import com.mohithash.tripweaver.data.AppDb
import com.mohithash.tripweaver.data.JsonStore

class App : Application() {
    lateinit var db: AppDb
    lateinit var store: JsonStore
    val client = AiClient()
    val trips by lazy { TripAi(client) }
    override fun onCreate() {
        super.onCreate()
        db = Room.databaseBuilder(this, AppDb::class.java, "tripweaver.db").build()
        store = JsonStore(this)
    }
}
