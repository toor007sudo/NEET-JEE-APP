package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.MainViewModel
import com.example.ui.theme.*

@Composable
fun LearnScreen(viewModel: MainViewModel) {
    val solverQuery by viewModel.aiSolverQuery
    val solverResult by viewModel.aiSolverResponse
    val solverLoading by viewModel.aiSolverLoading

    val plannerResult by viewModel.aiGeneratedPlan
    val plannerLoading by viewModel.aiPlanLoading

    val chapterReview by viewModel.aiChapterReport
    val chapterLoading by viewModel.aiChapterLoading

    var activeSubTab by remember { mutableStateOf("Doubt Solver") } // "Doubt Solver", "Study Planner", "Weak Chapter Matrix"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
    ) {
        // AI Title Bar
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(PrimaryNeon, SecondaryNeon)
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(45.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "AI Expert",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "My Study Room AI Assistant",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Powered by Google Gemini 3.5 Flash",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Sub Navigation Pill Tabs
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceCard)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Doubt Solver", "Study Planner", "Chapter Matrix").forEach { tab ->
                    val selected = activeSubTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) PrimaryNeon else Color.Transparent)
                            .clickable { activeSubTab = tab }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            color = if (selected) Color.White else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // TAB ACTIONS
        when (activeSubTab) {
            "Doubt Solver" -> {
                item {
                    Text(
                        text = "AI Doubt Solver",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Type any JEE/NEET doubt or choose a sample diagram to simulate visual input:",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    TextField(
                        value = solverQuery,
                        onValueChange = { viewModel.aiSolverQuery.value = it },
                        placeholder = { Text("E.g., What is the dipole moment of ammonia vs NF3?", color = TextSecondary) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = SurfaceCard,
                            unfocusedContainerColor = SurfaceCard,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedIndicatorColor = PrimaryNeon,
                            unfocusedIndicatorColor = BorderColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                            .testTag("ai_doubt_query_field")
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Simulated Multimodal Image Attachments Selection
                    Text(
                        text = "Simulate Image Snap (Select sample doubt blueprint):",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MockImageSelector(
                            label = "Electrostatics Circuit Drawing",
                            isSelected = viewModel.aiSolverImgBase64.value != null && viewModel.aiSolverQuery.value.contains("circuit"),
                            onSelect = {
                                viewModel.aiSolverImgBase64.value = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=" // Sample tiny 1x1 base64 png
                                viewModel.aiSolverQuery.value = "Identify the effective capacitance between terminals A and B for this circuit schematic containing 5 capacitors of 10μF each."
                            }
                        )
                        MockImageSelector(
                            label = "Chemical Benzene Mechanism",
                            isSelected = viewModel.aiSolverImgBase64.value != null && viewModel.aiSolverQuery.value.contains("Benzene"),
                            onSelect = {
                                viewModel.aiSolverImgBase64.value = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=" // Sample base64
                                viewModel.aiSolverQuery.value = "Solve this Organic Mechanism query: complete the reaction stages of Benzene undergoing Nitration in sulfuric acid on this chemistry flowchart."
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.solveDoubtWithAI() },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("ai_solve_btn"),
                        enabled = !solverLoading
                    ) {
                        if (solverLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Send, contentDescription = "Solve", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ask Gemini Specialist", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (solverResult.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, BorderColor, RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Gemini AI Solution:",
                                    color = NeonGreen,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = solverResult,
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            "Study Planner" -> {
                item {
                    Text(
                        text = "AI Personalized 7-Day Scheduler",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Click to synthesize a custom study list based on your dynamic physics/chemistry weak areas.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.generatePersonalizedStudyPlan() },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        enabled = !plannerLoading
                    ) {
                        if (plannerLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Generate 7-Day Smart Schedule", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (plannerResult.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, BorderColor, RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Automated Study Tracker Guide:",
                                    color = NeonCyan,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = plannerResult,
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            "Chapter Matrix" -> {
                item {
                    Text(
                        text = "Weak Chapter Diagnostic Matrix",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "We run diagnostics across your NEET/JEE analytics history to isolate traps and construct remedial items.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.analyzeWeaknessFromHistory() },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        enabled = !chapterLoading
                    ) {
                        if (chapterLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Isolate Syllabus Weak Holes", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (chapterReview.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, BorderColor, RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Trap Analysis Review Card:",
                                    color = GoldYellow,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = chapterReview,
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.MockImageSelector(
    label: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) PrimaryNeon.copy(alpha = 0.2f) else SurfaceCard)
            .border(1.dp, if (isSelected) PrimaryNeon else BorderColor, RoundedCornerShape(8.dp))
            .clickable { onSelect() }
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.PhotoCamera,
                contentDescription = "Diagram",
                tint = if (isSelected) PrimaryNeon else TextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                color = if (isSelected) TextPrimary else TextSecondary,
                fontSize = 9.sp,
                textAlign = TextAlign.Center,
                lineHeight = 11.sp
            )
        }
    }
}
