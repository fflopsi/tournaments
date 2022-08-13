package me.frauenfelderflorian.tournamentscompose.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

private val THEME_KEY = intPreferencesKey("theme")

class Prefs(private val context: Context) : ViewModel() {
    private val Context.dataStore by preferencesDataStore(name = "settings")
    var theme by mutableStateOf(0)
        private set
    val themeFlow = context.dataStore.data.map { prefs -> prefs[THEME_KEY] ?: 0 }

    fun updateTheme(newTheme: Int) = runBlocking {
        if (newTheme < 0 || newTheme > 2) throw IllegalArgumentException("Theme ID must be 0, 1, or 2")
        theme = newTheme
        launch {
            context.dataStore.edit { prefs -> prefs[THEME_KEY] = newTheme }
        }
    }
}


class PrefsFactory(private val context: Context) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = Prefs(context = context) as T
}
