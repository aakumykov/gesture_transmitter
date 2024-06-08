package com.github.aakumykov.server2

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.github.aakumykov.server2.databinding.FragmentServer2Binding

class Server2Fragment : Fragment(R.layout.fragment_server_2) {

    private var _binding: FragmentServer2Binding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentServer2Binding.bind(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): Server2Fragment {
            return Server2Fragment()
        }
    }
}