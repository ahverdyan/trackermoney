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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trackermoney.R

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
    val accent = colorResource(R.color.brand_accent)
    val muted = colorResource(R.color.brand_muted)

    //время показа
    LaunchedEffect(Unit) {
        started = true
        kotlinx.coroutines.delay(2000)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
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
                .background(accent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "₽",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                stringResource(R.string.home_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(6.dp))

            Text(
                stringResource(R.string.splash_slogan),
                color = muted,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(40.dp))

            CircularProgressIndicator(
                color = accent,
                strokeWidth = 3.dp,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
