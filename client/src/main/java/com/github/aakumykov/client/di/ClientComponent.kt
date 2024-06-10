package com.github.aakumykov.client.di

import com.github.aakumykov.client.ClientFragment
import dagger.Subcomponent

@Subcomponent
interface ClientComponent {
    fun injectClientFragment(clientFragment: ClientFragment)
}