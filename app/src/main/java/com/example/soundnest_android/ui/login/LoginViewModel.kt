import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soundnest_android.network.authService
import com.example.soundnest_android.network.LoginRequest
import com.example.soundnest_android.network.LoginResponse
import kotlinx.coroutines.launch

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val data: LoginResponse) : LoginState()
    data class Error(val msg: String) : LoginState()
}

class LoginViewModel : ViewModel() {
    private val _state = MutableLiveData<LoginState>(LoginState.Idle)
    val state: LiveData<LoginState> = _state

    fun login(username: String, password: String) {
        _state.value = LoginState.Loading
        viewModelScope.launch {
            try {
                val resp = authService.login(LoginRequest(username, password))
                if (resp.isSuccessful && resp.body() != null) {
                    _state.value = LoginState.Success(resp.body()!!)
                } else {
                    _state.value = LoginState.Error("Código ${resp.code()}: ${resp.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                _state.value = LoginState.Error(e.localizedMessage ?: "Error desconocido")
            }
        }
    }
}
