package protrnd.com.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import protrnd.com.data.models.Post
import protrnd.com.data.models.Profile
import protrnd.com.data.network.api.PostApi
import protrnd.com.data.network.api.ProfileApi
import protrnd.com.data.network.resource.Resource
import protrnd.com.data.repository.HomeRepository
import protrnd.com.databinding.ActivityHashTagResultsBinding
import protrnd.com.ui.adapter.PostsPagingAdapter
import protrnd.com.ui.base.BaseActivity
import protrnd.com.ui.home.HomeViewModel
import protrnd.com.ui.viewholder.PostsViewHolder

class HashTagResultsActivity :
    BaseActivity<ActivityHashTagResultsBinding, HomeViewModel, HomeRepository>() {
    private lateinit var postsAdapter: PostsPagingAdapter
    private lateinit var postsLayoutManager: LinearLayoutManager
    private var hashtag: String = ""

    override fun onViewReady(savedInstanceState: Bundle?, intent: Intent?) {
        super.onViewReady(savedInstanceState, intent)
        if (intent != null) hashtag = intent.getStringExtra("hashtag").toString()

        setSupportActionBar(binding.toolbarResults)
        binding.toolbarResults.contentInsetStartWithNavigation = 0
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.title = hashtag
            lifecycleScope.launch {
                when (val count = viewModel.getQueryCount(hashtag)) {
                    is Resource.Success -> {
                        val postCount =
                            count.value.data.toString().replace(".0", "").toLong().toInt()
                        actionBar.subtitle =
                            if (postCount > 1) "$postCount posts" else "$postCount post"
                    }
                    else -> {}
                }
            }
        }

        postsLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.hashTaggedResultsRv.layoutManager = postsLayoutManager
        postsAdapter = PostsPagingAdapter()
        binding.hashTaggedResultsRv.adapter = postsAdapter

        setupStoredData()

        binding.refreshLayout.setOnRefreshListener {
            if (binding.refreshLayout.isRefreshing) {
                loadPage()
                binding.refreshLayout.isRefreshing = false
            }
        }
    }

    private fun loadPage() {
        //Load first page
        setupRecyclerView()
        viewModel.getPostsQueried(hashtag).observe(this) {
            postsAdapter.submitData(lifecycle, it)
        }
    }

    private fun getSoredProfile(id: String): Profile? {
        val otherProfile = viewModel.getProfile(id)
        var result: Profile? = null
        otherProfile?.asLiveData()?.observe(this) {
            if (it != null) {
                result = it
            }
        }
        return result
    }

    private suspend fun getOtherProfile(
        id: String
    ): Profile? {
        return getSoredProfile(id)
            ?: when (val otherProfile = viewModel.getProfileById(id)) {
                is Resource.Success -> {
                    viewModel.saveProfile(otherProfile.value.data)
                    return otherProfile.value.data
                }
                is Resource.Loading -> {
                    return null
                }
                is Resource.Failure -> {
                    if (otherProfile.isNetworkError) {
                        return getSoredProfile(id)
                    } else {
                        return null
                    }
                }
                else -> {
                    return null
                }
            }
    }

    private fun setupRecyclerView() {
        postsAdapter = binding.hashTaggedResultsRv.adapter as PostsPagingAdapter
        postsAdapter.setupRecyclerResults(object : PostsPagingAdapter.SetupRecyclerResultsListener {
            override fun setupLikes(holder: PostsViewHolder, postData: Post) {
                lifecycleScope.launch {
                    if (isNetworkAvailable())
                        viewModel.setupLikes(
                            postData.id,
                            holder.view.likesCount,
                            holder.view.likeToggle
                        )
                }
            }

            override fun setupData(holder: PostsViewHolder, postData: Post) {
                lifecycleScope.launch {
                    val profileResult = getOtherProfile(postData.profileid)
                    if (profileResult != null) {
                        holder.bind(
                            this@HashTagResultsActivity,
                            postData,
                            profileResult,
                            currentUserProfile
                        )
                    }
                }
            }

            override fun showCommentSection(postData: Post) {
                lifecycleScope.launch {
                    val profileResult = getOtherProfile(postData.profileid)
                    if (profileResult != null) {
                        showCommentSection(
                            viewModel,
                            this@HashTagResultsActivity,
                            lifecycleScope,
                            profileResult,
                            currentUserProfile,
                            postData.identifier
                        )
                    }
                }
            }

            override fun like(holder: PostsViewHolder, postData: Post) {
                lifecycleScope.launch {
                    val profileResult = getOtherProfile(postData.profileid)
                    if (profileResult != null) {
                        val liked = holder.view.likeToggle.isChecked
                        if (isNetworkAvailable())
                            likePost(
                                holder.view.likeToggle,
                                holder.view.likesCount,
                                lifecycleScope,
                                viewModel,
                                postData.identifier,
                                profileResult,
                                currentUserProfile
                            )
                        else
                            holder.view.likeToggle.isChecked = !liked
                    }
                }
            }
        })
    }

    private fun setupStoredData() {
        if (isNetworkAvailable())
            loadPage()
    }

    override fun getViewModel() = HomeViewModel::class.java

    override fun getActivityBinding(inflater: LayoutInflater) =
        ActivityHashTagResultsBinding.inflate(inflater)

    override fun getActivityRepository(): HomeRepository {
        val token = runBlocking { profilePreferences.authToken.first() }
        val api = protrndAPIDataSource.buildAPI(ProfileApi::class.java, token)
        val postsApi = protrndAPIDataSource.buildAPI(PostApi::class.java, token)
        val postDatabase = protrndAPIDataSource.providePostDatabase(application)
        val profileDatabase =
            protrndAPIDataSource.provideProfileDatabase(application)
        return HomeRepository(api, postsApi, postDatabase, profileDatabase)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finishActivity()
            }
        }
        return true
    }
}