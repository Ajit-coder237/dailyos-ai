package com.example.presentation.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.components.PrimaryButton
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 5 })
    val scope = rememberCoroutineScope()

    val slides = listOf(
        OnboardingSlide(
            title = "Welcome to DailyOS AI",
            subtitle = "Your secure, high-fidelity offline personal life command center. Run tasks, notes, habits, memory flashcards, and budgets in one premium unified environment.",
            icon = Icons.Default.RocketLaunch,
            gradientColors = listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))
        ),
        OnboardingSlide(
            title = "Organize Your Daily Flow",
            subtitle = "Plan your schedule utilizing dynamic priorities (Low, Medium, High, Critical) and smart categories. Keep the mental workspace perfectly uncluttered.",
            icon = Icons.Default.TaskAlt,
            gradientColors = listOf(Color(0xFF0D9488), Color(0xFF0F766E))
        ),
        OnboardingSlide(
            title = "Build Habits & Deep Work",
            subtitle = "Track streaks of daily rituals automatically. Activate Pomodoro style deep focus clocks and lock notifications behind structural boundaries.",
            icon = Icons.Default.Timer,
            gradientColors = listOf(Color(0x8E,0x24,0xAA,0xFF), Color(0x6A,0x1B,0x9A,0xFF))
        ),
        OnboardingSlide(
            title = "Spaced Active Revision",
            subtitle = "Retain study topics securely. Utilizing double-spaced intervals (1, 2, 5, 10 days) and custom Memory Palaces to lock facts into place easily.",
            icon = Icons.Default.Psychology,
            gradientColors = listOf(Color(0xFFF97316), Color(0xFFEA580C))
        ),
        OnboardingSlide(
            title = "Privacy Sovereign AI",
            subtitle = "No external accounts or cloud databases are required. Your daily logs, journals, and local AI summaries remain encrypted on this specific hardware device.",
            icon = Icons.Default.Shield,
            gradientColors = listOf(Color(0xFF10B981), Color(0xFF047857))
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { page ->
            val slide = slides[page]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Brush.linearGradient(slide.gradientColors)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = slide.icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(80.dp)
                    )
                }
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = slide.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = slide.subtitle,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
        }

        // Bottom Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 24.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicator dots
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(5) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 18.dp else 8.dp, 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                            )
                    )
                }
            }

            // CTA Button
            Box(modifier = Modifier.width(140.dp)) {
                if (pagerState.currentPage == 4) {
                    PrimaryButton(
                        text = "Get Started",
                        onClick = onFinish
                    )
                } else {
                    PrimaryButton(
                        text = "Next",
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    )
                }
            }
        }
    }
}

data class OnboardingSlide(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val gradientColors: List<Color>
)
