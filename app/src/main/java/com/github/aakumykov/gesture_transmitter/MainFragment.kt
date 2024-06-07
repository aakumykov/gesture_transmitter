package com.github.aakumykov.gesture_transmitter

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.github.aakumykov.client.ClientFragment
import com.github.aakumykov.gesture_transmitter.databinding.FragmentMainBinding
import com.github.aakumykov.server.ServerFragment

class MainFragment : Fragment(R.layout.fragment_main) {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMainBinding.bind(view)

        binding.serverButton.setOnClickListener { setFragment(ServerFragment.newInstance()) }
        binding.clientButton.setOnClickListener { setFragment(ClientFragment.newInstance()) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .addToBackStack(null)
            .replace(R.id.fragmentContainer, fragment, null)
            .commit()
    }

    companion object {
        fun newInstance(): MainFragment {
            return MainFragment()
        }
    }
}