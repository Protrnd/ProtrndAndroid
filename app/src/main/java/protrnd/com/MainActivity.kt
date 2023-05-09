package protrnd.com

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import protrnd.com.data.network.api.PostApi
import protrnd.com.data.network.api.ProfileApi
import protrnd.com.data.repository.HomeRepository
import protrnd.com.databinding.ActivityMainBinding
import protrnd.com.ui.base.BaseActivity
import protrnd.com.ui.handleUnCaughtException
import protrnd.com.ui.home.HomeActivity
import protrnd.com.ui.post.PostActivity
import protrnd.com.ui.startActivityFromNotification
import protrnd.com.ui.startNewActivityFromAuth
import protrnd.com.ui.viewmodels.HomeViewModel

class MainActivity : BaseActivity<ActivityMainBinding, HomeViewModel, HomeRepository>() {

    override fun onViewReady(savedInstanceState: Bundle?, intent: Intent?) {
        super.onViewReady(savedInstanceState, intent)
        val bundle = intent!!.extras
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            binding.root.handleUnCaughtException(e)
        }
        if (bundle != null && bundle.containsKey("post_id")) {
            bundle.putBoolean("isFromNotification", true)
            startActivityFromNotification(PostActivity::class.java, bundle)
        } else if (currentUserProfile.id.isNotEmpty()) {
            startNewActivityFromAuth(HomeActivity::class.java)
        }
    }

    override fun getActivityBinding(inflater: LayoutInflater) =
        ActivityMainBinding.inflate(inflater)

    override fun getViewModel() = HomeViewModel::class.java

    override fun getActivityRepository(): HomeRepository {
        val token = runBlocking { profilePreferences.authToken.first() }
        val api = protrndAPIDataSource.buildAPI(ProfileApi::class.java, token)
        val postsApi = protrndAPIDataSource.buildAPI(PostApi::class.java, token)
        return HomeRepository(api, postsApi)
    }
}