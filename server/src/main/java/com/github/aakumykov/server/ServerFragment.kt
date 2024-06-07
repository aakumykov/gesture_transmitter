package com.github.aakumykov.server

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.aakumykov.kotlin_playground.Gesture
import com.github.aakumykov.server.databinding.FragmentServerBinding
import com.github.aakumykov.server.ktor_server.DEFAULT_SERVER_ADDRESS
import com.github.aakumykov.server.ktor_server.DEFAULT_SERVER_PORT
import com.github.aakumykov.server.ktor_server.KtorServer
import com.gitlab.aakumykov.exception_utils_module.ExceptionUtils
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

        GestureRecorder.recordedGesture.observe(viewLifecycleOwner, ::onNewGesture)

        binding.touchRecordingArea.setOnTouchListener(this)
        binding.startServerButton.setOnClickListener { startServer() }
        binding.stopServerButton.setOnClickListener { stopServer() }
    }

    private fun onNewGesture(gesture: Gesture?) {
        gesture?.also {
            Log.d(TAG, "Новый записанный жест: $gesture")
            lifecycleScope.launch(Dispatchers.IO) {
                ktorServer.send(gesture)
            }
        }
    }

    private fun startServer() {
        hideError()
        lifecycleScope.launch(Dispatchers.IO) {
            ktorServer.run(DEFAULT_SERVER_ADDRESS, DEFAULT_SERVER_PORT)
        }
    }

    private fun stopServer() {

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