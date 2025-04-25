package com.example.soundnest_android.ui.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soundnest_android.network.RegisterRequest
import com.example.soundnest_android.network.RegisterResponse
import com.example.soundnest_android.network.authService
import kotlinx.coroutines.launch
import retrofit2.Response

sealed class RegisterState {
    object Idle    : RegisterState()
    object Loading : RegisterState()
    data class Success(val data: RegisterResponse) : RegisterState()
    data class Error(val msg: String)             : RegisterState()
}

class RegisterViewModel : ViewModel() {
    private val _state = MutableLiveData<RegisterState>(RegisterState.Idle)
    val state: LiveData<RegisterState> = _state

    fun register(username: String, email: String, password: String) {
        _state.value = RegisterState.Loading
        viewModelScope.launch {
            try {
                val resp: Response<RegisterResponse> =
                    authService.register(RegisterRequest(username, email, password))

                if (resp.isSuccessful && resp.body() != null) {
                    _state.value = RegisterState.Success(resp.body()!!)
                } else {
                    _state.value = RegisterState.Error("Error ${resp.code()}: ${resp.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                _state.value = RegisterState.Error(e.localizedMessage ?: "Error desconocido")
            }
        }
    }
}
