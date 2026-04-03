package com.myoshita.bookshelf

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.myoshita.bookshelf.feature.barcodescan.BarcodeScan
import com.myoshita.bookshelf.feature.barcodescan.BarcodeScanScreen
import com.myoshita.bookshelf.feature.bookdetail.BookDetail
import com.myoshita.bookshelf.feature.bookdetail.BookDetailScreen
import com.myoshita.bookshelf.feature.bookedit.BookEdit
import com.myoshita.bookshelf.feature.bookedit.BookEditScreen
import com.myoshita.bookshelf.feature.bulkbarcodescan.BulkBarcodeScan
import com.myoshita.bookshelf.feature.bulkbarcodescan.BulkBarcodeScanScreen
import com.myoshita.bookshelf.feature.manualbookregistration.ManualBookRegistration
import com.myoshita.bookshelf.feature.manualbookregistration.ManualBookRegistrationScreen
import com.myoshita.bookshelf.feature.manualbooksearch.ManualBookSearch
import com.myoshita.bookshelf.feature.manualbooksearch.ManualBookSearchScreen
import com.myoshita.bookshelf.feature.searchresult.SearchResult
import com.myoshita.bookshelf.feature.searchresult.SearchResultScreen
import com.myoshita.bookshelf.feature.top.Top
import com.myoshita.bookshelf.feature.top.TopScreen
import com.myoshita.bookshelf.model.Suggestion

fun NavGraphBuilder.appNavGraph(navController: NavHostController) {
    composable<Top> {
        TopScreen(
            onClickBarcodeScan = {
                navController.navigate(BulkBarcodeScan)
            },
            onClickManualRegistration = {
                navController.navigate(ManualBookRegistration)
            },
            onClickBook = {
                val route = BookDetail(it.id)
                navController.navigate(route)
            },
            navigateToWordSearchResult = {
                val route = SearchResult(word = it)
                navController.navigate(route)
            },
            navigateToSuggestionSearchResult = {
                val route = when (it) {
                    is Suggestion.Author -> SearchResult(author = it.name)
                    is Suggestion.Title -> SearchResult(title = it.name)
                }
                navController.navigate(route)
            },
        )
    }
    composable<BarcodeScan> {
        BarcodeScanScreen(
            onNavigateUp = {
                navController.navigateUp()
            },
        )
    }
    composable<BulkBarcodeScan> {
        BulkBarcodeScanScreen(
            onNavigateUp = {
                navController.navigateUp()
            },
            onClickManualSearch = {
                navController.navigate(ManualBookSearch) {
                    popUpTo(BulkBarcodeScan) {
                        inclusive = true
                    }
                }
            },
        )
    }
    composable<BookDetail> { backstackEntry ->
        val bookDetail = backstackEntry.toRoute<BookDetail>()
        BookDetailScreen(
            bookId = bookDetail.bookId,
            onNavigateUp = {
                navController.navigateUp()
            },
            onClickEdit = {
                val route = BookEdit(it.id)
                navController.navigate(route)
            },
            navigateToAuthorSearch = {
                val route = SearchResult(author = it.name)
                navController.navigate(route)
            },
            navigateToTagSearch = {
                val route = SearchResult(tag = it)
                navController.navigate(route)
            },
        )
    }
    composable<BookEdit> { backstackEntry ->
        val bookEdit = backstackEntry.toRoute<BookEdit>()
        BookEditScreen(
            bookId = bookEdit.bookId,
            onNavigateUp = {
                navController.navigateUp()
            },
        )
    }
    composable<ManualBookSearch> {
        ManualBookSearchScreen(
            onNavigateUp = {
                navController.navigateUp()
            },
        )
    }
    composable<ManualBookRegistration> {
        ManualBookRegistrationScreen(
            onNavigateUp = {
                navController.navigateUp()
            },
        )
    }
    composable<SearchResult> { backstackEntry ->
        val searchResult = backstackEntry.toRoute<SearchResult>()
        SearchResultScreen(
            route = searchResult,
            onNavigateUp = {
                navController.navigateUp()
            },
            navigateToBookDetail = {
                val route = BookDetail(it.id)
                navController.navigate(route)
            },
        )
    }
}
