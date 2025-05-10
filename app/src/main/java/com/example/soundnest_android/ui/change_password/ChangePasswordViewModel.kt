package com.example.soundnest_android.ui.change_password

import androidx.lifecycle.*
import com.example.soundnest_android.restful.constants.ApiRoutes.BASE_URL
import com.example.soundnest_android.restful.services.AuthService
import com.example.soundnest_android.restful.services.UserService
import com.example.soundnest_android.restful.utils.ApiResult
import kotlinx.coroutines.launch

sealed class SendCodeState {
    object Idle    : SendCodeState()
    object Loading : SendCodeState()
    object Success : SendCodeState()
    data class Error(val msg: String) : SendCodeState()
}

// Nuevo estado para el cambio de contraseña
sealed class ChangePasswordState {
    object Idle    : ChangePasswordState()
    object Loading : ChangePasswordState()
    object Success : ChangePasswordState()
    data class Error(val msg: String) : ChangePasswordState()
}

class ChangePasswordViewModel : ViewModel() {
    private val _sendCodeState = MutableLiveData<SendCodeState>(SendCodeState.Idle)
    val sendCodeState: LiveData<SendCodeState> = _sendCodeState

    private val _changeState = MutableLiveData<ChangePasswordState>(ChangePasswordState.Idle)
    val changeState: LiveData<ChangePasswordState> = _changeState

    private val authService = AuthService(BASE_URL)
    private val userService = UserService(BASE_URL)

    /** Envía el código de verificación al correo */
    fun sendCode(email: String) {
        _sendCodeState.value = SendCodeState.Loading
        viewModelScope.launch {
            when (val r = authService.sendCodeToEmail(email)) {
                is ApiResult.Success      -> _sendCodeState.value = SendCodeState.Success
                is ApiResult.HttpError    -> _sendCodeState.value = SendCodeState.Error("HTTP ${r.code}: ${r.message}")
                is ApiResult.NetworkError -> _sendCodeState.value = SendCodeState.Error("Red: ${r.exception.message}")
                is ApiResult.UnknownError -> _sendCodeState.value = SendCodeState.Error("Error: ${r.exception.message}")
            }
        }
    }


}
