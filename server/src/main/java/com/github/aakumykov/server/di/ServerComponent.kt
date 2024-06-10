package com.github.aakumykov.server.di

import com.github.aakumykov.server.ServerFragment
import dagger.Subcomponent

@Subcomponent
interface ServerComponent {
    fun injectServerFragment(serverFragment: ServerFragment)
}