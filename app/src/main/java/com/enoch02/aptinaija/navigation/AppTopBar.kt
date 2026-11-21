package com.enoch02.aptinaija.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.enoch02.aptinaija.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    destination: TopLevelDestination,
    bookmarkCount: Int,
    onBookmarks: () -> Unit
) {
    TopAppBar(
        title = { Text(stringResource(destination.titleRes)) },
        actions = {
            if (destination.showBookmarksAction) {
                IconButton(onClick = onBookmarks) {
                    BadgedBox(
                        badge = {
                            if (bookmarkCount > 0) {
                                Badge { Text(bookmarkCount.toString()) }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = if (bookmarkCount > 0) {
                                pluralStringResource(
                                    R.plurals.bookmarks_saved,
                                    bookmarkCount,
                                    bookmarkCount
                                )
                            } else {
                                stringResource(R.string.saved_questions)
                            }
                        )
                    }
                }
            }
        }
    )
}
