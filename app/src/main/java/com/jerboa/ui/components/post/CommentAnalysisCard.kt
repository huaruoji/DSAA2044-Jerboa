package com.jerboa.ui.components.post

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class CommentAnalysis(
    val mainThemes: List<String>,
    val agreements: List<String>,
    val disagreements: List<String>,
)

@Composable
fun CommentAnalysisCard(
    analysis: CommentAnalysis,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Main Themes Section
            Text(
                text = "Main Themes",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            analysis.mainThemes.forEach { theme ->
                Text(
                    text = "• $theme",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
                )
            }

            // Agreements Section
            Text(
                text = "Agreements",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
            )
            analysis.agreements.forEach { agreement ->
                Text(
                    text = "• $agreement",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
                )
            }

            // Disagreements Section
            Text(
                text = "Disagreements",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
            )
            analysis.disagreements.forEach { disagreement ->
                Text(
                    text = "• $disagreement",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
                )
            }
        }
    }
}

