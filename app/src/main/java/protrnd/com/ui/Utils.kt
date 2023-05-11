package protrnd.com.ui

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.app.Dialog
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.media.MediaMetadataRetriever
import android.net.*
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Parcelable
import android.text.*
import android.text.format.DateUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatToggleButton
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import protrnd.com.MainActivity
import protrnd.com.R
import protrnd.com.data.NetworkConnectionLiveData
import protrnd.com.data.models.*
import protrnd.com.data.models.Constants.EMPTY_GUID
import protrnd.com.data.models.Constants.FROM
import protrnd.com.data.models.Constants.RECEIVE
import protrnd.com.data.models.Constants.TOP_UP
import protrnd.com.data.network.MemoryCache
import protrnd.com.data.network.ProfilePreferences
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.network.api.PostApi
import protrnd.com.data.network.api.ProfileApi
import protrnd.com.data.network.resource.Resource
import protrnd.com.data.repository.HomeRepository
import protrnd.com.data.responses.BasicResponseBody
import protrnd.com.databinding.BottomSheetCommentsBinding
import protrnd.com.databinding.ConfirmationLayoutBinding
import protrnd.com.databinding.TransactionDetailsLayoutBinding
import protrnd.com.ui.adapter.*
import protrnd.com.ui.adapter.listener.ImagePostItemClickListener
import protrnd.com.ui.adapter.listener.PromoteSupportListener
import protrnd.com.ui.auth.AuthenticationActivity
import protrnd.com.ui.auth.LoginFragment
import protrnd.com.ui.base.BaseActivity
import protrnd.com.ui.home.HomeActivity
import protrnd.com.ui.post.ForwardPostBottomSheetDialog
import protrnd.com.ui.post.PostActivity
import protrnd.com.ui.profile.ProfileActivity
import protrnd.com.ui.support.SupportBottomSheet
import protrnd.com.ui.viewholder.PostsViewHolder
import protrnd.com.ui.viewmodels.HomeViewModel
import protrnd.com.ui.viewmodels.PaymentViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.Executors
import kotlin.math.abs

const val REQUEST_PERMISSION_CODE = 7192

fun View.handleUnCaughtException(e: Throwable) {
    Log.e("Unknown Throwable Error", Gson().toJson(e))
    this.errorSnackBar("An Error occurred!")
}

fun generateRef(): String {
    return UUID.randomUUID().toString()
}

fun RecyclerView.loadPageData(
    fm: FragmentManager,
    activity: Activity?,
    viewModel: HomeViewModel,
    lifecycleScope: CoroutineScope,
    context: Context,
    lifecycleOwner: LifecycleOwner,
    currentProfile: Profile,
    fragment: Fragment?,
    removeAlphaAction: (() -> Unit)?,
    showAlphaAction: (() -> Unit)?,
    token: String
) {
    val recyclerViewReadyCallback = object : RecyclerViewReadyCallback {
        override fun onLayoutReady() {
            NetworkConnectionLiveData(context).observe(lifecycleOwner) {
                (adapter as PostsPagingAdapter).setupRecyclerView(
                    activity,
                    viewModel,
                    lifecycleOwner,
                    lifecycleScope,
                    currentProfile,
                    context,
                    fm,
                    token,
                    fragment,
                    removeAlphaAction,
                    showAlphaAction
                )
            }
        }
    }

    this.viewTreeObserver.addOnGlobalLayoutListener(object :
        ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            recyclerViewReadyCallback.onLayoutReady()
            this@loadPageData.viewTreeObserver.removeOnGlobalLayoutListener(this)
        }
    })

    (adapter as PostsPagingAdapter).promoteSupportPost(object : PromoteSupportListener {
        override fun click(post: Post) {
            if (currentProfile.id == post.profileid) {
//                    val bottomSheetPromote = PromotionBottomSheet(this@HomeFragment, post.id)
//                    binding.alphaBg.visible(true)
//                    bottomSheetPromote.show(childFragmentManager, bottomSheetPromote.tag)
            } else {
                val bottomSheetSupport =
                    SupportBottomSheet(fragment, post = post, activity = activity)
                showAlphaAction.let {
                    it?.invoke()
                }
                bottomSheetSupport.show(fm, bottomSheetSupport.tag)
            }
        }
    })
}

private fun PostsPagingAdapter.setupRecyclerView(
    activity: Activity?,
    viewModel: HomeViewModel,
    lifecycleOwner: LifecycleOwner,
    lifecycleScope: CoroutineScope,
    currentUserProfile: Profile,
    context: Context,
    fragmentManager: FragmentManager,
    token: String,
    fragment: Fragment?,
    removeAlphaAction: (() -> Unit)?,
    showAlphaAction: (() -> Unit)?
) {
    this.setupRecyclerResults(object : PostsPagingAdapter.SetupRecyclerResultsListener {
        override fun setupLikes(holder: PostsViewHolder, postData: Post) {
            lifecycleScope.launch {
                setupPostLikes(holder, postData, viewModel, lifecycleOwner)
            }
        }

        override fun setupData(holder: PostsViewHolder, postData: Post) {
            lifecycleScope.launch {
                setupPosts(
                    holder,
                    postData,
                    activity,
                    currentUserProfile,
                    viewModel,
                    lifecycleOwner
                )
            }

            if (postData.profileid == currentUserProfile.id) {
                holder.view.promoteSupport.visibility = View.INVISIBLE
                holder.view.promoteSupport.isEnabled = false
            }

            holder.view.sendTextBtn.setOnClickListener {
                showAlphaAction.let {
                    if (it != null) {
                        it()
                    }
                }
                val forwardPostDialog = ForwardPostBottomSheetDialog(
                    currentUserProfile,
                    postData,
                    token,
                    fragment,
                    activity
                )
                forwardPostDialog.show(fragmentManager, forwardPostDialog.tag)
            }
        }

        override fun showCommentSection(postData: Post) {
            showAlphaAction.let {
                if (it != null) {
                    it()
                }
            }
            val bottomSheet = BottomSheetDialog(context, R.style.BottomSheetTheme)
            val bottomSheetBinding =
                BottomSheetCommentsBinding.inflate(LayoutInflater.from(context))
            bottomSheet.setContentView(bottomSheetBinding.root)
            bottomSheet.setCanceledOnTouchOutside(true)
            bottomSheetBinding.commentSection.layoutManager =
                LinearLayoutManager(context)
            bottomSheetBinding.commentsCount.text =
                bottomSheetBinding.commentsCount.text.toString().setSpannableColor(
                    bottomSheetBinding.commentsCount.text.toString().replace("Comments", ""), 8
                )
            val timeAgo = postData.time.getAgo()
            bottomSheetBinding.timeAgo.text = timeAgo

            bottomSheet.setOnCancelListener {
                removeAlphaAction.let {
                    if (it != null) {
                        it()
                    }
                }
            }
            bottomSheet.setOnDismissListener {
                removeAlphaAction.let {
                    if (it != null) {
                        it()
                    }
                }
            }
            val profileFromComment = MutableLiveData<Profile>()
            val profileLive: LiveData<Profile> = profileFromComment
            var profileData = Profile()
            profileLive.observe(lifecycleOwner) { profileResult ->
                profileData = profileResult
                val postedBy = "Posted by ${profileData.username}"
                bottomSheetBinding.postedBy.text = postedBy

                bottomSheetBinding.showCommentSection(
                    viewModel,
                    lifecycleOwner,
                    lifecycleScope,
                    profileData,
                    currentUserProfile,
                    postData.identifier
                )
            }

            val commentRecyclerViewReadyCallback = object : RecyclerViewReadyCallback {
                override fun onLayoutReady() {
                    val cachedProfile = MemoryCache.profiles[postData.profileid]
                    if (cachedProfile != null) {
                        val profile: Profile = cachedProfile
                        profileFromComment.postValue(profile)
                    }
                    NetworkConnectionLiveData(context).observe(
                        lifecycleOwner
                    ) {
                        val executor = Executors.newFixedThreadPool(5)
                        executor.execute {
                            lifecycleScope.launch {
                                val profileResult =
                                    getOtherProfile(postData.profileid, viewModel, lifecycleOwner)
                                if (profileResult != null && profileResult != profileData) {
                                    profileData = profileResult
                                    profileFromComment.postValue(profileData)
                                }
                            }
                        }
                    }
                }
            }

            bottomSheetBinding.commentSection.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    commentRecyclerViewReadyCallback.onLayoutReady()
                    bottomSheetBinding.commentSection.viewTreeObserver.removeOnGlobalLayoutListener(
                        this
                    )
                }
            })
            bottomSheet.show()
        }

        override fun like(holder: PostsViewHolder, postData: Post) {
            val executor = Executors.newFixedThreadPool(5)
            executor.execute {
                lifecycleScope.launch {
                    likePost(
                        currentUserProfile,
                        holder,
                        postData,
                        activity,
                        lifecycleScope,
                        viewModel,
                        lifecycleOwner
                    )
                }
            }
        }
    })
}

private suspend fun likePost(
    currentProfile: Profile,
    holder: PostsViewHolder,
    postData: Post,
    activity: Activity?,
    lifecycleScope: CoroutineScope,
    viewModel: HomeViewModel,
    lifecycleOwner: LifecycleOwner
) {
    val profileResult = getOtherProfile(postData.profileid, viewModel, lifecycleOwner)
    if (profileResult != null) {
        val liked = holder.view.likeToggle.isChecked
        if (activity != null)
            likePost(
                holder.view.likeToggle,
                holder.view.likesCount,
                viewModel,
                postData.identifier,
                profileResult,
                currentProfile
            )
    }
}

private suspend fun getOtherProfile(
    id: String,
    viewModel: HomeViewModel,
    lifecycleOwner: LifecycleOwner
): Profile? {
    return getStoredProfile(id, viewModel, lifecycleOwner) ?: when (val otherProfile =
        viewModel.getProfileById(id)) {
        is Resource.Success -> {
            viewModel.saveProfile(otherProfile.value.data)
            MemoryCache.profiles[id] = otherProfile.value.data
            return otherProfile.value.data
        }
        is Resource.Loading -> {
            return null
        }
        is Resource.Failure -> {
            return null
        }
        else -> {
            return null
        }
    }
}

private fun getStoredProfile(
    id: String,
    viewModel: HomeViewModel,
    lifecycleOwner: LifecycleOwner
): Profile? {
    val profileResult = MemoryCache.profiles[id]
    if (profileResult != null)
        return profileResult
    val otherProfile = viewModel.getProfile(id)
    var result: Profile? = null
    otherProfile?.asLiveData()?.observe(lifecycleOwner) {
        if (it != null) {
            result = it
        }
    }
    return result
}

suspend fun setupPostLikes(
    holder: PostsViewHolder,
    postData: Post,
    viewModel: HomeViewModel,
    lifecycleOwner: LifecycleOwner
) {
    viewModel.setupLikes(
        postData.id,
        holder.view.likesCount,
        holder.view.likeToggle,
        lifecycleOwner
    )
}

suspend fun setupPosts(
    holder: PostsViewHolder,
    postData: Post,
    activity: Activity?,
    currentProfile: Profile,
    viewModel: HomeViewModel,
    lifecycleOwner: LifecycleOwner
) {
    val profileResult = getOtherProfile(postData.profileid, viewModel, lifecycleOwner)
    if (profileResult != null) {
        holder.bind(
            activity as AppCompatActivity,
            postData,
            profileResult,
            currentProfile,
            viewModel
        )
    }
}

fun <A : Activity> Activity.startNewActivityWithNoBackstack(activity: Class<A>) {
    Intent(
        this,
        activity
    ).also {
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(it)
        startAnimation()
    }
}

fun BaseActivity<*, *, *>.logout() {
    CoroutineScope(Dispatchers.IO).launch {
        profilePreferences.logoutProfile()
    }
    Intent(this, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(this)
        startAnimation()
    }
}

fun <A : Activity> Activity.startActivityFromNotification(activity: Class<A>, extras: Bundle) {
    Intent(
        this,
        activity
    ).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        putExtras(extras)
        startActivity(this)
    }
}

fun Context.isServiceRunning(serviceClass: Class<*>): Boolean {
    val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager?
    for (service in manager!!.getRunningServices(Int.MAX_VALUE)) {
        if (serviceClass.name == service.service.className) {
            return true
        }
    }
    return false
}

fun Activity.finishActivity() {
    finish()
    startAnimation()
}

fun Activity.startAnimation() {
    overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
}

fun String.setSpannableBold(section: String, start: Int = 0): Spannable {
    val span: Spannable =
        SpannableString(this)
    span.setSpan(
        StyleSpan(Typeface.BOLD),
        start,
        start + section.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    return span
}

suspend fun HomeViewModel.getProfile() {
    this.getCurrentProfile()
}

fun saveAndStartHomeFragment(
    token: String,
    scope: CoroutineScope,
    activity: Activity,
    preferences: ProfilePreferences
) {
    val api = ProtrndAPIDataSource().buildAPI(ProfileApi::class.java, token)
    val postsApi = ProtrndAPIDataSource().buildAPI(PostApi::class.java, token)
    val lvm = HomeViewModel(HomeRepository(api, postsApi))

    scope.launch {
        when (val profileResponse = lvm.getCurrentProfile()) {
            is Resource.Success -> {
                scope.launch {
                    preferences.saveAuthToken(token)
                    preferences.saveProfile(profileResponse.value.data)
                    activity.startNewActivityWithNoBackstack(HomeActivity::class.java)
                }
            }
            is Resource.Failure -> {
                if (profileResponse.isNetworkError)
                    Toast.makeText(
                        activity.applicationContext,
                        "Network error occurred",
                        Toast.LENGTH_SHORT
                    ).show()
            }
            else -> {
            }
        }
    }
}

fun TextView.setGradient() {
    val paint = this.paint
    val width = paint.measureText(this.text.toString())
    val textShader: Shader = LinearGradient(
        0f, 0f, width, this.textSize, intArrayOf(
            Color.parseColor("#170246"),
            Color.parseColor("#1F0342"),
            Color.parseColor("#38073A"),
            Color.parseColor("#600D2C"),
            Color.parseColor("#961519"),
            Color.parseColor("#CA1E07")
        ), null, Shader.TileMode.CLAMP
    )

    this.paint.shader = textShader
}

fun String.setSpannableColor(section: String, start: Int = 0): Spannable {
    val span: Spannable =
        SpannableString(this)
    span.setSpan(
        StyleSpan(Typeface.BOLD),
        start,
        start + section.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    span.setSpan(
        ForegroundColorSpan(Color.RED),
        start,
        start + section.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    return span
}

suspend fun HomeViewModel.setupLikes(
    postId: String,
    likesCountTv: TextView,
    likeToggle: AppCompatToggleButton,
    lifecycleOwner: LifecycleOwner
) {
    val likesMutable = MutableLiveData<Int>()
    val postLikesCount: LiveData<Int> = likesMutable

    val likedMutable = MutableLiveData<Boolean>()
    val likedPosts: LiveData<Boolean> = likedMutable

    val postLikesCached = MemoryCache.postLikes[postId]
    var cachedValue = 0
    if (postLikesCached != null) {
        cachedValue = postLikesCached
        likesMutable.postValue(cachedValue)
    }

    val isLikedCache = MemoryCache.likedPosts.contains(postId)
    likedMutable.postValue(isLikedCache)

    CoroutineScope(Dispatchers.IO).launch {
        when (val likesCount = this@setupLikes.getLikesCount(postId)) {
            is Resource.Success -> {
                val count = likesCount.data!!.data as Double
                val value = count.toInt()
                if (cachedValue < value) {
                    likesMutable.postValue(value)
                    MemoryCache.postLikes[postId] = value
                }
            }
            is Resource.Loading -> {

            }
            else -> {}
        }

        when (val isLiked = this@setupLikes.postIsLiked(postId)) {
            is Resource.Success -> {
                val liked = isLiked.data!!.data
                if (isLikedCache != liked) {
                    likedMutable.postValue(liked)
                    if (liked)
                        MemoryCache.likedPosts.add(postId)
                    else {
                        if (MemoryCache.likedPosts.contains(postId))
                            MemoryCache.likedPosts.remove(postId)
                    }
                }
            }
            is Resource.Loading -> {
            }
            else -> {}
        }
    }

    withContext(Dispatchers.Main) {
        postLikesCount.observe(lifecycleOwner) { like ->
            val likes =
                if (like > 1) "${
                    like.formatAmount()
                } trndrs like this post" else if (like == 1) "$like trndr likes this post" else "Be the first trndr to like this post"

            likesCountTv.text = likes
        }

        likedPosts.observe(lifecycleOwner) { isLiked ->
            likeToggle.isChecked = isLiked
        }
    }


}

fun sendCommentNotification(otherProfile: Profile, currentProfile: Profile, id: String) {
    val title = "Hi ${otherProfile.username}"
    val body = "${currentProfile.username} just commented on your post"
    val notificationData = NotificationData(currentProfile.username, "Post", id, body)
    sendNotification(otherProfile, title, notificationData)
}

fun sendLikeNotification(otherProfile: Profile, currentProfile: Profile, id: String) {
    val title = "Hi ${otherProfile.username}"
    val body = "${currentProfile.username} just liked your post"
    val notificationData = NotificationData(currentProfile.username, "Post", id, body)
    sendNotification(otherProfile, title, notificationData)
}

fun sendUploadNotification(currentProfile: Profile, id: String) {
    val title = "Hi ${currentProfile.username}"
    val body = if (id.isNotEmpty())
        "Your upload was successfully added"
    else
        "Error uploading"
    val notificationData = NotificationData(currentProfile.username, "Upload", id, body)
    sendNotification(currentProfile, title, notificationData)
}

fun sendFollowNotification(otherProfile: Profile, currentProfile: Profile) {
    val title = "Hi ${otherProfile.username}"
    val body = "${currentProfile.username} just followed you"
    val notificationData = NotificationData(currentProfile.username, "Follow", "", body)
    sendNotification(otherProfile, title, notificationData)
}

fun sendNotification(
    profile: Profile,
    title: String,
    notificationData: NotificationData
) {
    ProtrndAPIDataSource().getClients().sendNotification(
        PushNotification(
            NotificationPayload(title, notificationData.body),
            "/topics/${profile.identifier}",
            notificationData
        )
    ).enqueue(object : Callback<PushNotification> {
        override fun onResponse(
            call: Call<PushNotification>,
            response: Response<PushNotification>
        ) {
        }

        override fun onFailure(call: Call<PushNotification>, t: Throwable) {
        }
    })
}

fun likePost(
    likeToggle: AppCompatToggleButton,
    likesCount: TextView,
    viewModel: HomeViewModel,
    postId: String,
    otherProfile: Profile,
    currentUserProfile: Profile
) {
    val liked = likeToggle.isChecked
    val likesText = likesCount.text.toString().split(" ")
    val count = likesText[0].toIntOrNull()
    val text: String = if (liked) {
        if (count != null) {
            val number = count + 1
            "${number.formatAmount()} trndrs like this post"
        } else {
            "1 trndr likes this post"
        }
    } else {
        if (count != null && count > 1) {
            val number = count - 1
            "${number.formatAmount()} ${if (number == 1) "trndr" else "trndrs"} like this post"
        } else {
            "Be the first trndr to like this post"
        }
    }
    likesCount.text = text
    CoroutineScope(Dispatchers.IO).launch {
        if (liked) {
            when (viewModel.likePost(postId)) {
                is Resource.Success -> {
                    withContext(Dispatchers.Main) {
                        if (otherProfile != currentUserProfile)
                            sendLikeNotification(
                                otherProfile,
                                currentUserProfile,
                                postId
                            )
                    }

                }
                else -> {}
            }
        } else {
            when (viewModel.unlikePost(postId)) {
                is Resource.Success -> {

                }
                else -> {}
            }
        }
    }
}

fun BottomSheetCommentsBinding.showCommentSection(
    viewModel: HomeViewModel,
    lifecycleOwner: LifecycleOwner,
    scope: CoroutineScope,
    otherProfile: Profile,
    currentProfile: Profile,
    postId: String
) {
    val commentsLiveData = MutableLiveData<List<Comment>>()
    val liveData: LiveData<List<Comment>> = commentsLiveData

    liveData.observe(lifecycleOwner) { comments ->
        if (comments.isNotEmpty()) {
            this.commentSection.visible(true)
            this.noCommentsTv.visible(false)
            val commentsText = "Comments \u2022 ${comments.size.formatAmount()}"
            this.commentsCount.text =
                commentsText.setSpannableColor(commentsText.replace("Comments", ""), 8)
            val commentAdapter = CommentsAdapter(
                viewModel = viewModel,
                comments = comments,
                lifecycleOwner = lifecycleOwner
            )
            commentAdapter.clickListener(object : CommentsAdapter.ClickListener {
                override fun clickProfile(profileId: String) {
                    this@showCommentSection.root.context.startActivity(
                        Intent(
                            this@showCommentSection.root.context,
                            ProfileActivity::class.java
                        ).also { intent ->
                            intent.putExtra("profile_id", profileId)
                        })
                }
            })
            this.commentSection.adapter = commentAdapter
        }
    }

    try {
        val commentsCache = MemoryCache.commentsMap[postId]
        if (commentsCache != null) {
            val comments: List<Comment> = commentsCache
            commentsLiveData.postValue(comments)
        }

        viewModel.loadComments(postId)
        viewModel.comments.observe(lifecycleOwner) { comments ->
            when (comments) {
                is Resource.Success -> {
                    commentsLiveData.postValue(comments.value.data)
                    MemoryCache.commentsMap[postId] = comments.value.data
                }
                is Resource.Failure -> {
                    this.root.errorSnackBar("Error loading comments") {
                        viewModel.loadComments(postId)
                    }
                }
                else -> {}
            }
        }

        this.sendComment.setOnClickListener {
            val commentContent = this.commentInput.text.toString().trim()
            if (commentContent.isNotEmpty()) {
                this.sendComment.enable(false)
                val comment = CommentDTO(comment = commentContent, postid = postId)
                scope.launch {
                    when (val result = viewModel.addComment(comment)) {
                        is Resource.Success -> {
                            withContext(Dispatchers.Main) {
                                this@showCommentSection.sendComment.enable(true)
                                if (result.value.successful) {
                                    if (otherProfile != currentProfile)
                                        sendCommentNotification(
                                            otherProfile,
                                            currentProfile,
                                            postId
                                        )
                                    this@showCommentSection.commentInput.text.clear()
                                    viewModel.getComments(postId)
                                }
                            }
                        }
                        is Resource.Loading -> {
                            this@showCommentSection.sendComment.enable(false)
                        }
                        is Resource.Failure -> {
                            this@showCommentSection.sendComment.enable(true)
                        }
                    }
                }
            } else {
                this.commentInput.error = "This field cannot be empty"
            }
        }
    } catch (e: Exception) {
        throw e
    }
    this.commentsCount.text = this.commentsCount.text.toString()
        .setSpannableColor(this.commentsCount.text.toString().replace("Comments", ""), 8)
}

private fun HomeViewModel.loadComments(postId: String) {
    this.getComments(postId)
}

fun EditText.requestForFocus(next: EditText? = null, prev: EditText? = null) {
    this.addTextChangedListener {
        if (it.toString().length == 1 && next != null) {
            next.requestFocus()
        } else {
            if (it.toString().isEmpty())
                prev?.requestFocus()
        }
    }
}

fun getTimeWithCenterDot(time: String): String {
    return try {
        val sdf = LocalDateTime.parse(
            time,
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        )
        val sdfHour = sdf.hour + 1
        val hour = if (sdfHour > 12) sdfHour - 12 else sdfHour
        val minute = if (sdf.minute < 10) "0${sdf.minute}" else sdf.minute
        val am_pm = if (sdfHour > 12) "pm" else "am"
        "${time.getAgo()} \u2022 $hour:$minute $am_pm"
    } catch (t: Throwable) {
        "Error getting date"
    }
}

fun showTransactionDetails(
    context: Context,
    layoutInflater: LayoutInflater,
    transaction: Transaction,
    currentUserProfile: Profile,
    alphaBg: View,
    viewModel: PaymentViewModel,
    lifecycleOwner: LifecycleOwner
) {

    val mutableReceiver = MutableLiveData<Profile>()
    val receiver: LiveData<Profile> = mutableReceiver

    val mutableSender = MutableLiveData<Profile>()
    val sender: LiveData<Profile> = mutableSender
    val bottomSheetDialog = BottomSheetDialog(context, R.style.BottomSheetTheme)
    val bottomSheet = TransactionDetailsLayoutBinding.inflate(layoutInflater)
    bottomSheetDialog.setContentView(bottomSheet.root)
    bottomSheetDialog.setCanceledOnTouchOutside(true)

    bottomSheet.continueBtn.setOnClickListener {
        bottomSheetDialog.dismiss()
    }

    var amount = transaction.amount.formatAmount()
    if (transaction.purpose.contains("Support sent"))
        amount = "-$amount"
    if (amount.startsWith("-")) {
        amount = amount.replace("-", "")
        amount = "-₦$amount"
        bottomSheet.price.setTextColor(Color.parseColor("#FF0C08"))
    } else
        amount = "₦$amount"
    bottomSheet.price.text = amount
    bottomSheet.date.text = getTimeWithCenterDot(transaction.createdat)
    bottomSheet.transactId.text = transaction.trxref
    bottomSheet.purpose.text = transaction.purpose

    val errorText = "Error getting profile data"

    if (transaction.purpose.startsWith(TOP_UP))
        bottomSheet.fromToLayout.visible(false)

    sender.observe(lifecycleOwner) { senderProfile ->
        val profileName = "@${senderProfile.username}"
        bottomSheet.fromLocation.text = senderProfile.location
        bottomSheet.fromProfileName.text = senderProfile.fullname
        bottomSheet.fromProfileUsername.text = profileName
        if (transaction.profileid == EMPTY_GUID && senderProfile.id == transaction.profileid) {
            bottomSheet.fromProfileName.text = errorText
            bottomSheet.fromProfileUsername.visible(false)
            bottomSheet.fromLocation.visible(false)
        }
        if (transaction.receiverid == EMPTY_GUID && senderProfile.id == transaction.receiverid) {
            bottomSheet.fromProfileName.text = errorText
            bottomSheet.fromProfileUsername.visible(false)
            bottomSheet.fromLocation.visible(false)
        }
    }

    receiver.observe(lifecycleOwner) { receiverProfile ->
        val profileName = "@${receiverProfile.username}"
        bottomSheet.toLocation.text = receiverProfile.location
        bottomSheet.toProfileName.text = receiverProfile.fullname
        bottomSheet.toProfileUsername.text = profileName
        if (transaction.receiverid == EMPTY_GUID && receiverProfile.id == transaction.receiverid) {
            bottomSheet.toProfileName.text = errorText
            bottomSheet.toProfileUsername.visible(false)
            bottomSheet.toLocation.visible(false)
        }
        if (transaction.profileid == EMPTY_GUID && receiverProfile.id == transaction.profileid) {
            bottomSheet.toProfileName.text = errorText
            bottomSheet.toProfileUsername.visible(false)
            bottomSheet.toLocation.visible(false)
        }
    }

    if (transaction.purpose.startsWith(RECEIVE) || transaction.purpose.contains(FROM)) {
        val split = transaction.purpose.replace("@","").split(" ")
        val profileName = split[split.size-1]
        var senderProfile: Profile
        val cachedSenderProfile = MemoryCache.profilesByName[profileName]
        mutableReceiver.postValue(currentUserProfile)
        if (cachedSenderProfile != null) {
            senderProfile = cachedSenderProfile
            mutableSender.postValue(senderProfile)
        }
        CoroutineScope(Dispatchers.IO).launch {
            when (val profileResult =
                viewModel.getProfileByName(profileName)) {
                is Resource.Success -> {
                    val profile = profileResult.value.data[0]
                    senderProfile = profile
                    MemoryCache.profilesByName[profileName] = senderProfile
                    mutableSender.postValue(senderProfile)
                }
                else -> {}
            }
        }
    } else {
        val cachedReceiverProfile = MemoryCache.profiles[transaction.receiverid]
        var receiverProfile: Profile
        mutableSender.postValue(currentUserProfile)
        if (cachedReceiverProfile != null) {
            receiverProfile = cachedReceiverProfile
            mutableReceiver.postValue(receiverProfile)
        }
        CoroutineScope(Dispatchers.IO).launch {
            when (val profileResult =
                viewModel.getProfileById(transaction.receiverid)) {
                is Resource.Success -> {
                    val profile = profileResult.value.data
                    receiverProfile = profile
                    MemoryCache.profiles[transaction.receiverid] = receiverProfile
                    mutableReceiver.postValue(receiverProfile)
                }
                else -> {}
            }
        }
    }
    bottomSheetDialog.show()
    bottomSheetDialog.setOnDismissListener {
        alphaBg.visible(false)
    }
}

fun bindPostDetails(
    tabLayout: TabLayout,
    fullnameTv: TextView,
    locationTv: TextView,
    captionTv: TextView,
    post: Post,
    profileImage: ImageView,
    imagesPager: ViewPager2,
    postOwnerProfile: Profile?,
    currentProfile: Profile,
    timeText: TextView,
    activity: AppCompatActivity,
    viewModel: ViewModel? = null
) {
    if (postOwnerProfile != null) {
        fullnameTv.text = postOwnerProfile.fullname
        val caption = postOwnerProfile.username + " ${post.caption}"
        if (postOwnerProfile.acctype == activity.getString(R.string.business))
            fullnameTv.setCompoundDrawables(
                null,
                null,
                ContextCompat.getDrawable(
                    activity.applicationContext,
                    R.drawable.business_badge_ic
                ),
                null
            )
        captionTv.text = caption
        captionTv.setTags(postOwnerProfile.username, activity)

        if (postOwnerProfile.profileimg.isNotEmpty()) {
            Glide.with(profileImage)
                .load(postOwnerProfile.profileimg)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .circleCrop()
                .into(profileImage)
        }

        profileImage.setOnClickListener {
            if (postOwnerProfile != currentProfile) {
                activity.startActivity(Intent(activity, ProfileActivity::class.java).apply {
                    putExtra("profile_id", postOwnerProfile.id)
                })
                activity.startAnimation()
            }
        }

        captionTv.setOnClickListener {
            it.context.startActivity(Intent(activity, PostActivity::class.java).apply {
                putExtra("post_id", post.id)
            })
            activity.startAnimation()
        }
    }

    val location = post.location.city + ", " + post.location.state + " State"
    locationTv.text = location
    val time = post.time.getAgo()
    timeText.text = time

    //Setup images adapter
    imagesPager.clipChildren = false
    imagesPager.clipToPadding = false
    imagesPager.getChildAt(0).overScrollMode = View.OVER_SCROLL_NEVER
    val adapter = PostImagesAdapter(
        activity = activity,
        images = post.uploadurls,
        viewModel = viewModel,
        post = post,
        currentProfile = currentProfile
    )
    imagesPager.adapter = adapter

    val transformer = CompositePageTransformer()
    transformer.addTransformer(MarginPageTransformer(10))
    imagesPager.setPageTransformer(transformer)

    TabLayoutMediator(tabLayout, imagesPager) { _, _ -> }.attach()
}

fun Int.formatAmount(): String {
    return NumberFormat.getNumberInstance(Locale.US).format(this)
}

fun Double.formatAmount(): String {
    return NumberFormat.getNumberInstance(Locale.US).format(this)
}
fun String.formatAmount(): String {
    val amount = this.toDouble()
    val formatter = DecimalFormat("#,###.00")
    return formatter.format(amount)
}

private fun TextView.setTags(section: String, activity: Activity) {
    val pTagString: String = this.text.toString().trim()
    val string = SpannableString(pTagString)
    string.setSpan(
        StyleSpan(Typeface.BOLD),
        0,
        0 + section.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    var start = -1
    var i = 0
    while (i < pTagString.length) {
        if (pTagString[i] == '@' || pTagString[i] == '#') {
            start = i
        } else if (pTagString[i] == ' '
            || i == pTagString.length - 1 && start != -1
        ) {
            if (start != -1) {
                if (i == pTagString.length - 1) {
                    i++ // case for if hash is last word and there is no
                    // space after word
                }
                val tag = pTagString.substring(start, i)
                string.setSpan(object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        if (tag.startsWith("@")) {
                            Intent(this@setTags.context, ProfileActivity::class.java).apply {
                                putExtra("profile_name", tag)
                                this@setTags.context.startActivity(this)
                                activity.startAnimation()
                            }
                        }
                        if (tag.startsWith("#")) {
                            Intent(this@setTags.context, HashTagResultsActivity::class.java).apply {
                                putExtra("hashtag", tag)
                                this@setTags.context.startActivity(this)
                                activity.startAnimation()
                            }
                        }
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        if (tag.contains("@")) ds.color =
                            Color.parseColor("#E02A45") else ds.color =
                            Color.parseColor("#ed6057")
                        ds.isUnderlineText = false
                    }
                }, start, i, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                start = -1
            }
        }
        i++
    }
    this.movementMethod = LinkMovementMethod.getInstance()
    this.text = string
}

fun showConfirmationDialog(
    inflater: LayoutInflater,
    negativeText: String,
    positiveText: String,
    title: String,
    action: (() -> Unit)?
) {
    val dialog = Dialog(inflater.context)
    val binding = ConfirmationLayoutBinding.inflate(inflater)
    dialog.setContentView(binding.root)
    binding.negativeBtn.text = negativeText
    binding.positiveBtn.text = positiveText
    binding.questionTv.text = title
    binding.negativeBtn.setOnClickListener {
        dialog.dismiss()
    }
    binding.positiveBtn.setOnClickListener {
        dialog.dismiss()
        action.let {
            if (it != null) {
                it()
            }
        }
    }
    dialog.show()
    val window: Window = dialog.window!!
    window.setLayout(MATCH_PARENT, WRAP_CONTENT)
}

fun RecyclerView.addThumbnailGrid4(context: Context, thumbnails: List<Post>) {
    val gridlayout = GridLayoutManager(context, 4)
    this.layoutManager = gridlayout
    val thumbnailsAdapter = ImageThumbnailPostAdapter(thumbnails)
    thumbnailsAdapter.stateRestorationPolicy =
        RecyclerView.Adapter.StateRestorationPolicy.PREVENT
    thumbnailsAdapter.imageClickListener(object : ImagePostItemClickListener {
        override fun postItemClickListener(post: Post) {
            context.startActivity(Intent(context, PostActivity::class.java).apply {
                this.putExtra("post_id", post.identifier)
            })
        }
    })
    this.adapter = thumbnailsAdapter
}

fun RecyclerView.addThumbnailGrid3(context: Context, thumbnails: ArrayList<String>) {
    val gridlayout = GridLayoutManager(context, 3)
    this.layoutManager = gridlayout
    val thumbnailsAdapter = ImageVideoResultAdapter(thumbnails)
    thumbnailsAdapter.stateRestorationPolicy =
        RecyclerView.Adapter.StateRestorationPolicy.PREVENT
    thumbnailsAdapter.imageClickListener(object : ImagePostItemClickListener {
        override fun postItemClickListener(post: Post) {
            context.startActivity(Intent(context, PostActivity::class.java).apply {
                this.putExtra("post_id", post.identifier)
            })
        }
    })
    this.adapter = thumbnailsAdapter
}

fun View.errorSnackBar(message: String, action: (() -> Unit)? = null) {
    val snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG)
    snackbar.setBackgroundTint(Color.RED)
    action.let {
        if (it != null) {
            snackbar.setAction("retry") { it() }
        }
    }

    val textView =
        snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
    textView.setCompoundDrawablesWithIntrinsicBounds(
        R.drawable.retry_ic,
        0,
        0,
        0
    )
    textView.textSize = 15f
    textView.gravity = Gravity.CENTER_VERTICAL
    textView.compoundDrawablePadding =
        resources.getDimensionPixelOffset(R.dimen.snackbar_icon_padding)
    snackbar.show()

    postDelayed({
        if (snackbar.isShown)
            snackbar.dismiss {
                if (action != null) {
                    action()
                }
            }
    }, 60000)
}

fun Snackbar.dismiss(action: (() -> Unit)? = null) {
    action.let {
        if (it != null) {
            it()
            this.dismiss()
        }
    }
}

fun View.visible(isVisible: Boolean) {
    visibility = if (isVisible) View.VISIBLE else View.GONE
}

fun View.enable(enabled: Boolean) {
    if (this is LinearLayoutCompat) {
        for (i in 0 until this.childCount) {
            val view = this.getChildAt(i)
            view.enable(enabled)
        }
    }
    isClickable = enabled
    isLongClickable = enabled
    isEnabled = enabled
    alpha = if (enabled) 1f else 0.5f
}

interface RecyclerViewReadyCallback {
    fun onLayoutReady()
}

fun Fragment.handleAPIError(failure: Resource.Failure, retry: (() -> Unit)? = null) {
    when {
        failure.isNetworkError -> requireView().errorSnackBar(
            "Please check your internet connection",
            retry
        )
        failure.errorCode == 401 -> {
            if (this is LoginFragment) {
                requireView().errorSnackBar("You've entered incorrect email or password")
            } else {
                Intent(requireContext(), AuthenticationActivity::class.java).also {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(it)
                }
            }
        }
        else -> {
            val error = Gson().fromJson(failure.errorBody?.string(), BasicResponseBody::class.java)
            if (this is LoginFragment && failure.errorCode == 404) {
                requireView().errorSnackBar("Account does not exist please sign up!")
            } else {
                requireView().errorSnackBar(error.message)
            }
        }
    }
}

fun Activity.checkStoragePermissions() {
    if (ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        ActivityCompat.requestPermissions(
            this,
            listOf(Manifest.permission.READ_EXTERNAL_STORAGE).toTypedArray(),
            REQUEST_PERMISSION_CODE
        )
    }
}

fun Context.isVideoFile(uri: Uri): Boolean {
    val retriever = MediaMetadataRetriever()
    try {
        retriever.setDataSource(this, uri)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    val hasVideo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO)
    return "yes" == hasVideo
}

fun Context.getFileTypes(uris: List<Uri>): List<String> {
    val uriFiles = mutableListOf<String>()
    for (uri in uris) {
        if (this.isVideoFile(uri)) {
            uriFiles.add("mp4")
        } else {
            uriFiles.add("png")
        }
    }
    return uriFiles
}

fun String.formatNumber(): String {
    return if (abs(this.toInt() / 1000000) > 1) {
        (this.toInt() / 1000000).toString() + "M"
    } else if (abs(this.toInt() / 1000) > 1) {
        (this.toInt() / 1000).toString() + "K"
    } else {
        this.toInt().toString()
    }
}

fun String.getAgo(): CharSequence {
    val pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    val sdf = SimpleDateFormat(pattern, Locale.ENGLISH)
    sdf.timeZone = TimeZone.getTimeZone(TimeZone.getDefault().toZoneId())
    return try {
        val now = System.currentTimeMillis()
        val time: Long = sdf.parse(this)?.time ?: now
        val millis = now - time
        val mins = millis / (1000 * 60) % 60
        val hours = millis / (1000 * 60 * 60)
        if (mins < 59 && (hours - 1) < 1)
            return "$mins min ago"
        val ago = DateUtils.getRelativeTimeSpanString(
            time,
            now - 60000L,
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE
        )
        ago
    } catch (e: ParseException) {
        ""
    }
}

fun View.hideSystemUI(window: Window) {
    // Set the IMMERSIVE flag.
    // Set the content to appear under the system bars so that the content
    // doesn't resize when the system bars hide and show.
    WindowCompat.setDecorFitsSystemWindows(window, false)
    WindowInsetsControllerCompat(window, this).let { controller ->
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.hide(WindowInsetsCompat.Type.navigationBars())
        controller.hide(WindowInsetsCompat.Type.statusBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}

// This snippet shows the system bars. It does this by removing all the flags
// except for the ones that make the content appear under the system bars.
fun View.showSystemUI(window: Window) {
    WindowCompat.setDecorFitsSystemWindows(window, true)
    WindowInsetsControllerCompat(window, this).show(WindowInsetsCompat.Type.systemBars())
}

fun Context.isNetworkAvailable(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val nw = connectivityManager.activeNetwork ?: return false
    val activeNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
    return when {
        activeNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        activeNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        else -> false
    }
}

fun Context.showFeatureComingSoonDialog() {
    val dialog = Dialog(this)
    val layout = R.layout.feature_coming
    dialog.setContentView(layout)
    dialog.show()
    val window: Window = dialog.window!!
    window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    window.setLayout(
        WRAP_CONTENT,
        WRAP_CONTENT
    )
}