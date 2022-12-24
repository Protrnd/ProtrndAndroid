package protrnd.com.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.network.SettingsPreferences
import protrnd.com.data.repository.BaseRepository
import protrnd.com.ui.adapter.PostsAdapter
import protrnd.com.ui.handleUnCaughtException
import java.util.regex.Pattern

abstract class BaseFragment<VM : ViewModel, B : ViewBinding, R : BaseRepository> : Fragment() {
    protected lateinit var binding: B
    protected val protrndAPIDataSource = ProtrndAPIDataSource()
    protected lateinit var viewModel: VM
    lateinit var settingsPreferences: SettingsPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        settingsPreferences = SettingsPreferences(requireContext())
        binding = getFragmentBinding(inflater, container)
        lifecycleScope.launch { settingsPreferences.authToken.first() } //This automatically stores the information to memory
        val factory = ViewModelFactory(getFragmentRepository())
        viewModel = ViewModelProvider(this, factory)[getViewModel()]
        Thread.setDefaultUncaughtExceptionHandler { _, _ ->
            binding.root.handleUnCaughtException()
        }
        return binding.root
    }

    abstract fun getViewModel(): Class<VM>

    abstract fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?): B

    abstract fun getFragmentRepository(): R

    fun RecyclerView.setUpRecyclerView(adapter: PostsAdapter, layoutManager: LayoutManager) {
        this.layoutManager = layoutManager
        this.adapter = adapter
    }

    companion object {
        private val EMAIL_ADDRESS_PATTERN = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
        )
    }

    fun isValidEmail(str: String): Boolean {
        return EMAIL_ADDRESS_PATTERN.matcher(str).matches()
    }
}