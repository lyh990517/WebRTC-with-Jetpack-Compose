package com.example.connect

import android.util.Log
import androidx.compose.runtime.Composable
import com.example.screen.ConnectScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.components.ActivityRetainedComponent

class ConnectPresenter @AssistedInject constructor(
    @Assisted private val screen: ConnectScreen,
    @Assisted private val navigator: Navigator
) : Presenter<ConnectUiState> {

    @Composable
    override fun present(): ConnectUiState {
        Log.d("결과", screen.type.toString())
        return ConnectUiState()
    }

    @CircuitInject(ConnectScreen::class, ActivityRetainedComponent::class)
    @AssistedFactory
    fun interface Factory {
        fun create(
            screen: ConnectScreen,
            navigator: Navigator
        ): ConnectPresenter
    }
}