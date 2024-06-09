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
import com.github.aakumykov.client.ktor_client.KtorClientState
import com.github.aakumykov.client.ktor_client.KtorStateProvider
import com.github.aakumykov.common.showToast
import com.gitlab.aakumykov.exception_utils_module.ExceptionUtils
import kotlinx.coroutines.launch


class ClientFragment : Fragment(R.layout.fragment_client) {

    private var _binding: FragmentClientBinding? = null
    private val binding get() = _binding!!


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentClientBinding.bind(view)

        lifecycleScope.launch {
            KtorStateProvider.state.collect(::onKtorClientStateChanged)
        }

        lifecycleScope.launch {
            KtorStateProvider.error.collect(::onKtorClientError)
        }

        binding.accessibilityServiceButton.setOnClickListener {
            requireContext().openAccessibilitySettings()
        }

        binding.startGoogleChromeButton.setOnClickListener {
            requireContext().packageManager
                ?.also {pm ->
                    pm.getLaunchIntentForPackage(GesturePlayingService.GOOGLE_CHROME_PACKAGE_NAME)
                        ?.also { chromeIntent ->
                            startActivity(chromeIntent)
                        } ?: showToast(R.string.google_chrome_not_found)
                } ?: showToast("Ошибка поиска Google Chrome")
        }
    }

    override fun onResume() {
        super.onResume()
        displayAccessibilityServiceState()
        binding.startGoogleChromeButton.isEnabled = isAccessibilityServiceEnabled()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun onKtorClientStateChanged(state: KtorClientState) {

        binding.clientStateView.setText(when(state) {
            KtorClientState.INACTIVE -> R.string.ktor_client_state_inactive
            KtorClientState.RUNNING -> R.string.ktor_client_state_running
            KtorClientState.PAUSED -> R.string.ktor_client_state_paused
            KtorClientState.STOPPED -> R.string.ktor_client_state_stopped
            KtorClientState.ERROR -> R.string.ktor_client_state_error
        })

        when(state) {
            KtorClientState.ERROR -> binding.clientErrorView.visibility = View.VISIBLE
            else -> binding.clientErrorView.visibility = View.GONE
        }
    }

    private fun onKtorClientError(e: Exception?) {
        e?.also {
            ExceptionUtils.getErrorMessage(e).also { errorMsg ->
                binding.clientErrorView.text = errorMsg
                Log.e(TAG, errorMsg, e)
            }
        }
    }


    private fun displayAccessibilityServiceState() {
        binding.accessibilityServiceButton.setText(getString(
            if (isAccessibilityServiceEnabled())
                R.string.button_acc_service_enabled
            else
                R.string.button_acc_service_disabled
        ))
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