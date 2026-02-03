package com.github.junsik.markdown.mermaid

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.junsik.markdown.mermaid.core.ui.theme.DarkBackground
import com.github.junsik.markdown.mermaid.core.ui.theme.Primary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onAnimationFinish: () -> Unit) {
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }
    val textYOffset = remember { Animatable(50f) }

    LaunchedEffect(Unit) {
        // Logo scale in
        scale.animateTo(
            targetValue = 1.2f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        )
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        )

        // Glow and Text fade in
        launch {
            alpha.animateTo(1f, animationSpec = tween(1000, easing = LinearOutSlowInEasing))
        }
        launch {
            textYOffset.animateTo(0f, animationSpec = tween(1000, easing = LinearOutSlowInEasing))
        }

        delay(1500)
        onAnimationFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        // Shimmering Aurora Effect
        Box(
            modifier = Modifier
                .size(400.dp)
                .graphicsLayer(alpha = alpha.value * 0.4f)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Primary.copy(alpha = 0.6f), Color.Transparent),
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Premium Logo 'M'
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .graphicsLayer(scaleX = scale.value, scaleY = scale.value)
                    .shadow(elevation = 20.dp, shape = RoundedCornerShape(28.dp), ambientColor = Primary)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color.White, Color(0xFFF0F0F0))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "M",
                    style = MaterialTheme.typography.displayLarge.copy(
                        color = Primary,
                        fontWeight = FontWeight.Black,
                        fontSize = 64.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Text with slide-up fade-in
            Text(
                text = "Markdown Mermaid",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                ),
                modifier = Modifier.graphicsLayer(
                    alpha = alpha.value,
                    translationY = textYOffset.value
                )
            )
            
            Text(
                text = "Viewer",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                ),
                modifier = Modifier.graphicsLayer(
                    alpha = alpha.value,
                    translationY = textYOffset.value
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Visualize your notes with diagrams",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.graphicsLayer(
                    alpha = alpha.value,
                    translationY = textYOffset.value * 0.5f
                )
            )
        }
    }
}
