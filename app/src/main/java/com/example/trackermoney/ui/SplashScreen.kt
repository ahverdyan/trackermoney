package com.example.trackermoney.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Accent = Color(0xFF6275ED)
private val Muted = Color(0xFF737887)

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    //анимация
    var started by remember { mutableStateOf(false) }

    val alpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "splashAlpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (started) 1f else 0.7f,
        animationSpec = tween(durationMillis = 600),
        label = "splashScale"
    )

    //время показа
    LaunchedEffect(Unit) {
        started = true
        kotlinx.coroutines.delay(2000)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        //логотип + текст
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .alpha(alpha)
                .scale(scale)
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(Accent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "₽",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "Трекер расходов",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF171923)
            )

            Spacer(Modifier.height(6.dp))

            Text(
                "Сэкономил — заработал",
                color = Muted,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(40.dp))

            CircularProgressIndicator(
                color = Accent,
                strokeWidth = 3.dp,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}