package protrnd.com.data.network.backgroundtask

import android.app.IntentService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.ListenableWorker
import co.paystack.android.PaystackSdk
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import protrnd.com.R
import protrnd.com.data.models.Location
import protrnd.com.data.models.PostDTO
import protrnd.com.data.network.ProtrndAPIDataSource
import protrnd.com.data.network.api.PostApi
import protrnd.com.data.responses.PostResponseBody
import protrnd.com.ui.getFileTypes
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UploadJobService: IntentService("UploadJobService") {
    companion object {
        const val NOTIFICATION_CHANNEL = "protrnd_post"
        const val NOTIFICATION_NAME = "Protrnd"
        const val NOTIFICATION_ID = 93819
        const val NOTIFICATION_NEW_ID = 34819
        const val FAILED_NOTIFICATION_ID = 722984
    }

    private fun sendNewUploadNotification() {
        val notificationManager = applicationContext.getSystemService(
            NOTIFICATION_SERVICE
        ) as NotificationManager
        val titleNotification = applicationContext.getString(R.string.notification_title)
        val subtitleNotification = applicationContext.getString(R.string.notification_subtitle)
        val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.launcher_filled_ic)
            .setOngoing(true)
            .setContentTitle(titleNotification).setContentText(subtitleNotification)
            .setDefaults(NotificationCompat.DEFAULT_ALL).setAutoCancel(true)

        notification.priority = NotificationCompat.PRIORITY_MAX

        notification.setChannelId(NOTIFICATION_CHANNEL)

        val ringtoneManager = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val audioAttributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()

        val channel =
            NotificationChannel(NOTIFICATION_CHANNEL, NOTIFICATION_NAME, NotificationManager.IMPORTANCE_HIGH)

        channel.enableLights(true)
        channel.lightColor = applicationContext.getColor(R.color.main_pink)
        channel.setSound(ringtoneManager,audioAttributes)
        notificationManager.createNotificationChannel(channel)

        notificationManager.notify(NOTIFICATION_NEW_ID, notification.build())
    }

    private fun sendNotification(): Notification {
        val notificationManager = applicationContext.getSystemService(
            NOTIFICATION_SERVICE
        ) as NotificationManager
        val titleNotification = applicationContext.getString(R.string.notification_title)
        val subtitleNotification = applicationContext.getString(R.string.notification_subtitle)
        val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.launcher_filled_ic)
            .setOngoing(true)
            .setContentTitle(titleNotification).setContentText(subtitleNotification)
            .setDefaults(NotificationCompat.DEFAULT_ALL).setAutoCancel(false)

        notification.priority = NotificationCompat.PRIORITY_MAX

        notification.setChannelId(NOTIFICATION_CHANNEL)

        val ringtoneManager = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val audioAttributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()

        val channel =
            NotificationChannel(NOTIFICATION_CHANNEL, NOTIFICATION_NAME, NotificationManager.IMPORTANCE_HIGH)

        channel.enableLights(true)
        channel.lightColor = applicationContext.getColor(R.color.main_pink)
        channel.setSound(ringtoneManager,audioAttributes)
        notificationManager.createNotificationChannel(channel)

        return notification.build()
    }

    private fun sendFailedNotification() {
        val notificationManager = applicationContext.getSystemService(
            NOTIFICATION_SERVICE
        ) as NotificationManager
        val titleNotification = applicationContext.getString(R.string.notification_title)
        val subtitleNotification = applicationContext.getString(R.string.notification_failed)
        val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.launcher_filled_ic)
            .setContentTitle(titleNotification).setContentText(subtitleNotification)
            .setDefaults(NotificationCompat.DEFAULT_ALL).setAutoCancel(true)

        notification.priority = NotificationCompat.PRIORITY_MAX

        notification.setChannelId(NOTIFICATION_CHANNEL)

        val ringtoneManager = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val audioAttributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()

        val channel =
            NotificationChannel(NOTIFICATION_CHANNEL, NOTIFICATION_NAME, NotificationManager.IMPORTANCE_HIGH)

        channel.enableLights(true)
        channel.lightColor = applicationContext.getColor(R.color.main_pink)
        channel.setSound(ringtoneManager,audioAttributes)
        notificationManager.createNotificationChannel(channel)

        notificationManager.notify(FAILED_NOTIFICATION_ID, notification.build())
    }

    private fun sendSuccessNotification() {
        val notificationManager = applicationContext.getSystemService(
            NOTIFICATION_SERVICE
        ) as NotificationManager
        val titleNotification = applicationContext.getString(R.string.notification_title)
        val subtitleNotification = applicationContext.getString(R.string.notification_success)
        val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.launcher_filled_ic)
            .setContentTitle(titleNotification).setContentText(subtitleNotification)
            .setDefaults(NotificationCompat.DEFAULT_ALL).setAutoCancel(true)

        notification.priority = NotificationCompat.PRIORITY_MAX

        notification.setChannelId(NOTIFICATION_CHANNEL)

        val ringtoneManager = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val audioAttributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()

        val channel =
            NotificationChannel(NOTIFICATION_CHANNEL, NOTIFICATION_NAME, NotificationManager.IMPORTANCE_HIGH)

        channel.enableLights(true)
        channel.lightColor = applicationContext.getColor(R.color.main_pink)
        channel.setSound(ringtoneManager,audioAttributes)
        notificationManager.createNotificationChannel(channel)

        notificationManager.notify(FAILED_NOTIFICATION_ID, notification.build())
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID,sendNotification())
    }

    override fun onHandleIntent(intent: Intent?) {
        intent!!
        val authToken = intent.getStringExtra("auth")!!
        val caption = intent.getStringExtra("caption")!!
        val tags = intent.getStringArrayListExtra("tags")!!
        val postUriList = intent.getStringArrayListExtra("uris")!!
        val username = intent.getStringExtra("name")!!
        val city = intent.getStringExtra("city")!!
        val state = intent.getStringExtra("state")!!
        val location = Location(city = city, state = state)
        val api = ProtrndAPIDataSource().buildAPI(PostApi::class.java, authToken)
        val uriList = arrayListOf<Uri>()
        postUriList.forEach {
            uriList.add(Uri.parse(it))
        }
        addImageToFirebase(uriList, username, applicationContext.getFileTypes(uriList),caption,location,tags, api)
    }

    private fun addImageToFirebase(
        uris: List<Uri>,
        username: String,
        fileType: List<String>,
        caption: String,
        location: Location,
        tags: List<String>,
        api: PostApi
    ): List<String> {
        val urls = mutableListOf<String>()
        val notificationManager = applicationContext.getSystemService(
            NOTIFICATION_SERVICE
        ) as NotificationManager
        sendNewUploadNotification()
        for (position in uris.indices) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val fileReference: StorageReference =
                        FirebaseStorage.getInstance().reference.child(
                            username +
                                    System.currentTimeMillis()
                                        .toString() + "." + fileType[position]
                        )
                    val downloadUrl =
                        fileReference.putFile(uris[position]).await().storage.downloadUrl.await()
                    urls.add(downloadUrl.toString())
                    if (urls.size == uris.size) {
                        val postDto = PostDTO(
                            caption = caption,
                            location = location,
                            uploadurls = urls,
                            tags = tags.toList()
                        )

                        api.addPost(postDto).enqueue(object : Callback<PostResponseBody> {
                            override fun onResponse(
                                call: Call<PostResponseBody>,
                                response: Response<PostResponseBody>
                            ) {
                                notificationManager.cancel(NOTIFICATION_NEW_ID)
                                if (response.body()?.successful!!)
                                    sendSuccessNotification()
                                else
                                    sendFailedNotification()
                            }

                            override fun onFailure(call: Call<PostResponseBody>, t: Throwable) {
                                sendFailedNotification()
                            }
                        })
                    }
                } catch (e: Exception) {
                    throw e
                }
            }
        }
        return urls
    }
}