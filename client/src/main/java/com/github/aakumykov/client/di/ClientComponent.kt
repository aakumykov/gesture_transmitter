package com.github.aakumykov.client.di

import com.github.aakumykov.client.ClientFragment
import com.github.aakumykov.common.di.CommonComponent
import dagger.Subcomponent

@Subcomponent
interface ClientComponent {

    fun injectClientFragment(clientFragment: ClientFragment)

    fun getAppComponent(): CommonComponent
}