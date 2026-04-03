package com.myoshita.bookshelf.feature.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import bookshelf.shared.generated.resources.Res
import bookshelf.shared.generated.resources.placeholder
import coil3.compose.AsyncImage
import com.myoshita.bookshelf.model.Book
import com.myoshita.bookshelf.model.authorText
import com.myoshita.bookshelf.model.buildTitleText
import com.myoshita.bookshelf.model.isSigned
import org.jetbrains.compose.resources.painterResource

@Composable
fun BookGridItem(
    book: Book,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        shape = RectangleShape,
        modifier = modifier,
    ) {
        Column {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                AsyncImage(
                    model = book.thumbnailUrl,
                    placeholder = painterResource(Res.drawable.placeholder),
                    error = painterResource(Res.drawable.placeholder),
                    contentDescription = null,
                    contentScale = ContentScale.FillHeight,
                    modifier = Modifier.matchParentSize(),
                )
                if (book.isSigned) {
                    Text(
                        text = "サイン本",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .background(Color.Yellow.copy(alpha = 0.8f))
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter),
                    )
                }
            }
            Text(
                text = book.buildTitleText(hasSeriesTitle = false),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .padding(top = 4.dp),
            )
            Text(
                text = book.authorText(),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .padding(bottom = 4.dp),
            )
        }
    }
}
