package com.jurobil.progressian.ui.screens.homeScreen.components
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jurobil.progressian.ui.screens.homeScreen.viewmodel.HomeUiState

@Composable
fun HomeHeader(
    state: HomeUiState, 
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 200.dp) 
    ) {
        Image(
            painter = painterResource(id = com.jurobil.progressian.R.drawable.back),
            contentDescription = null,
            contentScale = ContentScale.Crop, 
            modifier = Modifier.matchParentSize()
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.background 
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp) 
        ) {
            Text(
                text = "Progressian",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White, 
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProgressCard(state)
        }
    }
}