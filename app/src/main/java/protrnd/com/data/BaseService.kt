package protrnd.com.data

import androidx.lifecycle.*

open class BaseService : LifecycleService(), ViewModelStoreOwner,
    HasDefaultViewModelProviderFactory {

    var vmStore = ViewModelStore()
    lateinit var factory: ViewModelProvider.Factory

    override fun getViewModelStore(): ViewModelStore {
        return vmStore
    }

    override fun onCreate() {
        super.onCreate()
        lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (source.lifecycle.currentState == Lifecycle.State.DESTROYED) {
                    vmStore.clear()
                    source.lifecycle.removeObserver(this)
                }
            }
        })
    }

    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory {
        factory = ViewModelProvider.AndroidViewModelFactory(application)
        return factory
    }
}