package com.ba.walletcase

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point. [HiltAndroidApp] triggers Hilt's code generation and
 * creates the application-level dependency container that the rest of the app
 * (Activities, ViewModels) draws from.
 */
@HiltAndroidApp
class WalletApplication : Application()
