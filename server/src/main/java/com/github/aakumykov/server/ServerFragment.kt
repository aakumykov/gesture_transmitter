package com.github.aakumykov.server

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import com.github.aakumykov.server.databinding.FragmentServerBinding

class ServerFragment : Fragment(R.layout.fragment_server), View.OnTouchListener {

    private var _binding: FragmentServerBinding? = null
    private val binding get() = _binding!!
    private val gestureRecorder by lazy { GestureRecorder }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentServerBinding.bind(view)

        GestureServer.state.observe(viewLifecycleOwner) { isActive ->
            binding.serverStateView.setText(if (isActive) R.string.server_state_running else R.string.server_state_not_running)
        }

        GestureRecorder.recordedGesture.observe(viewLifecycleOwner) { newGesture ->
            Log.d(TAG, "Новый записанный жест: ${newGesture}")
            GestureServer.send(newGesture)
        }

        binding.touchRecordingArea.setOnTouchListener(this)
        binding.startServerButton.setOnClickListener { startServer() }
        binding.stopServerButton.setOnClickListener { stopServer() }
    }

    private fun startServer() {

    }

    private fun stopServer() {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when(event?.action) {
            MotionEvent.ACTION_DOWN -> gestureRecorder.startRecording(event)
            MotionEvent.ACTION_MOVE -> gestureRecorder.recordEvent(event)
            MotionEvent.ACTION_UP -> {
                gestureRecorder.finishRecording(event)
                Log.d("TAG", gestureRecorder.getLastRecord()?.toString() ?: "Нет записанных жестов")
            }
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