package com.example.ui

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.BorderStroke
import com.example.viewmodel.MainViewModel
import com.example.ui.theme.*
import com.example.data.DoubtThread
import com.example.data.ThreadReply

@Composable
fun CommunityScreen(viewModel: MainViewModel) {
    val threads by viewModel.doubtThreads.collectAsState()
    val activeThreadId by viewModel.activeThreadId.collectAsState()
    val activeReplies by viewModel.activeReplies.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        if (activeThreadId == null) {
            // Main Threads List Screen
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Discussion Lounge",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier
                        .background(PrimaryNeon.copy(alpha = 0.2f), CircleShape)
                        .size(36.dp)
                        .testTag("add_doubt_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddComment,
                        contentDescription = "Post doubt",
                        tint = PrimaryNeon,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(threads) { thread ->
                    ThreadCard(
                        thread = thread,
                        onClick = { viewModel.selectActiveThread(thread) }
                    )
                }
            }
        } else {
            // Expanded Thread Message Screen
            val thread = threads.firstOrNull { it.id == activeThreadId }
            if (thread != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.clearActiveThread() }) {
                        // Resets to thread selection
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                    Text(
                        text = "Thread Details",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                ThreadDetailsView(
                    thread = thread,
                    replies = activeReplies,
                    onSendReply = { content -> viewModel.replyToThread(thread.id, content) }
                )
            }
        }
    }

    if (showCreateDialog) {
        CreateDoubtDialog(
            onSubmit = { title, content, subject, askAi ->
                viewModel.submitNewDoubt(title, content, subject, askAi)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }
}

@Composable
fun ThreadCard(thread: DoubtThread, onClick: () -> Unit) {
    val subColor = when (thread.subject) {
        "Physics" -> Color(0xFF7F4FFF)
        "Chemistry" -> Color(0xFF00E5FF)
        "Maths" -> Color(0xFFFFB300)
        else -> Color(0xFF00E676)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(subColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = thread.subject,
                        color = subColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "by ${thread.authorName}",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = thread.title,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = thread.content,
                color = TextSecondary,
                fontSize = 12.sp,
                maxLines = 2,
                lineHeight = 15.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Forum,
                    contentDescription = "Replies icon",
                    tint = PrimaryNeon,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${thread.repliesCount} replies",
                    color = PrimaryNeon,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ThreadDetailsView(
    thread: DoubtThread,
    replies: List<ThreadReply>,
    onSendReply: (String) -> Unit
) {
    var replyText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Opener Question Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderColor, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryNeon.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Author",
                                    tint = PrimaryNeon,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = thread.authorName,
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = thread.subject,
                                    color = PrimaryNeon,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = thread.title,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = thread.content,
                            color = TextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Discussion Thread (${replies.size}):",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Replies
            if (replies.isEmpty()) {
                item {
                    Text(
                        text = "No replies yet. Be the first to answer this doubt!",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(replies) { reply ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (reply.isAi) Color(0xFF141935) else SurfaceCard)
                            .border(
                                1.dp,
                                if (reply.isAi) PrimaryNeon.copy(alpha = 0.4f) else BorderColor,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (reply.isAi) Icons.Default.SmartToy else Icons.Default.AccountCircle,
                                    contentDescription = "User",
                                    tint = if (reply.isAi) SecondaryNeon else TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = reply.authorName,
                                    color = if (reply.isAi) SecondaryNeon else TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (reply.isAi) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(SecondaryNeon.copy(alpha = 0.2f))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text("AI Mentor Response", color = SecondaryNeon, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = reply.content,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }

        // Static Reply Field Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceCard)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = replyText,
                onValueChange = { replyText = it },
                placeholder = { Text("Write your reply answer...", color = TextSecondary, fontSize = 12.sp) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                    unfocusedContainerColor = Color.Black.copy(alpha = 0.2f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .testTag("reply_input_field"),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    onSendReply(replyText)
                    replyText = ""
                },
                modifier = Modifier
                    .background(PrimaryNeon, CircleShape)
                    .size(36.dp)
                    .testTag("reply_submit_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun CreateDoubtDialog(
    onSubmit: (String, String, String, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Physics") }
    var askAi by remember { mutableStateOf(true) }

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
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Post an academic doubt",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Title
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("Short question summary...", color = TextSecondary) },
                    colors = TextFieldDefaults.colors(focusedContainerColor = Color.Black.copy(alpha = 0.15f), unfocusedContainerColor = Color.Black.copy(alpha = 0.15f), focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Content
                TextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = { Text("Describe the specific problem or formula stages...", color = TextSecondary) },
                    colors = TextFieldDefaults.colors(focusedContainerColor = Color.Black.copy(alpha = 0.15f), unfocusedContainerColor = Color.Black.copy(alpha = 0.15f), focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                    modifier = Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(6.dp))
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Subject Select
                Text(text = "Subject Category:", color = TextSecondary, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Physics", "Chemistry", "Maths", "Biology").forEach { sub ->
                        val active = subject == sub
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (active) PrimaryNeon else BorderColor)
                                .clickable { subject = sub }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(text = sub, color = if (active) Color.White else TextSecondary, fontSize = 10.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))

                // Ask AI Auto trigger Checkbox
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = askAi,
                        onCheckedChange = { askAi = it },
                        colors = CheckboxDefaults.colors(checkedColor = PrimaryNeon)
                    )
                    Text(
                        text = "🤖 Ask Gemini to post background expert reply",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { onDismiss() }) {
                        Text("Cancel", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = { onSubmit(title, content, subject, askAi) },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon)
                    ) {
                        Text("Post Doubt")
                    }
                }
            }
        }
    }
}
