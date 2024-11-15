package com.example.config

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.circuit.wrapEventSink
import com.example.designsystem.ext.showToast
import com.example.domain.usecase.firebase.CheckRookResultType
import com.example.domain.usecase.firebase.CheckRoomUseCase
import com.example.screen.ConfigScreen
import com.example.screen.ConnectScreen
import com.example.screen.ConnectType
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


class ConfigPresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
    @ApplicationContext private val context: Context,
    private val checkRoomUseCase: CheckRoomUseCase
) : Presenter<ConfigUiState> {

    @Composable
    override fun present(): ConfigUiState {
        var accessCode by remember { mutableStateOf("") }

        val eventSink : CoroutineScope.(ConfigUiEvent) -> Unit = { event ->
            when(event){
                is ConfigUiEvent.InputAccessCode -> {
                    accessCode = event.code
                }
                is ConfigUiEvent.Route -> {
                    checkRoomUseCase(accessCode).onEach { result ->
                        when (result) {
                            CheckRookResultType.AlreadyExistRoom, CheckRookResultType.Error -> {
                                context.showToast("방에 접속할 수 없습니다.")
                            }

                            CheckRookResultType.NotInputAccessCode -> {
                                context.showToast("방 번호를 입력해주세요.")
                            }

                            CheckRookResultType.NotExistRoom -> {
                                val type = when (event) {
                                    ConfigUiEvent.Route.CreateRoom -> ConnectType.CREATE
                                    ConfigUiEvent.Route.JoinRoom -> ConnectType.JOIN
                                }
                                navigator.goTo(ConnectScreen(accessCode, type))
                            }
                        }
                    }.launchIn(this)
                }
            }
        }
        return ConfigUiState(
            accessCode = accessCode,
            eventSink = wrapEventSink(eventSink)
        )
    }

    @CircuitInject(ConfigScreen::class, ActivityRetainedComponent::class)
    @AssistedFactory
    fun interface Factory {
        fun create(navigator: Navigator): ConfigPresenter
    }
}