package com.example.presentaion.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.domain.event.PeerConnectionEvent
import com.example.domain.event.WebRTCEvent
import com.example.domain.state.UiState
import com.example.presentaion.databinding.FragmentConnectionBinding
import com.example.presentaion.viewmodel.ConnectionViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ConnectionFragment : Fragment() {

    private val viewModel: ConnectionViewModel by viewModels()

    private var _binding: FragmentConnectionBinding? = null
    private val binding
        get() = checkNotNull(_binding) { "binding was accessed outside of view lifecycle" }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConnectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        collectConnectionEvent()
        collectRTCEvent()
        collectState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun collectState() {
        lifecycleScope.launch {
            viewModel.uiState.collect{
                when(it){
                    is UiState.UnInitialized -> viewModel.initRTC()
                }
            }
        }
    }

    private fun collectRTCEvent() {
        lifecycleScope.launch {
            viewModel.webRTCEvent.collect {
                when (it) {
                    is WebRTCEvent.Initialize -> {
                        it.webRTCClient.apply {
                            initPeerConnectionFactory(requireActivity().application)
                            initVideoCapture(requireActivity().application)
                            initSurfaceView(binding.remoteView)
                            initSurfaceView(binding.localView)
                            startLocalView(binding.localView)
                            viewModel.call()
                            viewModel.connect()
                        }
                    }
                }
            }
        }
    }

    private fun collectConnectionEvent() {
        lifecycleScope.launch {
            viewModel.peerConnectionEvent.collect {
                when (it) {
                    is PeerConnectionEvent.OnIceCandidate -> {
                        viewModel.sendIceCandidate(it.data)
                        viewModel.addCandidate(it.data)
                    }

                    is PeerConnectionEvent.OnAddStream -> {
                        it.data.videoTracks?.get(0)?.addSink(binding.remoteView)
                    }
                }
            }
        }
    }
}