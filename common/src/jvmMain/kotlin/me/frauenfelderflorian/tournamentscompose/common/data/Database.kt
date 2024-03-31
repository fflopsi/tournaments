package me.frauenfelderflorian.tournamentscompose.common.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import me.frauenfelderflorian.tournamentscompose.TournamentsDB

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        val driver: SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        TournamentsDB.Schema.create(driver)
        return driver
    }
}
