package at.fhooe.hagenberg.tutorbot.network

import dagger.Module
import dagger.Provides
import okhttp3.CookieJar
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import java.net.CookieManager
import javax.inject.Singleton

@Module
object NetworkModule {

    @Provides
    @Singleton
    fun provideCookieJar(): CookieJar {
        return JavaNetCookieJar(CookieManager())
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(cookieJar: CookieJar): OkHttpClient {
        return OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .build()
    }

    @Provides
    fun provideUrlProvider() = object : UrlProvider {
        override fun baseUrl() = "https://hagenberg.elearning.fh-ooe.at/"
    }
}
