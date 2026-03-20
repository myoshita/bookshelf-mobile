package com.myoshita.bookshelf

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.setSingletonImageLoaderFactory
import coil3.util.DebugLogger
import com.myoshita.bookshelf.api.apiModule
import com.myoshita.bookshelf.db.dataBaseModule
import com.myoshita.bookshelf.db.platformDataBaseModule
import com.myoshita.bookshelf.feature.barcodescan.barcodeScanViewModelModule
import com.myoshita.bookshelf.feature.bookdetail.bookDetailViewModeModule
import com.myoshita.bookshelf.feature.bookedit.bookEditViewModelModule
import com.myoshita.bookshelf.feature.bulkbarcodescan.bulkViewModelModule
import com.myoshita.bookshelf.feature.manualbookregistration.manualBookRegistrationViewModelModule
import com.myoshita.bookshelf.feature.manualbooksearch.manualBookSearchViewModelModule
import com.myoshita.bookshelf.feature.searchresult.searchResultViewModelModule
import com.myoshita.bookshelf.feature.top.Top
import com.myoshita.bookshelf.feature.top.topViewModelModule
import com.myoshita.bookshelf.repository.bookRepositoryModule
import com.myoshita.bookshelf.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.dsl.KoinAppDeclaration


@OptIn(ExperimentalCoilApi::class)
@Composable
@Preview
fun App(koinAppDeclaration: KoinAppDeclaration = {}) {
    val navController = rememberNavController()
    setSingletonImageLoaderFactory { context ->
        ImageLoader(context).newBuilder()
            .logger(DebugLogger())
            .build()
    }
    val scope = rememberCoroutineScope()

    AppTheme {
        KoinApplication(
            application = {
                koinAppDeclaration()
                modules(
                    apiModule,
                    dataBaseModule,
                    platformDataBaseModule(),
                    bookRepositoryModule,
                    topViewModelModule,
                    barcodeScanViewModelModule,
                    bulkViewModelModule,
                    bookDetailViewModeModule,
                    bookEditViewModelModule,
                    manualBookSearchViewModelModule,
                    manualBookRegistrationViewModelModule,
                    searchResultViewModelModule,
                )
            }
        ) {
            NavHost(
                navController = navController,
                startDestination = Top,
                modifier = Modifier.fillMaxSize()
            ) {
                appNavGraph(navController)
            }
        }
    }
}
