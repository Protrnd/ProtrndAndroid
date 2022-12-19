package protrnd.com.ui.base

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import protrnd.com.data.ProfilePreferences
import protrnd.com.data.models.Profile
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.repository.BaseRepository
import protrnd.com.ui.checkStoragePermissions


abstract class BaseActivity<B : ViewBinding, VM : ViewModel, R : BaseRepository> :
    AppCompatActivity() {
    lateinit var viewModel: VM
    lateinit var profilePreferences: ProfilePreferences
    protected val protrndAPIDataSource = ProtrndAPIDataSource()
    lateinit var binding: B
    val profileHash = HashMap<String, Profile>()
    var currentUserProfile = Profile()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getActivityBinding(layoutInflater)
        setContentView(binding.root)
        this.checkStoragePermissions()
        FirebaseMessaging.getInstance().token.addOnCompleteListener {

        }
        profilePreferences = ProfilePreferences(this)
        val profileP = runBlocking { profilePreferences.profile.first() }
        if (profileP != null) {
            currentUserProfile = Gson().fromJson(profileP, Profile::class.java)
        }
        val factory = ViewModelFactory(getActivityRepository())
        viewModel = ViewModelProvider(this, factory)[getViewModel()]
        onViewReady(savedInstanceState, intent)
    }

    abstract fun getActivityBinding(inflater: LayoutInflater): B

    abstract fun getViewModel(): Class<VM>

    abstract fun getActivityRepository(): R

    @CallSuper
    protected open fun onViewReady(savedInstanceState: Bundle?, intent: Intent?) {
        //To be used by child activities
    }
}