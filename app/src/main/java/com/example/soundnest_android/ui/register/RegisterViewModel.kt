package com.example.soundnest_android.ui.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import soundNest.auth.IAuthService
import com.example.soundnest_android.network.ApiClient
import soundNest.models.NewUserRequest
//TODO es este import
//import soundNest.models.NewUserResponse
import kotlinx.coroutines.launch
import retrofit2.Response


sealed class RegisterState {
    object Idle    : RegisterState()
    object Loading : RegisterState()
    data class Success(val data: Unit) : RegisterState()
    //TODO este es el bueno
    //data class Success(val data: NewUserResponse) : RegisterState()
    data class Error(val msg: String)             : RegisterState()
}

class RegisterViewModel : ViewModel() {
    private val _state = MutableLiveData<RegisterState>(RegisterState.Idle)
    val state: LiveData<RegisterState> = _state

    val authService: IAuthService = ApiClient.retrofit.create(IAuthService::class.java)

    fun register(username: String, email: String, password: String) {
        _state.value = RegisterState.Loading
        viewModelScope.launch {
            try {
                //TODO es Response<NewUserResponse>
                val resp: Response<Unit> =
                    authService.createUser(NewUserRequest(username, email, password))

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
