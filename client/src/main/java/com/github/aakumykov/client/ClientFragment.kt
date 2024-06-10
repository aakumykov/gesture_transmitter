package com.github.aakumykov.client

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.aakumykov.client.databinding.FragmentClientBinding
import com.github.aakumykov.client.extensions.isAccessibilityServiceEnabled
import com.github.aakumykov.client.extensions.openAccessibilitySettings
import com.github.aakumykov.client.gesture_player.GesturePlayingService
import com.github.aakumykov.client.ktor_client.GestureClient
import com.github.aakumykov.client.ktor_client.KtorClientState
import com.github.aakumykov.client.ktor_client.KtorStateProvider
import com.github.aakumykov.client.settings_provider.SettingsProvider
import com.github.aakumykov.common.showToast
import com.gitlab.aakumykov.exception_utils_module.ExceptionUtils
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random


class ClientFragment : Fragment(R.layout.fragment_client) {

    private var _binding: FragmentClientBinding? = null
    private val binding get() = _binding!!

    private val ktorClient: GestureClient by lazy {
        GestureClient.getInstance(Gson(), KtorStateProvider)
    }

    private val settingsProvider: SettingsProvider by lazy {
        SettingsProvider.getInstance(requireActivity().applicationContext)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentClientBinding.bind(view)
        subscribeToKtorClientState()
        prepareButtons()
    }

    private fun subscribeToKtorClientState() {
        lifecycleScope.launch {
            KtorStateProvider.state.collect(::onKtorClientStateChanged)
        }
        lifecycleScope.launch {
            KtorStateProvider.error.collect(::onKtorClientError)
        }
    }

    private fun prepareButtons() {

        binding.accessibilityServiceButton.setOnClickListener { requireContext().openAccessibilitySettings() }

        binding.startButton.setOnClickListener { onStartButtonClicked() }

        binding.pauseButton.setOnClickListener {}

        binding.finishButton.setOnClickListener { disconnectFromServer() }

        binding.requestFinishButton.setOnClickListener { requestDisconnectFromServer() }


        binding.testMessageButton.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                ktorClient.sendTextMessage("Привет от Клиента-${Random.nextInt(100)}")
            }
        }

        binding.closeMessageButton.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                ktorClient.sendCloseMessage()
            }
        }
    }

    private fun onFinishButtonClicked() {
        requestDisconnectFromServer()
    }

    private fun onStartButtonClicked() {
        connectToServer()
        /*requireContext().packageManager
            .getLaunchIntentForPackage(GesturePlayingService.GOOGLE_CHROME_PACKAGE_NAME)
            ?.also { startActivity(it) }
            ?: showToast(R.string.google_chrome_not_found)*/
    }


    private fun connectToServer() {
        when(ktorClient.currentState) {
            KtorClientState.CONNECTED -> { showToast(R.string.toast_already_connected) }
            KtorClientState.CONNECTING -> { showToast(R.string.toast_connecting_now) }
            KtorClientState.DISCONNECTING -> { showToast(R.string.toast_disconnecting_now) }
            else -> connectToServerReal()
        }
    }

    private fun connectToServerReal() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                ktorClient.connect(
                    settingsProvider.getIpAddress(),
                    settingsProvider.getPort(),
                    settingsProvider.getPath()
                )
            } catch (e: Exception) {
                showError(e)
            }
        }
    }


    private fun disconnectFromServer() {
        Log.d(TAG, "${TAG}: disconnectFromServer()")
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                ktorClient.disconnect()
            }
        }
    }


    // TODO: наблюдать за ходом отключения
    private fun requestDisconnectFromServer() {
        Log.d(TAG, "${TAG}: requestDisconnectFromServer()")
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                ktorClient.requestDisconnect()
            }
        }
    }


    override fun onResume() {
        super.onResume()
        displayAccessibilityServiceState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun onKtorClientStateChanged(state: KtorClientState) {
        displayClientState(state)
        showHideProgressBar(state)
        hideErrorIfNotError(state)
    }

    private fun hideErrorIfNotError(state: KtorClientState) {
        when(state) {
            KtorClientState.ERROR -> {}
            else -> hideError()
        }
    }

    private fun showHideProgressBar(state: KtorClientState) {
        when(state) {
            KtorClientState.CONNECTING -> showProgressBar()
            KtorClientState.DISCONNECTING -> showProgressBar()
            else -> hideProgressBar()
        }
    }

    private fun displayClientState(state: KtorClientState) {
        binding.clientStateView.setText(when(state) {
            KtorClientState.INACTIVE -> R.string.ktor_client_state_inactive
            KtorClientState.CONNECTING -> R.string.ktor_client_state_connecting
            KtorClientState.DISCONNECTING -> R.string.ktor_client_state_disconnecting
            KtorClientState.CONNECTED -> R.string.ktor_client_state_running
            KtorClientState.PAUSED -> R.string.ktor_client_state_paused
            KtorClientState.DISCONNECTED -> R.string.ktor_client_state_stopped
            KtorClientState.ERROR -> R.string.ktor_client_state_error
        })
    }

    private fun onKtorClientError(e: Exception?) {
        e?.also {
            ExceptionUtils.getErrorMessage(e).also { errorMsg ->
                showError(errorMsg)
                Log.e(TAG, ExceptionUtils.getErrorMessage(e), e);
            }
        }
    }

    private fun showError(e: Exception) {
        ExceptionUtils.getErrorMessage(e).also {
            showToast(it)
            Log.e(TAG, it, e)
        }
    }

    private fun showError(errorMsg: String) {
        binding.clientErrorView.apply {
            text = errorMsg
            visibility = View.VISIBLE
        }
    }

    private fun hideError() {
        binding.clientErrorView.apply {
            text = ""
            visibility = View.GONE
        }
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.INVISIBLE
    }


    private fun displayAccessibilityServiceState() {
        binding.accessibilityServiceButton.apply {
            setText(getString(
                if (isAccessibilityServiceEnabled()) R.string.button_acc_service_enabled
                else R.string.button_acc_service_disabled
            ))
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        return requireContext().isAccessibilityServiceEnabled(GesturePlayingService::class.java)
    }


    companion object {
        val TAG: String = ClientFragment::class.java.simpleName
        @JvmStatic
        fun newInstance(): ClientFragment {
            return ClientFragment()
        }
    }
}