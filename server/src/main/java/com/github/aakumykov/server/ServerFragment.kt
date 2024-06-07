package com.github.aakumykov.server

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class ServerFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_server, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance(): ServerFragment {
            return ServerFragment()
        }
    }
}