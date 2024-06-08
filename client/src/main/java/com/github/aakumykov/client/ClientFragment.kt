package com.github.aakumykov.client

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.github.aakumykov.client.databinding.FragmentClientBinding
import com.github.aakumykov.common.showToast
import com.gitlab.aakumykov.exception_utils_module.ExceptionUtils
import kotlinx.coroutines.launch

class ClientFragment : Fragment(R.layout.fragment_client) {

    private var _binding: FragmentClientBinding? = null
    private val binding get() = _binding!!
    private var ktorClient: KtorClient? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentClientBinding.bind(view)

        binding.clientPrepareButton.setOnClickListener { prepareClient() }
    }

    private fun prepareClient() {
        hideError()

        lifecycleScope.launch {
            KtorClient().init(
                "192.168.0.171",
                8081,
                "/chat"
            )
                .onSuccess {
                    ktorClient = it
                    showToast("Клиент подготовлен")
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