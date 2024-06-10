package com.github.aakumykov.server

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.aakumykov.common.inMainThread
import com.github.aakumykov.common.showToast
import com.github.aakumykov.kotlin_playground.UserGesture
import com.github.aakumykov.server.databinding.FragmentServerBinding
import com.github.aakumykov.common.DEFAULT_SERVER_ADDRESS
import com.github.aakumykov.common.DEFAULT_SERVER_PATH
import com.github.aakumykov.common.DEFAULT_SERVER_PORT
import com.github.aakumykov.server.ktor_server.KtorServer
import com.gitlab.aakumykov.exception_utils_module.ExceptionUtils
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random

class ServerFragment : Fragment(R.layout.fragment_server), View.OnTouchListener {

    private var _binding: FragmentServerBinding? = null
    private val binding get() = _binding!!

    private val gestureRecorder by lazy { GestureRecorder }

    private val ktorServer: KtorServer by lazy { KtorServer(Gson()) }

    private fun showError(throwable: Throwable) {
        binding.serverErrorView.apply {
            text = ExceptionUtils.getErrorMessage(throwable)
            visibility = View.VISIBLE
        }
    }

    private fun hideError() {
        binding.serverErrorView.apply {
            text = ""
            visibility = View.GONE
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentServerBinding.bind(view)

        lifecycleScope.launch {
            GestureRecorder.recordedGestureFlow.collect(::onNewGesture)
        }

        binding.touchRecordingArea.setOnTouchListener(this)
        binding.startServerButton.setOnClickListener { startServer() }
        binding.stopServerButton.setOnClickListener { stopServer() }
        binding.sendTestMessageButton.setOnClickListener { sendTetsMessage() }
        binding.sendCloseMessageButton.setOnClickListener { sendCloseMessage() }
    }

    private fun sendCloseMessage() {
        lifecycleScope.launch(Dispatchers.IO) {
            ktorServer.sendCloseMessage()
        }
    }

    private fun sendTetsMessage() {
        lifecycleScope.launch(Dispatchers.IO) {
            ktorServer.sendTestMessage("Привет-${Random.nextInt(100)}")
        }
    }

    private fun onNewGesture(gesture: UserGesture?) {
        gesture?.also {
            Log.d(TAG, "Новый записанный жест: $gesture")
            lifecycleScope.launch(Dispatchers.IO) {
                ktorServer.sendUserGesture(gesture)
            }
        }
    }

    private fun startServer() {
        hideError()
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                ktorServer.start(DEFAULT_SERVER_ADDRESS, DEFAULT_SERVER_PORT, DEFAULT_SERVER_PATH)
            } catch (e: Exception) {
                inMainThread { showError(e) }
                Log.e(TAG, ExceptionUtils.getErrorMessage(e), e)
            }
        }
    }

    private fun stopServer() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                ktorServer.stop()
                // TODO: сообщать об останове, основываясь на статусе, полученном от сервера
                inMainThread { showToast(R.string.server_was_stopped) }
            } catch (e: Exception) {
                inMainThread { showError(e) }
                Log.e(TAG, ExceptionUtils.getErrorMessage(e), e)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when(event?.action) {
            MotionEvent.ACTION_DOWN -> gestureRecorder.startRecording(event)
            MotionEvent.ACTION_MOVE -> gestureRecorder.recordEvent(event)
            MotionEvent.ACTION_UP -> gestureRecorder.finishRecording(event)
            MotionEvent.ACTION_CANCEL -> gestureRecorder.cancelRecording()
            MotionEvent.ACTION_OUTSIDE -> { Toast.makeText(requireContext(), "ACTION_OUTSIDE", Toast.LENGTH_SHORT).show() }
            else -> {}
        }
        return true
    }

    companion object {
        val TAG: String = ServerFragment::class.java.simpleName
        @JvmStatic
        fun newInstance(): ServerFragment {
            return ServerFragment()
        }
    }
}