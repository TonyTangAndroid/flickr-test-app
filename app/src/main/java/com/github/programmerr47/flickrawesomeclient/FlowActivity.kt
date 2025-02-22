package com.github.programmerr47.flickrawesomeclient

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.github.programmerr47.flickrawesomeclient.pages.search.SearchPhotoFragment
import com.github.programmerr47.flickrawesomeclient.util.commitTransaction
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.android.FragmentActivityInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance

class FlowActivity : AppCompatActivity(), FragmentActivityInjector {
    override val injector: KodeinInjector = KodeinInjector()
    override fun provideOverridingModule() = Kodein.Module {
        bind<AppCompatActivity>("activity") with instance(this@FlowActivity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flow)
        initializeInjector()

        supportFragmentManager.commitTransaction {
            replace(R.id.fragment_container, SearchPhotoFragment())
        }
    }

    override fun onDestroy() {
        destroyInjector()
        super.onDestroy()
    }
}
