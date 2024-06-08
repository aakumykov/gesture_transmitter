package com.github.aakumykov.client

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.github.aakumykov.client.databinding.FragmentClientBinding
import com.github.aakumykov.common.inMainThread
import com.github.aakumykov.common.showToast
import com.gitlab.aakumykov.exception_utils_module.ExceptionUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ClientFragment : Fragment(R.layout.fragment_client) {

    private var _binding: FragmentClientBinding? = null
    private val binding get() = _binding!!
    private var client: KtorClient? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentClientBinding.bind(view)

        binding.connectButton.setOnClickListener { connectToServer() }
        binding.disconnectButton.setOnClickListener { disconnectFromServer() }
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
            KtorClient().connect(
                "192.168.0.171",
                8081,
                "chat"
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

    companion object {
        @JvmStatic
        fun newInstance(): ClientFragment {
            return ClientFragment()
        }
    }
}