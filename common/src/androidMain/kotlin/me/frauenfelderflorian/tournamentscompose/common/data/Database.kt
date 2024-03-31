package me.frauenfelderflorian.tournamentscompose.common.data

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import me.frauenfelderflorian.tournamentscompose.TournamentsDB

actual class DriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(TournamentsDB.Schema, context, "TournamentsDB")
    }
}
