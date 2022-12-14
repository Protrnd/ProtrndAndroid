package protrnd.com.ui

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.media.MediaMetadataRetriever
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.format.DateUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import protrnd.com.R
import protrnd.com.data.models.Post
import protrnd.com.data.models.Profile
import protrnd.com.data.network.Resource
import protrnd.com.data.responses.BasicResponseBody
import protrnd.com.databinding.ConfirmationLayoutBinding
import protrnd.com.ui.adapter.ImageThumbnailPostAdapter
import protrnd.com.ui.adapter.PostImagesAdapter
import protrnd.com.ui.auth.AuthenticationActivity
import protrnd.com.ui.auth.LoginFragment
import protrnd.com.ui.home.HomeViewModel
import protrnd.com.ui.profile.ProfileActivity
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

const val REQUEST_PERMISSION_CODE = 7192

fun <A : Activity> Activity.startNewActivityFromAuth(activity: Class<A>) {
    Intent(
        this,
        activity
    ).also {
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(it)
    }
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

fun ViewBinding.bindPostDetails(
    tabLayout: TabLayout,
    fullnameTv: TextView,
    usernameTv: TextView,
    locationTv: TextView,
    captionTv: TextView,
    post: Post,
    profileImage: ImageView,
    imagesPager: ViewPager2,
    postOwnerProfile: Profile,
    timeText: TextView
) {
    val username = "@${postOwnerProfile.username}"
    usernameTv.text = username
    fullnameTv.text = postOwnerProfile.fullname
    val location = post.location.cities[0] + ", " + post.location.state + " State"
    locationTv.text = location
    val time = post.time.getAgo()
    timeText.text = time
    val caption = postOwnerProfile.username + " ${post.caption}"
    captionTv.text = caption
    captionTv.setTags(postOwnerProfile.username)

    if (postOwnerProfile.profileimg.isNotEmpty()) {
        Glide.with(this.root)
            .load(postOwnerProfile.profileimg)
            .circleCrop()
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(profileImage)
    }

    //Setup images adapter
    imagesPager.clipChildren = false
    imagesPager.clipToPadding = false
    imagesPager.getChildAt(0).overScrollMode = View.OVER_SCROLL_NEVER
    val adapter = PostImagesAdapter(images = post.uploadurls)
    imagesPager.adapter = adapter

    val transformer = CompositePageTransformer()
    transformer.addTransformer(MarginPageTransformer(10))
    imagesPager.setPageTransformer(transformer)

    TabLayoutMediator(tabLayout, imagesPager) { _, _ -> }.attach()
}

//fun <T : Serializable?> Intent.getSerializable(key: String, m_class: Class<T>): T {
//    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
//        this.getSerializableExtra(key, m_class)!!
//    else
//        this.getSerializableExtra(key) as T
//}

private fun TextView.setTags(section: String) {
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
                            }
                        }
                        if (tag.startsWith("#")) {
                            Intent(this@setTags.context, HashTagResultsActivity::class.java).apply {
                                putExtra("hashtag", tag)
                                this@setTags.context.startActivity(this)
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

suspend fun RecyclerView.showUserPostsInGrid(
    context: Context,
    viewModel: HomeViewModel,
    profile: Profile
) {
    when (val posts = viewModel.getProfilePosts(profile.identifier)) {
        is Resource.Success -> {
            val gridlayout = GridLayoutManager(context, 3)
            this.layoutManager = gridlayout
            val thumbnailsAdapter = ImageThumbnailPostAdapter(posts.value.data)
            this.adapter = thumbnailsAdapter
        }
        is Resource.Failure -> {
            if (posts.isNetworkError)
                this.showUserPostsInGrid(context, viewModel, profile)
        }
        else -> {}
    }
}

suspend fun TextView.showFollowersCount(viewModel: HomeViewModel, profile: Profile) {
    when (val followersCount =
        viewModel.getFollowersCount(profile.identifier)) {
        is Resource.Success -> {
            val count = followersCount.value.data.toString().formatNumber()
            val followersResult = "$count Followers"
            this.text = followersResult.setSpannableBold(count)
            this.visible(true)
        }
        is Resource.Failure -> {
            if (followersCount.isNetworkError)
                this.showFollowersCount(viewModel, profile)
        }
        else -> {}
    }
}

suspend fun TextView.showFollowingCount(viewModel: HomeViewModel, profile: Profile) {
    when (val f = viewModel.getFollowingsCount(profile.identifier)) {
        is Resource.Success -> {
            val count = f.value.data.toString().formatNumber()
            val followersResult = "$count Following"
            this.text = followersResult.setSpannableBold(count)
            this.visible(true)
        }
        is Resource.Failure -> {
            if (f.isNetworkError)
                this.showFollowingCount(viewModel, profile)
        }
        else -> {}
    }
}

fun View.snackbar(message: String, action: (() -> Unit)? = null) {
    val snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT)
    snackbar.setBackgroundTint(Color.RED)
    action.let {
        if (it != null) {
            it()
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
}

fun View.visible(isVisible: Boolean) {
    visibility = if (isVisible) View.VISIBLE else View.GONE
}

fun View.enable(enabled: Boolean) {
    isEnabled = enabled
    alpha = if (enabled) 1f else 0.5f
}

fun Fragment.handleAPIError(failure: Resource.Failure, retry: (() -> Unit)? = null) {
    when {
        failure.isNetworkError -> requireView().snackbar(
            "Please check your internet connection",
            retry
        )
        failure.errorCode == 401 -> {
            if (this is LoginFragment) {
                requireView().snackbar("You've entered incorrect email or password")
            } else {
                Intent(requireContext(), AuthenticationActivity::class.java).also {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(it)
                }
            }
        }
        else -> {
            val error = Gson().fromJson(failure.errorBody?.string(), BasicResponseBody::class.java)
            requireView().snackbar(error.message)
        }
    }
}

fun handleAPIError(view: View, failure: Resource.Failure, retry: (() -> Unit)? = null) {
    when {
        failure.isNetworkError -> view.snackbar("Please check your internet connection", retry)
        failure.errorCode == 401 -> {
            Intent(view.context, AuthenticationActivity::class.java).also {
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                view.context.startActivity(it)
            }
        }
        else -> {
            view.snackbar("Server error occurred")
        }
    }
}

fun AppCompatActivity.setupHomeIndicator(toolbar: Toolbar) {
    setSupportActionBar(toolbar)
    toolbar.contentInsetStartWithNavigation = 0
    val ab = supportActionBar!!
    ab.setHomeAsUpIndicator(R.drawable.arrow_back_ic)
    ab.setDisplayHomeAsUpEnabled(true)
}

fun Activity.checkStoragePermissions() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
}

fun Activity.isVideoFile(uri: Uri): Boolean {
    val retriever = MediaMetadataRetriever()
    try {
        retriever.setDataSource(this, uri)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    val hasVideo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO)
    return "yes" == hasVideo
}

fun Activity.getFileTypes(uris: List<Uri>): List<String> {
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
            now,
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

fun Activity.isNetworkAvailable(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val nw = connectivityManager.activeNetwork ?: return false
    val activeNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
    return when {
        activeNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        activeNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        else -> false
    }
}