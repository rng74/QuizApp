package kz.yers.quiz.koin

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import kz.yers.quiz.QuizAppViewModel
import kz.yers.quiz.data.local.AppDatabase
import kz.yers.quiz.data.notifications.NotificationRepository
import kz.yers.quiz.data.prefs.UserPrefs
import kz.yers.quiz.data.remote.DailyStatsRepository
import kz.yers.quiz.data.remote.DuelRepository
import kz.yers.quiz.data.remote.LeaderboardRepository
import kz.yers.quiz.repo.AnimeRepository
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule =
    module {
        single { Gson() }
        single<SharedPreferences> {
            androidContext().getSharedPreferences("quiz_prefs", Context.MODE_PRIVATE)
        }
        single { AppDatabase.build(androidContext()) }
        single { get<AppDatabase>().runHistoryDao() }
        single { get<AppDatabase>().dailyAttemptDao() }
        single { get<AppDatabase>().notificationDao() }
        single { NotificationRepository(androidContext(), get()) }
        single { UserPrefs(androidContext()) }
        single { AnimeRepository(androidContext(), get(), get()) }
        single { LeaderboardRepository() }
        single { DailyStatsRepository() }
        single { DuelRepository() }
        viewModel { QuizAppViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    }
