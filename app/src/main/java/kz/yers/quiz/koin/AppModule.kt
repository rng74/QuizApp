package kz.yers.quiz.koin

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import kz.yers.quiz.QuizAppViewModel
import kz.yers.quiz.repo.AnimeRepository
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { Gson() }
    single<SharedPreferences> {
        androidContext().getSharedPreferences("quiz_prefs", Context.MODE_PRIVATE)
    }
    single { AnimeRepository(androidContext(), get(), get()) }
    viewModel { QuizAppViewModel(get()) }
}