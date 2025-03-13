package io.github.thegbguy.fluidgradientclock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FluidGradientClock()
        }
    }
}

@Composable
fun FluidGradientClock() {
    Box(modifier = Modifier.fillMaxSize()) {
        GradientBackground()
        ClockFace(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.Center)
        )
    }
}

@Composable
fun GradientBackground() {
    val measurer = rememberTextMeasurer()
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFff7eb3), Color(0xFFff758c), Color(0xFFff7eb3)),
                center = Offset(size.width / 2, size.height / 2),
                radius = size.minDimension / 1.5f
            )
        )

        drawText(
            textMeasurer = measurer,
            text = "Fluid Gradient Clock",
            topLeft = Offset(size.width / 2 - 150, size.height / 2 - 100),
        )
    }
}

@Composable
fun ClockFace(
    modifier: Modifier = Modifier
) {
    val calendar by produceState(initialValue = Calendar.getInstance()) {
        while (isActive) {
            value = Calendar.getInstance()
            delay(1000)
        }
    }

    val hours by remember { derivedStateOf { calendar.get(Calendar.HOUR) % 12 } }
    val minutes by remember { derivedStateOf { calendar.get(Calendar.MINUTE) } }
    val seconds by remember { derivedStateOf { calendar.get(Calendar.SECOND) } }

    val hourRotation by remember { derivedStateOf { hours * 30f } }
    val minuteRotation by remember { derivedStateOf { minutes * 6f } }
    val secondRotation by remember { derivedStateOf { seconds * 6f } }

    val animatedHour = remember { Animatable(hourRotation) }
    val animatedMinute = remember { Animatable(minuteRotation) }
    val animatedSecond = remember { Animatable(secondRotation) }

    LaunchedEffect(hourRotation, minuteRotation, secondRotation) {
        launch { animatedHour.animateTo(hourRotation, animationSpec = tween(500)) }
        launch { animatedMinute.animateTo(minuteRotation, animationSpec = tween(300)) }
        launch { animatedSecond.animateTo(secondRotation, animationSpec = tween(150)) }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .offset(y = 60.dp)
                .background(Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(8.dp))
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DigitalClockDisplay(hours, minutes, seconds)
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val clockRadius = size.width / 2.2f

            drawCircle(
                color = Color.White.copy(.3f),
                radius = clockRadius,
                center = Offset(size.width / 2, size.height / 2)
            )

            // Draw minute indicators
            for (i in 0 until 60) {
                val angle = Math.toRadians(i * 6.0)

                val isHourMark = i % 5 == 0
                val startRadiusFactor = if (isHourMark) 0.8 else 0.9

                val startX = (center.x + clockRadius * startRadiusFactor * cos(angle)).toFloat()
                val startY = (center.y + clockRadius * startRadiusFactor * sin(angle)).toFloat()
                val endX = (center.x + clockRadius * cos(angle)).toFloat()
                val endY = (center.y + clockRadius * sin(angle)).toFloat()

                drawLine(
                    color = if (i % 5 == 0) Color.White else Color.Gray, // Highlight major indicators
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = if (i % 5 == 0) 8f else 2f
                )
            }

            fun drawHand(rotation: Float, length: Float, strokeWidth: Float, color: Color) {
                val angle = Math.toRadians(rotation.toDouble() - 90)
                val endX = (center.x + length * cos(angle)).toFloat()
                val endY = (center.y + length * sin(angle)).toFloat()
                drawLine(color, center, Offset(endX, endY), strokeWidth)
            }

            drawHand(animatedHour.value, clockRadius * 0.5f, 8f, Color.White)  // Hour Hand
            drawHand(animatedMinute.value, clockRadius * 0.7f, 5f, Color.LightGray) // Minute Hand
            drawHand(animatedSecond.value, clockRadius * 0.8f, 3f, Color.Red) // Second Hand
        }
    }
}

@Composable
fun DigitalClockDisplay(hours: Int, minutes: Int, seconds: Int) {
    Row(
        modifier = Modifier.padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedClockNumber(hours)
        Text(":", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        AnimatedClockNumber(minutes)
        Text(":", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        AnimatedClockNumber(seconds)
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedClockNumber(value: Int) {
    AnimatedContent(targetState = value, transitionSpec = {
        slideInVertically { height -> height } + fadeIn() with
                slideOutVertically { height -> -height } + fadeOut()
    }) { targetValue ->
        Text(
            text = targetValue.toString().padStart(2, '0'),
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewClock() {
    FluidGradientClock()
}
