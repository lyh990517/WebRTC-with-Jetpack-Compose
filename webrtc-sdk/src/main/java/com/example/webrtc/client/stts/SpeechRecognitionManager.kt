package com.example.webrtc.client.stts

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.example.webrtc.client.event.WebRtcEvent
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class SpeechRecognitionManager @Inject constructor(
    private val speechRecognizer: SpeechRecognizer
) {
    private val speechEvent = MutableSharedFlow<WebRtcEvent>(extraBufferCapacity = 100)
    private val speechRecognizerIntent =
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }


    fun getResult() = callbackFlow {
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(bundle: Bundle) {
                Log.d("SpeechRecognizer", "onReadyForSpeech called")
            }

            override fun onBeginningOfSpeech() {
                Log.d("SpeechRecognizer", "onBeginningOfSpeech called")
            }

            override fun onRmsChanged(v: Float) {
                Log.d("SpeechRecognizer", "onRmsChanged called with value: $v")
            }

            override fun onBufferReceived(bytes: ByteArray) {
                Log.d(
                    "SpeechRecognizer",
                    "onBufferReceived called with buffer size: ${bytes.size}"
                )
            }

            override fun onEndOfSpeech() {
                Log.d("SpeechRecognizer", "onEndOfSpeech called")
                speechEvent.tryEmit(WebRtcEvent.Speech.End)
            }

            override fun onError(i: Int) {
                Log.e("SpeechRecognizer", "onError called with error code: $i")
            }

            override fun onResults(bundle: Bundle) {
                val matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                Log.d("SpeechRecognizer", "onResults called, matches: $matches")
                matches?.let {
                    trySend(it[0])
                }
            }

            override fun onPartialResults(bundle: Bundle) {
                Log.d("SpeechRecognizer", "onPartialResults called")
            }

            override fun onEvent(i: Int, bundle: Bundle) {
                Log.d("SpeechRecognizer", "onEvent called with event code: $i")
            }
        })

        awaitClose { }
    }

    fun getEvent() = speechEvent.asSharedFlow()

    fun startListen() {
        speechEvent.tryEmit(WebRtcEvent.Speech.Start)
        speechRecognizer.startListening(speechRecognizerIntent)
    }
}