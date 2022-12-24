package protrnd.com.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import protrnd.com.data.models.Post
import protrnd.com.data.network.api.PostApi
import protrnd.com.data.network.api.ProfileApi
import protrnd.com.data.network.resource.Resource
import protrnd.com.data.repository.HomeRepository
import protrnd.com.databinding.ActivityHashTagResultsBinding
import protrnd.com.ui.adapter.PostsAdapter
import protrnd.com.ui.base.BaseActivity
import protrnd.com.ui.home.HomeViewModel

class HashTagResultsActivity :
    BaseActivity<ActivityHashTagResultsBinding, HomeViewModel, HomeRepository>() {
    private lateinit var postsAdapter: PostsAdapter
    private lateinit var postsLayoutManager: LinearLayoutManager
    private var isLoading: Boolean = false
    private var page = 1
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

        loadPage()

        binding.refreshLayout.setOnRefreshListener {
            if (binding.refreshLayout.isRefreshing) {
                binding.hashTaggedResultsRv.visible(false)
                page = 1
                loadPage()
                binding.refreshLayout.isRefreshing = false
            }
        }

        binding.hashTaggedResultsRv.setOnScrollChangeListener { _, _, _, _, _ ->
            if (!isLoading) {
                loadMoreItems(false)
                isLoading = true
            }
        }
    }

    private fun loadPage() {
        postsAdapter = PostsAdapter(
            viewModel = viewModel,
            lifecycleOwner = this,
            currentProfile = currentUserProfile,
            activity = this
        )
        //Load first page
        setupRecyclerView()
        loadMoreItems(true)
    }

    private fun loadMoreItems(isFirstPage: Boolean) {
        if (!isFirstPage) page += 1
        lifecycleScope.launch {
            when (val posts = viewModel.getPostsQueried(page, hashtag)) {
                is Resource.Success -> {
                    val result = posts.value.data
                    if (result.isEmpty())
                        return@launch
                    if (!isFirstPage) postsAdapter.addAll(result)
                    else postsAdapter.setList(result as MutableList<Post>)
                    isLoading = false
                    binding.progressBar.visible(false)
                    binding.hashTaggedResultsRv.visible(true)
                }
                is Resource.Loading -> {
                    isLoading = true
                }
                is Resource.Failure -> {
                    isLoading = false
                    binding.progressBar.visible(false)
                    handleAPIError(binding.root, posts) {
                        lifecycleScope.launch {
                            loadMoreItems(
                                isFirstPage
                            )
                        }
                    }
                }
                else -> {}
            }
        }
    }

    private fun setupRecyclerView() {
        binding.hashTaggedResultsRv.apply {
            adapter = postsAdapter
        }
    }

    override fun getViewModel() = HomeViewModel::class.java

    override fun getActivityBinding(inflater: LayoutInflater) =
        ActivityHashTagResultsBinding.inflate(inflater)

    override fun getActivityRepository(): HomeRepository {
        val token = runBlocking { profilePreferences.authToken.first() }
        val api = protrndAPIDataSource.buildAPI(ProfileApi::class.java, token)
        val postsApi = protrndAPIDataSource.buildAPI(PostApi::class.java, token)
        return HomeRepository(api, postsApi)
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