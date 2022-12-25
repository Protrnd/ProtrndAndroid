package protrnd.com.data.network

import android.app.Application
import androidx.room.Room
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import protrnd.com.BuildConfig
import protrnd.com.data.models.FCMValues
import protrnd.com.data.network.api.FCMNotificationApi
import protrnd.com.data.network.database.NotificationDatabase
import protrnd.com.data.network.database.PostDatabase
import protrnd.com.data.network.database.ProfileDatabase
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ProtrndAPIDataSource {
    companion object {
        private const val BASE_URL = "https://protrndapi-rxxcn57klq-uc.a.run.app/api/"
    }

    fun <API> buildAPI(
        api: Class<API>,
        authToken: String? = null
    ): API {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(
                OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        chain.proceed(chain.request().newBuilder().also {
                            it.addHeader("Authorization", "Bearer $authToken")
                        }.build())
                    }
                    .also { client -> //Only needed during debugging
                        if (BuildConfig.DEBUG) {
                            val logging = HttpLoggingInterceptor()
                            logging.setLevel(HttpLoggingInterceptor.Level.BODY)
                            client.addInterceptor(logging)
                        }
                    }.build()
            )
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(api)
    }

    fun getClients(): FCMNotificationApi {
        return Retrofit.Builder()
            .baseUrl(FCMValues.BASE_URL)
            .client(
                OkHttpClient.Builder()
                    .also { client ->
                        if (BuildConfig.DEBUG) {
                            val logging = HttpLoggingInterceptor()
                            logging.setLevel(HttpLoggingInterceptor.Level.BODY)
                            client.addInterceptor(logging)
                        }
                    }.build()
            )
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FCMNotificationApi::class.java)
    }

    fun providePostDatabase(application: Application): PostDatabase =
        Room.databaseBuilder(application, PostDatabase::class.java, "Post Database").build()

    fun provideProfileDatabase(application: Application): ProfileDatabase =
        Room.databaseBuilder(application, ProfileDatabase::class.java, "Profile Database").build()

    fun provideNotificationDatabase(application: Application): NotificationDatabase =
        Room.databaseBuilder(application, NotificationDatabase::class.java, "Notification Database")
            .build()
}