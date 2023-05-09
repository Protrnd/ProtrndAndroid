package protrnd.com.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import protrnd.com.data.models.Post
import protrnd.com.data.network.MemoryCache
import protrnd.com.data.network.api.PostApi
import protrnd.com.data.network.api.ProfileApi
import protrnd.com.data.network.resource.Resource
import protrnd.com.data.repository.HomeRepository
import protrnd.com.databinding.ActivityHashTagResultsBinding
import protrnd.com.ui.adapter.PostsPagingAdapter
import protrnd.com.ui.base.BaseActivity
import protrnd.com.ui.viewmodels.HomeViewModel

class HashTagResultsActivity :
    BaseActivity<ActivityHashTagResultsBinding, HomeViewModel, HomeRepository>() {
    private lateinit var postsAdapter: PostsPagingAdapter
    private lateinit var postsLayoutManager: LinearLayoutManager
    private var hashtag: String = ""
    private val postCountMutable = MutableLiveData<Int>()
    private val postCountLive: LiveData<Int> = postCountMutable

    override fun onViewReady(savedInstanceState: Bundle?, intent: Intent?) {
        super.onViewReady(savedInstanceState, intent)
        if (intent != null) hashtag = intent.getStringExtra("hashtag").toString()
        setSupportActionBar(binding.toolbarResults)
        binding.toolbarResults.contentInsetStartWithNavigation = 0
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        postsLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.hashTaggedResultsRv.layoutManager = postsLayoutManager
        postsAdapter = PostsPagingAdapter()
        binding.hashTaggedResultsRv.adapter = postsAdapter

        setupStoredData()

        loadPage()

        binding.refreshLayout.setOnRefreshListener {
            loadPage()
            binding.refreshLayout.isRefreshing = false
        }

        binding.hashTaggedResultsRv.loadPageData(
            supportFragmentManager,
            this,
            viewModel,
            lifecycleScope,
            this,
            this,
            currentUserProfile,
            null,
            { binding.alphaBg.visible(false) },
            { binding.alphaBg.visible(true) },
            authToken!!
        )

        postCountLive.observe(this) { postCount ->
            binding.toolbarResults.title = hashtag
            binding.toolbarResults.subtitle =
                if (postCount > 1) "${postCount.formatAmount()} posts" else "$postCount post"
        }
    }

    private fun loadPage() {
        //Load first page
//        setupRecyclerView()
        viewModel.getPostsQueried(hashtag).observe(this) {
            lifecycleScope.launch {
                withContext(Dispatchers.Main) {
                    postsAdapter.loadStateFlow.collectLatest { loadStates ->
                        if (loadStates.refresh is LoadState.Loading) {
//                            binding.shimmer.visible(true)
                        } else {
//                            binding.shimmer.visible(false)
                            binding.refreshLayout.isRefreshing = false
                            if (postsAdapter.itemCount < 1) {
                                binding.root.errorSnackBar("Error loading hashtags") { loadPage() }
                            } else {
                                // TODO: Network error
                            }
                        }
                    }
                }
            }
            postsAdapter.submitData(lifecycle, it)
        }

        lifecycleScope.launch {
            when (val count = viewModel.getQueryCount(hashtag)) {
                is Resource.Success -> {
                    val postCount = count.value.data.toString().replace(".0", "").toLong().toInt()
                    MemoryCache.hashTagPostCount[hashtag] = postCount
                    postCountMutable.postValue(postCount)
                }
                else -> {}
            }
        }
    }

    private fun setupStoredData() {
        val result = MemoryCache.hashTagPosts[hashtag]
        if (result != null) {
            val posts: List<Post> = result
            postsAdapter.submitData(lifecycle, PagingData.from(posts))
            postCountMutable.postValue(posts.size)
        }
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

    override fun onDestroy() {
        super.onDestroy()
        MemoryCache.hashTagPosts[hashtag] = postsAdapter.snapshot().items
    }

    fun removeAlphaVisibility() {
        binding.alphaBg.visible(false)
    }
}