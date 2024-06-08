package com.github.aakumykov.client

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.github.aakumykov.client.databinding.FragmentClientBinding
import com.github.aakumykov.client.extensions.isAccessibilityServiceEnabled
import com.github.aakumykov.client.extensions.openAccessibilitySettings
import com.github.aakumykov.common.inMainThread
import com.github.aakumykov.common.showToast
import com.github.aakumykov.kotlin_playground.UserGesture
import com.gitlab.aakumykov.exception_utils_module.ExceptionUtils
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ClientFragment : Fragment(R.layout.fragment_client) {

    private var _binding: FragmentClientBinding? = null
    private val binding get() = _binding!!
    private var client: KtorClient? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentClientBinding.bind(view)

        displayAccessibilityServiceState()

        binding.accessibilityServiceButton.setOnClickListener { onAccessibilityButtonClicked() }
        binding.connectButton.setOnClickListener { connectToServer() }
        binding.disconnectButton.setOnClickListener { disconnectFromServer() }
        binding.readGesturesButton.setOnClickListener { readGesturesFromServer() }
    }

    private fun displayAccessibilityServiceState() {
        binding.accessibilityServiceButton.setText(getString(
            if (isAccessibilityServiceEnabled())
                R.string.button_acc_service_disabled
            else
                R.string.button_acc_service_enabled
        ))
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        return requireActivity().isAccessibilityServiceEnabled(ACCESSIBILITY_SERVICE_ID)
    }

    private fun onAccessibilityButtonClicked() {
        requireActivity().openAccessibilitySettings()
    }

    private fun readGesturesFromServer() {
        lifecycleScope.launch(Dispatchers.IO) { 
            client?.getGesturesFlow()?.collect { userGesture: UserGesture? ->
                Log.d(TAG, "Жест во фрагменте: $userGesture")
            } ?: inMainThread { showToast("Клиент не подключен") }
        }
    }

    private fun disconnectFromServer() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                inMainThread { hideError() }
                client?.disconnect()
                inMainThread { showToast(R.string.toast_disconnected_from_server) }
            } catch (e: Exception) {
                showError(e)
            }
        }
    }

    // TODO: показывать крутилку ожидания
    private fun connectToServer() {
        hideError()
        lifecycleScope.launch {
            KtorClient(Gson()).connect(
                "192.168.0.171",
                8081,
                "gestures"
            )
                .onSuccess {
                    client = it
                    showToast(R.string.toast_connected_to_server)
                }
                .onFailure {
                    showError(it)
                }
        }
    }

    private fun showError(throwable: Throwable) {
        binding.errorView.apply {
            text = ExceptionUtils.getErrorMessage(throwable)
            visibility = View.VISIBLE
        }
    }

    private fun hideError() {
        binding.errorView.apply {
            text = ""
            visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val ACCESSIBILITY_SERVICE_ID = requireContext().packageName + "/." + GesturePlayingService::class.simpleName

    companion object {
        val TAG: String = ClientFragment::class.java.simpleName
        @JvmStatic
        fun newInstance(): ClientFragment {
            return ClientFragment()
        }
    }
}