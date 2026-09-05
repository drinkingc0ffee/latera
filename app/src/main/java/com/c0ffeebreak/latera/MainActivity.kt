package com.c0ffeebreak.latera

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        setContent {
            LateraApp()
        }
    }
}

@Composable
fun LateraApp() {
    val context = LocalContext.current
    val settings = remember { SettingsStore(context) }
    val clickPlayer = remember { StereoClickPlayer(context) }

    var running by remember { mutableStateOf(true) }
    var beatsPerSecond by remember { mutableFloatStateOf(settings.beatsPerSecond) }
    var ballSize by remember { mutableFloatStateOf(settings.ballSize) }
    var darkBackground by remember { mutableStateOf(settings.darkBackground) }
    var audioEnabled by remember { mutableStateOf(settings.audioEnabled) }
    var progress by remember { mutableFloatStateOf(0f) }
    var direction by remember { mutableIntStateOf(1) }

    val audioOn = rememberUpdatedState(audioEnabled)

    DisposableEffect(Unit) {
        onDispose { clickPlayer.release() }
    }

    LaunchedEffect(running, beatsPerSecond) {
        if (!running) return@LaunchedEffect
        var lastNanos = 0L
        while (true) {
            withFrameNanos { now ->
                if (lastNanos == 0L) {
                    lastNanos = now
                    return@withFrameNanos
                }
                val dt = (now - lastNanos) / 1_000_000_000f
                lastNanos = now
                var next = progress + direction * beatsPerSecond * dt
                var dir = direction
                if (next >= 1f) {
                    next = 1f
                    dir = -1
                    if (audioOn.value) clickPlayer.playRight()
                } else if (next <= 0f) {
                    next = 0f
                    dir = 1
                    if (audioOn.value) clickPlayer.playLeft()
                }
                progress = next
                direction = dir
            }
        }
    }

    val background = if (darkBackground) Color.Black else Color.White
    val foreground = if (darkBackground) Color.White else Color.Black
    val muted = foreground.copy(alpha = 0.72f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) {
                    running = !running
                },
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val diameter = size.minDimension * ballSize
                val radius = diameter / 2f
                val travel = (size.width - diameter).coerceAtLeast(0f)
                val cx = radius + travel * progress
                val cy = size.height / 2f
                drawCircle(
                    color = foreground,
                    radius = radius,
                    center = Offset(cx, cy),
                )
            }
            TextButton(
                onClick = {
                    (context as? ComponentActivity)?.finishAndRemoveTask()
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 8.dp),
            ) {
                Text(
                    text = "EXIT",
                    color = foreground,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            if (!running) {
                Text(
                    text = "Paused  ·  tap to start",
                    color = muted,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp),
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val sliderColors = SliderDefaults.colors(
                thumbColor = foreground,
                activeTrackColor = foreground,
                inactiveTrackColor = foreground.copy(alpha = 0.28f),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Beats per second  ${formatBps(beatsPerSecond)}",
                    color = foreground,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                )
                Slider(
                    value = beatsPerSecond,
                    onValueChange = { value ->
                        beatsPerSecond = value
                    },
                    onValueChangeFinished = {
                        settings.beatsPerSecond = beatsPerSecond
                    },
                    valueRange = SettingsStore.MIN_BPS..SettingsStore.MAX_BPS,
                    steps = 14,
                    colors = sliderColors,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Ball size  ${formatBallSize(ballSize)}",
                    color = foreground,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                )
                Slider(
                    value = ballSize,
                    onValueChange = { value ->
                        ballSize = value
                    },
                    onValueChangeFinished = {
                        settings.ballSize = ballSize
                    },
                    valueRange = SettingsStore.MIN_BALL_SIZE..SettingsStore.MAX_BALL_SIZE,
                    colors = sliderColors,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                TextButton(
                    onClick = {
                        audioEnabled = !audioEnabled
                        settings.audioEnabled = audioEnabled
                    },
                    modifier = Modifier.widthIn(min = 88.dp),
                ) {
                    Text(
                        text = if (audioEnabled) "Audio on" else "Audio off",
                        color = foreground,
                        fontSize = 14.sp,
                    )
                }
                TextButton(
                    onClick = {
                        darkBackground = !darkBackground
                        settings.darkBackground = darkBackground
                    },
                    modifier = Modifier.widthIn(min = 88.dp),
                ) {
                    Text(
                        text = if (darkBackground) "White bg" else "Black bg",
                        color = foreground,
                        fontSize = 14.sp,
                    )
                }
            }
        }
    }
}

private fun formatBps(value: Float): String {
    val tenths = (value * 10f).roundToInt() / 10f
    return if (tenths == tenths.toInt().toFloat()) {
        tenths.toInt().toString()
    } else {
        String.format("%.1f", tenths)
    }
}

private fun formatBallSize(value: Float): String {
    val span = SettingsStore.MAX_BALL_SIZE - SettingsStore.MIN_BALL_SIZE
    val level = 1 + ((value - SettingsStore.MIN_BALL_SIZE) / span * 9f).roundToInt()
    return level.toString()
}
