package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.viewmodel.MainViewModel
import com.example.ui.theme.*
import com.example.data.*
import androidx.compose.foundation.BorderStroke

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToTab: (Int) -> Unit
) {
    val profile by viewModel.userProfile.collectAsState()
    val planList by viewModel.studyPlan.collectAsState()

    var showFormulaBook by remember { mutableStateOf(false) }
    var showFlashcards by remember { mutableStateOf(false) }
    var showStudyBattle by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
    ) {
        // --- Header greeting and avatar ---
        item {
            HeaderSection(userName = profile?.name ?: "Arjun", points = profile?.points ?: 1200)
            Spacer(modifier = Modifier.height(20.dp))
        }

        // --- Study Streak Card with circular flame ring ---
        item {
            StreakCard(
                streakCount = profile?.streakCount ?: 27,
                progressPercentage = profile?.completedQuota ?: 67,
                onCheckIn = { viewModel.checkInAndBoostStreak() }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // --- Today's Plan Section ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Plan",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "View Plan",
                    color = PrimaryNeon,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateToTab(3) } // Goes to Learn screen
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(planList) { item ->
            StudyPlanRowItem(
                item = item,
                onToggleComplete = { viewModel.toggleStudyPlanCompleted(item) }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // --- Quick Access Grid ---
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Access",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Edit",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            QuickAccessGrid(
                onSolverClick = { onNavigateToTab(3) }, // Learn tab
                onMockClick = { onNavigateToTab(2) },   // New Test tab
                onFormulaBookClick = { showFormulaBook = true },
                onFlashcardClick = { showFlashcards = true },
                onStudyBattleClick = { showStudyBattle = true }
            )
        }
    }

    // --- Overlay popups for features ---
    if (showFormulaBook) {
        FormulaBookDialog(onDismiss = { showFormulaBook = false })
    }

    if (showFlashcards) {
        FlashcardsDialog(onDismiss = { showFlashcards = false })
    }

    if (showStudyBattle) {
        StudyBattleDialog(onDismiss = { showStudyBattle = false })
    }
}

@Composable
fun HeaderSection(userName: String, points: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Welcome back,",
                color = AccentPurple,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$userName Sharma",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "\"Success is not final; failure is not fatal: it is the courage to continue that counts.\"",
                color = TextSecondary,
                fontSize = 10.sp,
                lineHeight = 13.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Profile Avatar Indicator with Points tag
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // Points bubble
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceCard)
                    .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "🔥 ", fontSize = 12.sp)
                Text(
                    text = "14 Days",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.sweepGradient(
                            colors = listOf(PrimaryNeon, SecondaryNeon, PrimaryNeon)
                        )
                    )
                    .border(1.5.dp, Color.White.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile avatar",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun StreakCard(
    streakCount: Int,
    progressPercentage: Int,
    onCheckIn: () -> Unit
) {
    var checkInDone by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0x55581C87), // Purple-900 / 40% alpha
                        Color(0x226B21A8), // Purple-800 / 20% alpha
                        Color(0xFF0F172A)  // Slate-900 / solid
                    )
                )
            )
            .border(1.2.dp, PrimaryNeon.copy(alpha = 0.35f), RoundedCornerShape(28.dp))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Text Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "JEE Preparation",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$progressPercentage% Syllabus Completed",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                
                Spacer(modifier = Modifier.height(14.dp))
                // Interactive Check-In button to boost gamification streak!
                Button(
                    onClick = {
                        onCheckIn()
                        checkInDone = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (checkInDone) Color(0xFF1E293B) else PrimaryNeon
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.height(34.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = if (checkInDone) "Streak Active" else "Resume Mock Test",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Glowing Circular Ring Visualization
            Box(
                modifier = Modifier
                    .size(90.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background track
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Color(0xFF1E1E24),
                        radius = size.minDimension / 2,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Progress Indicator arc
                val animatedProgress by animateFloatAsState(
                    targetValue = progressPercentage.toFloat() / 100f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
                    label = "streak_progress"
                )
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(PrimaryNeon, SecondaryNeon, PrimaryNeon)
                        ),
                        startAngle = -90f,
                        sweepAngle = animatedProgress * 360f,
                        useCenter = false,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Inner content
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text(
                        text = "$progressPercentage%",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "OVERALL",
                        color = TextSecondary,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
fun StudyPlanRowItem(
    item: StudyPlanItem,
    onToggleComplete: () -> Unit
) {
    val subjectColor = Color(item.hexColor.drop(2).toLong(16))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Time Label
        Text(
            text = item.timeLabel,
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(60.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Divider
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(subjectColor)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Subject content text
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.subjectName,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = item.topicName,
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        // Action Checkbox (represented by a custom play/completed state circle indicator)
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (item.isCompleted) NeonGreen.copy(alpha = 0.2f) else PrimaryNeon.copy(alpha = 0.1f))
                .border(
                    1.dp,
                    if (item.isCompleted) NeonGreen else PrimaryNeon.copy(alpha = 0.5f),
                    CircleShape
                )
                .clickable { onToggleComplete() }
                .testTag("todo_check_${item.id}"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (item.isCompleted) Icons.Default.Check else Icons.Default.PlayArrow,
                contentDescription = "Toggle completion",
                tint = if (item.isCompleted) NeonGreen else PrimaryNeon,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun QuickAccessGrid(
    onSolverClick: () -> Unit,
    onMockClick: () -> Unit,
    onFormulaBookClick: () -> Unit,
    onFlashcardClick: () -> Unit,
    onStudyBattleClick: () -> Unit
) {
    val items = listOf(
        QuickAccessItemData("AI Doubt Solver", Icons.Filled.SmartToy, "0xFFFF4500", onSolverClick),
        QuickAccessItemData("Mock Tests", Icons.Filled.Assignment, "0xFF7F4FFF", onMockClick),
        QuickAccessItemData("PYQ Archive", Icons.Filled.Restore, "0xFFFFAA00", onFormulaBookClick), // formula book maps easily
        QuickAccessItemData("Flashcards", Icons.Filled.CardMembership, "0xFF00E676", onFlashcardClick),
        QuickAccessItemData("Formula Book", Icons.Filled.MenuBook, "0xFF00E5FF", onFormulaBookClick),
        QuickAccessItemData("Study Battle", Icons.Filled.Quiz, "0xFFFF1744", onStudyBattleClick)
    )

    Column {
        for (i in items.indices step 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    QuickAccessGridCard(item = items[i])
                }
                if (i + 1 < items.size) {
                    Box(modifier = Modifier.weight(1f)) {
                        QuickAccessGridCard(item = items[i + 1])
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

data class QuickAccessItemData(
    val title: String,
    val icon: ImageVector,
    val hexColor: String,
    val onClick: () -> Unit
)

@Composable
fun QuickAccessGridCard(item: QuickAccessItemData) {
    val color = Color(item.hexColor.drop(2).toLong(16))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .clickable { item.onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = item.title,
            color = TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}


// --- Overlay Feature Dialogs ---

@Composable
fun FormulaBookDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, PrimaryNeon)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Formula Book",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier
                            .clickable { onDismiss() }
                            .size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                val formulaList = listOf(
                    "Centripetal Force: Fc = m·v² / r",
                    "Electrostatic Force: Fe = k·q1·q2 / r²",
                    "Schrödinger Wave: Hψ = Eψ",
                    "Gibbs Free Energy: ΔG = ΔH - TΔS",
                    "Ideal Gas Law: P·V = n·R·T",
                    "Compound Interest: A = P(1 + r/n)^(nt)"
                )

                LazyColumn(modifier = Modifier.height(200.dp)) {
                    items(formulaList) { formula ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(text = formula, color = PrimaryNeon, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FlashcardsDialog(onDismiss: () -> Unit) {
    var step by remember { mutableStateOf(0) }
    val cards = listOf(
        Pair("What is Newton's Second Law of Motion?", "F = m · a (Force is mass times acceleration)"),
        Pair("Define Heisenberg's Uncertainty Principle.", "Δx · Δp ≥ h / 4π (Cannot measure both position and momentum exactly)"),
        Pair("What is the hybridization of carbon in methane?", "sp³ hybridization with 109.5° bond angle.")
    )

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, PrimaryNeon)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Interactive Flashcards",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier
                            .clickable { onDismiss() }
                            .size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))

                var isFlipped by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isFlipped) Color(0xFF1B203E) else Color(0xFF14192B))
                        .border(1.dp, if (isFlipped) SecondaryNeon else BorderColor, RoundedCornerShape(12.dp))
                        .clickable { isFlipped = !isFlipped }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (!isFlipped) cards[step].first else cards[step].second,
                        color = if (isFlipped) SecondaryNeon else TextPrimary,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Tap to Flip",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            isFlipped = false
                            step = if (step > 0) step - 1 else cards.size - 1
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BorderColor)
                    ) {
                        Text("Prev", color = TextPrimary)
                    }

                    Text(
                        text = "${step + 1} / ${cards.size}",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )

                    Button(
                        onClick = {
                            isFlipped = false
                            step = if (step < cards.size - 1) step + 1 else 0
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon)
                    ) {
                        Text("Next")
                    }
                }
            }
        }
    }
}

@Composable
fun StudyBattleDialog(onDismiss: () -> Unit) {
    var score by remember { mutableStateOf(0) }
    var questionNum by remember { mutableStateOf(1) }

    val questions = listOf(
        Triple("Benzene undergoes which reaction easily?", listOf("Nucleophilic addition", "Electrophilic substitution", "Free radical addition"), 1),
        Triple("What is the dimensions of angular momentum?", listOf("ML²T⁻¹", "MLT⁻²", "ML²T⁻²"), 0),
        Triple("Derivative of cot(x) with respect to x is:", listOf("cosec²(x)", "-cosec²(x)", "sec²(x)"), 1)
    )

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, SecondaryNeon)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Study Battle Arena ⚔️",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier
                            .clickable { onDismiss() }
                            .size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (questionNum <= questions.size) {
                    val currentQuestion = questions[questionNum - 1]

                    Text(
                        text = "Question $questionNum of ${questions.size}",
                        color = SecondaryNeon,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = currentQuestion.first,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    currentQuestion.second.forEachIndexed { index, option ->
                        Button(
                            onClick = {
                                if (index == currentQuestion.third) {
                                    score += 20
                                }
                                questionNum++
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, BorderColor)
                        ) {
                            Text(text = option, color = TextPrimary, fontSize = 13.sp)
                        }
                    }
                } else {
                    Text(text = "Battle Complete!", color = NeonGreen, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "Final Quiz Score: $score points earned 🎉", color = TextPrimary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { onDismiss() },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon)
                    ) {
                        Text("Claim Points & Leave")
                    }
                }
            }
        }
    }
}
