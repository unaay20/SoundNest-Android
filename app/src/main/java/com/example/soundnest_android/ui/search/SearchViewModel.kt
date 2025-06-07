package com.example.soundnest_android.ui.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.soundnest_android.auth.SharedPrefsTokenProvider
import com.example.soundnest_android.restful.constants.RestfulRoutes
import com.example.soundnest_android.restful.models.song.GenreResponse
import com.example.soundnest_android.restful.services.SongService
import com.example.soundnest_android.restful.utils.ApiResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val _recent = MutableLiveData<MutableList<String>>(mutableListOf())
    val recent: LiveData<MutableList<String>> = _recent

    private val _genres = MutableLiveData<List<GenreResponse>>()
    val genres: LiveData<List<GenreResponse>> = _genres

    private val service by lazy {
        SongService(RestfulRoutes.getBaseUrl(), SharedPrefsTokenProvider(getApplication()))
    }

    private val fileName = "recent_searches.txt"

    fun loadRecent() {
        val file = File(getApplication<Application>().filesDir, fileName)
        if (!file.exists()) file.createNewFile()
        val lines = file.readLines().map { it.trim() }.filter { it.isNotBlank() }
        _recent.value = lines.toMutableList()
    }

    fun saveRecent() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = _recent.value ?: return@launch
            File(getApplication<Application>().filesDir, fileName).bufferedWriter().use { w ->
                list.forEach { w.write("$it\n") }
            }
        }
    }

    fun addSearch(q: String) {
        val list = _recent.value ?: mutableListOf()
        list.indexOf(q).takeIf { it != -1 }?.let { list.removeAt(it) }
        list.add(0, q)
        _recent.value = list
        saveRecent()
    }

    fun removeSearch(q: String) {
        val list = _recent.value ?: return
        list.remove(q)
        _recent.value = list
        saveRecent()
    }

    fun clearAll() {
        _recent.value = mutableListOf()
        saveRecent()
    }

    fun loadGenres() {
        viewModelScope.launch {
            when (val r = service.getGenres()) {
                is ApiResult.Success -> _genres.value = r.data.orEmpty()
                else -> _genres.value = emptyList()
            }
        }
    }
}
