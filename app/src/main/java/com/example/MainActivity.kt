package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.*
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContainer()
            }
        }
    }
}

@Composable
fun MainAppContainer() {
    val viewModel: MainViewModel = viewModel()
    var selectedTab by remember { mutableStateOf(0) } // 0 = Home, 1 = Test, 2 = New Test, 3 = Learn, 4 = Community

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            CustomBottomNavigation(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        },
        containerColor = DarkBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToTab = { selectedTab = it }
                )
                1 -> PerformanceTrackerScreen(viewModel = viewModel)
                2 -> TestFlowScreen(viewModel = viewModel)
                3 -> LearnScreen(viewModel = viewModel)
                4 -> CommunityScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun CustomBottomNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val items = listOf(
        NavigationItem("Home", Icons.Default.Home, 0, "tab_home"),
        NavigationItem("Test", Icons.Default.Analytics, 1, "tab_test"),
        NavigationItem("New Test", Icons.Default.AddTask, 2, "tab_new_test"),
        NavigationItem("Learn", Icons.Default.MenuBook, 3, "tab_learn"),
        NavigationItem("Community", Icons.Default.Group, 4, "tab_community")
    )

    // Window insets safe custom bottom bar decorated with Elegant Dark theme
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .border(1.dp, BorderColor.copy(alpha = 0.8f), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
        color = Color(0xFF111114), // Matches Design HTML bg-[#111114]
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = selectedTab == item.index

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onTabSelected(item.index) }
                        .testTag(item.testTag)
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (item.index == 2) {
                        // Central Prominent AI/Test Action Button
                        Box(
                            modifier = Modifier
                                .offset(y = (-14).dp)
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.sweepGradient(
                                        colors = listOf(PrimaryNeon, SecondaryNeon, PrimaryNeon)
                                    )
                                )
                                .border(4.dp, DarkBg, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = if (isSelected) PrimaryNeon else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (item.index != 2) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.title,
                            color = if (isSelected) PrimaryNeon else TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    } else {
                        // For center button, offset label to fit design nicely
                        Text(
                            text = item.title,
                            color = if (isSelected) PrimaryNeon else TextSecondary,
                            fontSize = 9.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.offset(y = (-8).dp)
                        )
                    }
                }
            }
        }
    }
}

data class NavigationItem(
    val title: String,
    val icon: ImageVector,
    val index: Int,
    val testTag: String
)

// --- Secondary Performance and Gamification Tracker Screen (Tab 1: Test Results History) ---
@Composable
fun PerformanceTrackerScreen(viewModel: MainViewModel) {
    val results by viewModel.testResults.collectAsState()
    val profile by viewModel.userProfile.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
    ) {
        // Gamification metrics
        item {
            Text(
                text = "Performance Diagnostics",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Observe accuracy rankings, streak status, and medals index:",
                color = TextSecondary,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Medals/Badges Index
        item {
            Text(
                text = "Unlocked Accolades",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            val badges = profile?.badges?.split(",") ?: listOf("Streak Master", "Accuracy Ace", "AI Solver Pro")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                badges.forEach { badge ->
                    val cleanBadge = badge.trim()
                    val badgeColor = when (cleanBadge) {
                        "Streak Master" -> FlameOrange
                        "Accuracy Ace" -> NeonGreen
                        else -> PrimaryNeon
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceCard)
                            .border(1.dp, BorderColor, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = when (cleanBadge) {
                                    "Streak Master" -> Icons.Default.OfflineBolt
                                    "Accuracy Ace" -> Icons.Default.Recommend
                                    else -> Icons.Default.Psychology
                                },
                                contentDescription = badge,
                                tint = badgeColor,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = cleanBadge,
                                color = TextPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Leaderboard segment
        item {
            Text(
                text = "Accuracy Rankings Leaderboard",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            val players = listOf(
                LeaderboardPlayer("1", "Kartik Goyal", "JEE", "96.8% Accuracy", true),
                LeaderboardPlayer("2", "${profile?.name ?: "Arjun"} (You)", profile?.selectedStream ?: "JEE", "92.4% Accuracy", false),
                LeaderboardPlayer("3", "Soumya Sinha", "NEET", "90.1% Accuracy", false),
                LeaderboardPlayer("4", "Priyanshu Verma", "JEE", "85.5% Accuracy", false)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceCard)
                    .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                players.forEach { p ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (p.isFirst) PrimaryNeon.copy(alpha = 0.1f) else Color.Transparent)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = p.rank,
                            color = if (p.isFirst) GoldYellow else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.width(30.dp)
                        )
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (p.isFirst) GoldYellow.copy(alpha = 0.2f) else BorderColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (p.isFirst) Icons.Default.WorkspacePremium else Icons.Default.Person,
                                contentDescription = "user",
                                tint = if (p.isFirst) GoldYellow else TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = p.name,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Stream focus: ${p.stream}",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                        Text(
                            text = p.scoreLabel,
                            color = NeonGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Historic Mock Results Log
        item {
            Text(
                text = "Mock Tests Attempt History",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (results.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No history logged yet. Build your first test under 'New Test' wizard!",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(results) { res ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceCard)
                        .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PrimaryNeon.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = "Test record",
                            tint = PrimaryNeon,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = res.testTitle,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${res.examType} • Score of ${res.correctCount}/${res.totalCount}",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Text(
                        text = "${res.percentage}%",
                        color = NeonGreen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

data class LeaderboardPlayer(
    val rank: String,
    val name: String,
    val stream: String,
    val scoreLabel: String,
    val isFirst: Boolean
)
