package kz.yers.quiz

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.google.android.gms.ads.MobileAds
import kz.yers.quiz.data.notifications.DailyReminderWorker
import kz.yers.quiz.data.notifications.NotificationChannels
import kz.yers.quiz.koin.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/**
 * Implements [ImageLoaderFactory] so Coil uses our tuned cache instead of its
 * defaults. Default in-memory cap is 25% of free RAM — fine on a Pixel 7, but
 * on a 2 GB device (Samsung A03 ships with 2 GB) that's a 200–500 MB cap that
 * the Android low-memory killer reacts to long before Coil ever fills it.
 * 0.15 (15%) keeps roomy posters cached without inflating the foreground process
 * footprint past what the LMK budget allows.
 */
class MyApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MyApplication)
            modules(appModule)
        }

        MobileAds.initialize(this)

        NotificationChannels.ensure(this)
        DailyReminderWorker.schedule(this)
    }

    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.15)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02)
                    .build()
            }
            .crossfade(true)
            .build()
}
