package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.example.viewmodel.MainViewModel
import com.example.ui.theme.*
import com.example.data.TestItem

@Composable
fun TestFlowScreen(viewModel: MainViewModel) {
    val activeStep by remember { derivedStateOf { viewModel.testFlowStep.value } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Shared Screen Title Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (activeStep > 1) {
                IconButton(onClick = { viewModel.backToWizardInput() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
            }
            Text(
                text = "New Test",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = if (activeStep > 1) TextAlign.Start else TextAlign.Center
            )
        }

        // 1. Shared High-Fidelity Steps Progress Bar (1 -> 2 -> 3)
        WizardStepsBar(currentStep = activeStep)

        Spacer(modifier = Modifier.height(16.dp))

        // Screen routing based on steps
        Box(modifier = Modifier.fillMaxSize()) {
            when (activeStep) {
                1 -> StepOneWizard(viewModel)
                2 -> StepTwoTestSelection(viewModel)
                3 -> StepThreeTestFinished(viewModel)
            }
        }
    }
}

@Composable
fun WizardStepsBar(currentStep: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Node 1
        StepNode(
            stepNumber = 1,
            label = "Stream & Type",
            isActive = currentStep == 1,
            isCompleted = currentStep > 1
        )

        // Line 1
        Box(
            modifier = Modifier
                .weight(1f)
                .height(2.dp)
                .background(if (currentStep > 1) PrimaryNeon else BorderColor)
                .padding(horizontal = 4.dp)
        )

        // Node 2
        StepNode(
            stepNumber = 2,
            label = "Test Selection",
            isActive = currentStep == 2,
            isCompleted = currentStep > 2
        )

        // Line 2
        Box(
            modifier = Modifier
                .weight(1f)
                .height(2.dp)
                .background(if (currentStep > 2) PrimaryNeon else BorderColor)
                .padding(horizontal = 4.dp)
        )

        // Node 3
        StepNode(
            stepNumber = 3,
            label = "Test Settings",
            isActive = currentStep == 3,
            isCompleted = false
        )
    }
}

@Composable
fun RowScope.StepNode(
    stepNumber: Int,
    label: String,
    isActive: Boolean,
    isCompleted: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(90.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isCompleted -> PrimaryNeon
                        isActive -> PrimaryNeon.copy(alpha = 0.2f)
                        else -> BorderColor
                    }
                )
                .border(
                    1.5.dp,
                    if (isActive || isCompleted) PrimaryNeon else Color.Transparent,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            } else {
                Text(
                    text = "$stepNumber",
                    color = if (isActive) PrimaryNeon else TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = if (isActive || isCompleted) PrimaryNeon else TextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

// --- SCREEN step 1 implementation ---
@Composable
fun StepOneWizard(viewModel: MainViewModel) {
    val stream by remember { derivedStateOf { viewModel.wizardStream.value } }
    val examType by remember { derivedStateOf { viewModel.wizardExamType.value } }
    val subjects by remember { derivedStateOf { viewModel.wizardSubjects.value } }
    val testType by remember { derivedStateOf { viewModel.wizardTestType.value } }
    val difficulty by remember { derivedStateOf { viewModel.wizardDifficulty.value } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // 1. Stream Selection
        item {
            Text(
                text = "Choose your stream",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Select the stream you want to test in",
                color = TextSecondary,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StreamCardItem(
                    title = "JEE",
                    description = "Engineering\nEntrance",
                    icon = Icons.Default.Memory,
                    isSelected = stream == "JEE",
                    onSelect = { viewModel.setWizardStreamSelection("JEE") },
                    modifier = Modifier.weight(1f).testTag("select_jee")
                )
                StreamCardItem(
                    title = "NEET",
                    description = "Medical\nEntrance",
                    icon = Icons.Default.LocalHospital,
                    isSelected = stream == "NEET",
                    onSelect = { viewModel.setWizardStreamSelection("NEET") },
                    modifier = Modifier.weight(1f).testTag("select_neet")
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // 2. Select Exam Type
        item {
            Text(
                text = "Select Exam Type",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            val exams = if (stream == "JEE") listOf("JEE Main", "JEE Advanced") else listOf("NEET UG")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                exams.forEach { exam ->
                    val selected = examType == exam
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (selected) PrimaryNeon else SurfaceCard)
                            .border(1.dp, if (selected) PrimaryNeon else BorderColor, RoundedCornerShape(20.dp))
                            .clickable { viewModel.wizardExamType.value = exam }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = exam,
                            color = if (selected) Color.White else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // 3. Choose Subjects
        item {
            Text(
                text = "Choose Subject(s)",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "You can select multiple subjects",
                color = TextSecondary,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            val availSubjects = if (stream == "JEE") listOf("Physics", "Chemistry", "Maths") else listOf("Physics", "Chemistry", "Biology")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availSubjects.forEach { sub ->
                    val isChecked = subjects.contains(sub)
                    val icon = when (sub) {
                        "Physics" -> Icons.Default.Science
                        "Chemistry" -> Icons.Default.BlurOn
                        "Maths" -> Icons.Default.Functions
                        else -> Icons.Default.Spa
                    }
                    Box(
                        modifier = Modifier
                            .weight(1F)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceCard)
                            .border(
                                1.dp,
                                if (isChecked) PrimaryNeon else BorderColor,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.toggleSubjectSelected(sub) }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isChecked) PrimaryNeon else BorderColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = sub,
                                    tint = if (isChecked) Color.White else TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = sub,
                                color = if (isChecked) TextPrimary else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Selected Corner Indicator Dot
                        if (isChecked) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(NeonGreen)
                                    .align(Alignment.TopEnd)
                                    .border(1.dp, Color.Black, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "checked",
                                    tint = Color.Black,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // 4. Select Test Type
        item {
            Text(
                text = "Select Test Type",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            val testTypes = listOf("Full Syllabus Test", "Chapter Test", "Topic Test", "Previous Year Test")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                for (j in testTypes.indices step 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (k in 0..1) {
                            if (j + k < testTypes.size) {
                                val t = testTypes[j + k]
                                val isSelected = testType == t
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SurfaceCard)
                                        .border(
                                            1.dp,
                                            if (isSelected) PrimaryNeon else BorderColor,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { viewModel.wizardTestType.value = t }
                                        .padding(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isSelected) Icons.Default.AssignmentTurnedIn else Icons.Default.Assignment,
                                            contentDescription = "TestType",
                                            tint = if (isSelected) PrimaryNeon else TextSecondary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = t,
                                            color = if (isSelected) TextPrimary else TextSecondary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // 5. Select Difficulty
        item {
            Text(
                text = "Select Difficulty",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("Easy", "Medium", "Hard").forEach { diff ->
                    val selected = difficulty == diff
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) PrimaryNeon else SurfaceCard)
                            .border(1.dp, if (selected) PrimaryNeon else BorderColor, RoundedCornerShape(8.dp))
                            .clickable { viewModel.wizardDifficulty.value = diff }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = diff,
                            color = if (selected) Color.White else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
        }

        // 6. Primary Continue Gradient Button
        item {
            Button(
                onClick = { viewModel.continueToTestSelection() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(25.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(PrimaryNeon, SecondaryNeon)
                        )
                    )
                    .testTag("submit_wizard_btn"),
                contentPadding = PaddingValues(0.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Continue",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Forward arrow",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StreamCardItem(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .border(
                1.5.dp,
                if (isSelected) PrimaryNeon else BorderColor,
                RoundedCornerShape(16.dp)
            )
            .clickable { onSelect() }
            .padding(16.dp)
    ) {
        Column {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) PrimaryNeon else TextSecondary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 14.sp
            )
        }

        // Checklist confirmation circle
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(if (isSelected) PrimaryNeon else Color.Transparent)
                .border(1.dp, if (isSelected) PrimaryNeon else BorderColor, CircleShape)
                .align(Alignment.TopEnd),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "checked",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

// --- SCREEN step 2 mock tests selection (Matches screen 3 reference) ---
@Composable
fun StepTwoTestSelection(viewModel: MainViewModel) {
    val stream by remember { derivedStateOf { viewModel.wizardStream.value } }
    val examType by remember { derivedStateOf { viewModel.wizardExamType.value } }
    val subjects by remember { derivedStateOf { viewModel.wizardSubjects.value } }
    val testType by remember { derivedStateOf { viewModel.wizardTestType.value } }
    val difficulty by remember { derivedStateOf { viewModel.wizardDifficulty.value } }

    val testItemsList by viewModel.testItems.collectAsState()
    val activeFilter by remember { derivedStateOf { viewModel.testListFilter.value } }

    // Filter list according to selection, tags and active filter
    val filteredTests = remember(testItemsList, examType, activeFilter) {
        testItemsList.filter { test ->
            test.examType == examType && (
                activeFilter == "All" ||
                test.badgeTag.lowercase() == activeFilter.lowercase() ||
                (activeFilter == "High Accuracy" && test.accuracyPercent >= 90)
            )
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // 1. Wizard Summary Indicator Box
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF291E4F), Color(0xFF141935))
                        )
                    )
                    .border(1.dp, PrimaryNeon.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "$examType • $testType",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${subjects.joinToString(", ")} • $difficulty",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(PrimaryNeon.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Subject,
                            contentDescription = "Subjects Summary",
                            tint = PrimaryNeon,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // 2. Filter Title
        item {
            Text(
                text = "Choose Test",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // 3. Filter Chips (All, Recommended, Trending, High Accuracy) Method
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Recommended", "Trending", "High Accuracy").forEach { filter ->
                    item {
                        val isSelected = activeFilter == filter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) PrimaryNeon else SurfaceCard)
                                .border(1.dp, if (isSelected) PrimaryNeon else BorderColor, RoundedCornerShape(16.dp))
                                .clickable { viewModel.testListFilter.value = filter }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = filter,
                                color = if (isSelected) Color.White else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 4. Test Card List
        if (filteredTests.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.AssignmentLate,
                        contentDescription = "Empty list",
                        tint = TextSecondary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No tests match active criteria for $examType.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(filteredTests) { test ->
                TestCardItem(
                    test = test,
                    onClick = {
                        viewModel.takeActiveTest(
                            testTitle = test.title,
                            examType = test.examType,
                            category = test.badgeTag
                        )
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // 5. Settings continue action button
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (filteredTests.isNotEmpty()) {
                        val first = filteredTests.first()
                        viewModel.takeActiveTest(first.title, first.examType, first.badgeTag)
                    } else {
                        viewModel.takeActiveTest("Syllabus Review Alpha-1", examType, "Recommended")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    text = "Continue to Settings →",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TestCardItem(test: TestItem, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Badge
                if (test.badgeTag.isNotEmpty()) {
                    val badgeColor = when (test.badgeTag) {
                        "Recommended" -> Color(0xFFE65100)
                        "Trending" -> Color(0xFF6A1B9A)
                        else -> Color(0xFFADFFF0)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(badgeColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = test.badgeTag,
                            color = if (test.badgeTag == "High Accuracy") NeonCyan else badgeColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    text = test.title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${test.questionsCount} Questions • ${test.marks} Marks • ${test.durationMins} mins",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Right Info section
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${test.accuracyPercent}%",
                        color = NeonGreen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Accuracy",
                        color = TextSecondary,
                        fontSize = 9.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${test.studentsCount} Students",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Select",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// --- SCREEN step 3 test finished screen ---
@Composable
fun StepThreeTestFinished(viewModel: MainViewModel) {
    val results by viewModel.testResults.collectAsState()
    val activeResult = results.firstOrNull()

    var showAiExplanation by remember { mutableStateOf(false) }
    val explanationText by viewModel.aiSolverResponse

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(NeonGreen.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Success Trophy",
                    tint = NeonGreen,
                    modifier = Modifier.size(50.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Test Completed!",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "You earned +100 points coins! 🪙",
                color = GoldYellow,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .border(1.dp, BorderColor, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = activeResult?.testTitle ?: "Full Syllabus Test Solver",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Exam Environment: ${activeResult?.examType ?: "JEE Main"}",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )

                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = BorderColor)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Correct Answers",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "${activeResult?.correctCount ?: 0} / ${activeResult?.totalCount ?: 0}",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Score Percentage",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "${activeResult?.percentage ?: 0}%",
                                color = NeonGreen,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Smart Weak Chapter Detection + AI Explanation segment
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PrimaryNeon.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141935)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "AI solver feedback",
                            tint = PrimaryNeon,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Smart AI Recommendations",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Let Gemini analyze your weak areas and generate automated physics & chemistry revision guide cards.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (viewModel.aiSolverLoading.value) {
                        CircularProgressIndicator(
                            color = PrimaryNeon,
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    } else if (showAiExplanation) {
                        Text(
                            text = explanationText,
                            color = TextPrimary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Button(
                        onClick = {
                            showAiExplanation = true
                            viewModel.aiSolverQuery.value = "Review results for test: ${activeResult?.testTitle ?: "Full Test"}. Student scored ${activeResult?.percentage ?: 88}% correct Answers. What 2 critical chapters should they revise next and why?"
                            viewModel.solveDoubtWithAI()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = if (showAiExplanation) "Re-Analyze with Gemini" else "Prompt AI Weak Chapter Review",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Back to Start Action
        item {
            Spacer(modifier = Modifier.height(28.dp))
            Button(
                onClick = { viewModel.restartTestFlow() },
                colors = ButtonDefaults.buttonColors(containerColor = BorderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    text = "Build New Custom Test Plan",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
