package protrnd.com.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import protrnd.com.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ProtrndAPIDataSource {
    companion object {
        private const val BASE_URL = "https://protrndapi-i4fwl43ekq-uc.a.run.app/api/"
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
}