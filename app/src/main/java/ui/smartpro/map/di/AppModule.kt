package ui.smartpro.map.di

import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ui.smartpro.map.ui.MarkerViewModel

val appModule = module {
    //vm
    viewModel { MarkerViewModel(androidApplication()) }
}