package protrnd.com.ui.base

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import protrnd.com.data.ProfilePreferences
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.repository.BaseRepository


abstract class BaseActivity<B: ViewBinding, VM: ViewModel, R: BaseRepository>: AppCompatActivity() {
    lateinit var viewModel: VM
    lateinit var profilePreferences: ProfilePreferences
    protected val protrndAPIDataSource = ProtrndAPIDataSource()
    lateinit var binding: B

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getActivityBinding(layoutInflater)
        setContentView(binding.root)
        profilePreferences = ProfilePreferences(this)
        val factory = ViewModelFactory(getActivityRepository())
        viewModel = ViewModelProvider(this, factory)[getViewModel()]
        onViewReady(savedInstanceState, intent);
    }

    abstract fun getActivityBinding(inflater: LayoutInflater): B

    abstract fun getViewModel(): Class<VM>

    abstract fun getActivityRepository(): R

    @CallSuper
    protected open fun onViewReady(savedInstanceState: Bundle?, intent: Intent?) {
        //To be used by child activities
    }
}