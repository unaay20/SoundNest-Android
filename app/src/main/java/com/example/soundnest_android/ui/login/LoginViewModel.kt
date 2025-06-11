package com.example.soundnest_android.ui.login

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.soundnest_android.network.ApiService
import com.example.soundnest_android.restful.constants.RestfulRoutes
import com.example.soundnest_android.restful.models.auth.LoginResponse
import com.example.soundnest_android.restful.services.AuthService
import com.example.soundnest_android.restful.services.UserService
import com.example.soundnest_android.restful.utils.ApiResult
import com.example.soundnest_android.utils.Constants
import com.example.soundnest_android.utils.toDisplayMessage
import kotlinx.coroutines.launch

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val data: LoginResponse) : LoginState()
    data class Error(val msg: String) : LoginState()
}

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableLiveData<LoginState>(LoginState.Idle)
    val state: LiveData<LoginState> = _state

    private val tokenProvider = ApiService.tokenProvider
    private val authService = AuthService(RestfulRoutes.getBaseUrl())
    private val userService = UserService(RestfulRoutes.getBaseUrl(), tokenProvider)

    fun login(username: String, password: String) {
        _state.value = LoginState.Loading
        viewModelScope.launch {
            when (val result = authService.login(username, password)) {
                is ApiResult.Success -> {
                    result.data?.let {
                        tokenProvider.saveToken(it.token)
                        when (val addInfo = userService.getAdditionalInfo()) {
                            is ApiResult.Success ->
                                addInfo.data?.info?.let { info ->
                                    Log.d(Constants.LOGIN_ACTIVITY, "Additional info: $info")
                                    tokenProvider.saveAdditionalInformation(info)
                                }

                            else -> { /* ignoro */
                            }
                        }
                        tokenProvider.getToken()?.let { token ->
                            Log.d(Constants.LOGIN_ACTIVITY, token)
                        }
                        _state.value = LoginState.Success(it)
                    } ?: run {
                        _state.value = LoginState.Error("Respuesta vacía del servidor")
                    }
                }

                is ApiResult.HttpError -> {
                    _state.value = LoginState.Error(
                        result.toDisplayMessage(getApplication())
                    )
                    Log.d(
                        Constants.LOGIN_ACTIVITY,
                        "HTTP ${result.code}: ${result.message}"
                    )
                }

                is ApiResult.NetworkError -> {
                    _state.value = LoginState.Error(
                        result.toDisplayMessage(getApplication())
                    )
                    Log.d(
                        Constants.LOGIN_ACTIVITY,
                        "Red: ${result.exception.message}"
                    )
                }

                is ApiResult.UnknownError -> {
                    _state.value = LoginState.Error(
                        result.toDisplayMessage(getApplication())
                    )
                    Log.d(
                        Constants.LOGIN_ACTIVITY,
                        "Desconocido: ${result.exception.message}"
                    )
                }
            }
        }
    }
}
