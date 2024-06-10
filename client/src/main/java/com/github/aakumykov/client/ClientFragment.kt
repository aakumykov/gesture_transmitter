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
import com.github.aakumykov.client.ktor_client.ClientState
import com.github.aakumykov.client.ktor_client.KtorStateProvider
import com.github.aakumykov.client.settings_provider.SettingsProvider
import com.github.aakumykov.common.GOOGLE_CHROME_PACKAGE_NAME
import com.github.aakumykov.common.showToast
import com.gitlab.aakumykov.exception_utils_module.ExceptionUtils
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ClientFragment : Fragment(R.layout.fragment_client) {

    private var _binding: FragmentClientBinding? = null
    private val binding get() = _binding!!

    private val gestureClient: GestureClient by lazy {
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
            KtorStateProvider.state.collect(::onNewClientState)
        }
        lifecycleScope.launch {
            KtorStateProvider.error.collect(::onKtorClientError)
        }
    }

    private fun prepareButtons() {
        binding.accessibilityServiceButton.setOnClickListener {
            requireContext().openAccessibilitySettings() }
        binding.startButton.setOnClickListener { onStartButtonClicked() }
        binding.pauseButton.setOnClickListener { onPauseButtonClicked() }
        binding.finishButton.setOnClickListener { onFinishButtonClicked() }
    }

    private fun onPauseButtonClicked() {
        lifecycleScope.launch(Dispatchers.IO) {
            gestureClient.currentState.also { state ->
                when(state) {
                    ClientState.PAUSED -> gestureClient.resumeInteraction()
                    ClientState.CONNECTED -> gestureClient.pauseInteraction()
                    else -> Log.w(TAG, "Пауза/возобновление недоступны в статусе '$state'")
                }
            }
        }
    }

    private fun onFinishButtonClicked() {
        requestDisconnectFromServer()
    }

    private fun onStartButtonClicked() {
        if (gestureClient.isConnected())
            launchGoogleChrome()
        else
            connectToServer()
    }


    private fun connectToServer() {
        when(gestureClient.currentState) {
            ClientState.CONNECTED -> { showToast(R.string.toast_already_connected) }
            ClientState.CONNECTING -> { showToast(R.string.toast_connecting_now) }
            ClientState.DISCONNECTING -> { showToast(R.string.toast_disconnecting_now) }
            else -> connectToServerReal()
        }
    }

    private fun connectToServerReal() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                gestureClient.connect(
                    settingsProvider.getIpAddress(),
                    settingsProvider.getPort(),
                    settingsProvider.getPath()
                )
            } catch (e: Exception) {
                showError(e)
            }
        }
    }


    private fun requestDisconnectFromServer() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                gestureClient.requestDisconnection()
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


    private fun onNewClientState(state: ClientState) {
        displayClientState(state)
        updatePauseButton(state)
        showHideProgressBar(state)
        hideErrorIfNotError(state)
        launchGoogleChromeOnConnected(state)
    }

    private fun launchGoogleChromeOnConnected(state: ClientState) {
        if (ClientState.CONNECTED == state)
            launchGoogleChrome()
    }

    private fun launchGoogleChrome() {
        requireContext().packageManager
            .getLaunchIntentForPackage(GOOGLE_CHROME_PACKAGE_NAME)
            ?.also { startActivity(it) }
            ?: showToast(R.string.google_chrome_not_found)
    }

    private fun updatePauseButton(state: ClientState) {
        binding.pauseButton.setText(getString(
            when(state) {
                ClientState.PAUSED -> R.string.pause_button_resume
                else -> R.string.pause_button_pause
            }
        ))
    }

    private fun hideErrorIfNotError(state: ClientState) {
        when(state) {
            ClientState.ERROR -> {}
            else -> hideError()
        }
    }

    private fun showHideProgressBar(state: ClientState) {
        when(state) {
            ClientState.CONNECTING -> showProgressBar()
            ClientState.DISCONNECTING -> showProgressBar()
            else -> hideProgressBar()
        }
    }

    private fun displayClientState(state: ClientState) {
        binding.clientStateView.setText(when(state) {
            ClientState.INACTIVE -> R.string.ktor_client_state_inactive
            ClientState.CONNECTING -> R.string.ktor_client_state_connecting
            ClientState.DISCONNECTING -> R.string.ktor_client_state_disconnecting
            ClientState.CONNECTED -> R.string.ktor_client_state_running
            ClientState.PAUSED -> R.string.ktor_client_server_state_paused
            ClientState.DISCONNECTED -> R.string.ktor_client_state_disconnected
            ClientState.ERROR -> R.string.ktor_client_state_error
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