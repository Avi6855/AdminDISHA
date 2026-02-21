package com.avinashpatil.app.admindisha.screens.dishaAI

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

class DishaAIScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            DishaAIScreen(navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishaAIScreen(navController: NavController) {
    val context = LocalContext.current
    var chatInput by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf<Message>()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top App Bar - updated to match AllStudentScreen style
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF6B7280),
                                Color(0xFF4B5563)
                            )
                        )
                    )
                    .shadow(8.dp, shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "DISHA AI",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Button(
                        onClick = { messages = emptyList() },
                        modifier = Modifier.padding(end = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            text = "+ New Chat",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Chat Messages List
            ChatMessagesList(
                messages = messages,
                onEditPrompt = { chatInput = it },
                onCopyPrompt = { copyToClipboard(context, it, "Prompt copied") },
                onRegenerate = { prompt ->
                    messages = messages.dropLast(1)
                    sendMessage(prompt, messages) { newMessages ->
                        messages = newMessages
                    }
                },
                onCopyAnswer = { copyToClipboard(context, it, "Answer copied") }
            )
        }

        // Chat Input Field - moved to the bottom of the screen
        ChatInputField(
            chatInput = chatInput,
            onChatInputChange = { chatInput = it },
            onSend = {
                if (chatInput.isNotBlank()) {
                    sendMessage(chatInput, messages) { newMessages ->
                        messages = newMessages
                    }
                    chatInput = ""
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun ChatMessagesList(
    messages: List<Message>,
    onEditPrompt: (String) -> Unit,
    onCopyPrompt: (String) -> Unit,
    onRegenerate: (String) -> Unit,
    onCopyAnswer: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding( bottom = 72.dp), // Added bottom padding for ChatInputField
        verticalArrangement = Arrangement.Top
    ) {
        item {
            Text(
                text = "Welcome to DISHA AI",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E88E5),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                textAlign = TextAlign.Center
            )
        }
        items(messages) { message ->
            ChatMessageItem(
                message = message,
                onEditPrompt = onEditPrompt,
                onCopyPrompt = onCopyPrompt,
                onRegenerate = onRegenerate,
                onCopyAnswer = onCopyAnswer
            )
        }
    }
}

@Composable
private fun ChatMessageItem(
    message: Message,
    onEditPrompt: (String) -> Unit,
    onCopyPrompt: (String) -> Unit,
    onRegenerate: (String) -> Unit,
    onCopyAnswer: (String) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    var showTextOptions by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        if (message.isUser) {
            UserMessage(
                message = message,
                onEditPrompt = onEditPrompt,
                onCopyPrompt = onCopyPrompt,
                scale = scale
            )
        } else {
            AIMessage(
                message = message,
                onRegenerate = onRegenerate,
                onCopyAnswer = onCopyAnswer,
                scale = scale,
                showTextOptions = showTextOptions,
                onShowTextOptionsChanged = { showTextOptions = it }
            )
        }
    }
}

@Composable
private fun UserMessage(
    message: Message,
    onEditPrompt: (String) -> Unit,
    onCopyPrompt: (String) -> Unit,
    scale: Float
) {
    Text(
        text = "You: ${message.prompt}",
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        color = Color.Black,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFE0F7FA),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.End
    ) {
        AnimatedButton(
            text = "Edit Prompt",
            onClick = { onEditPrompt(message.prompt) },
            scale = scale
        )
        Spacer(modifier = Modifier.width(8.dp))
        AnimatedButton(
            text = "Copy Prompt",
            onClick = { onCopyPrompt(message.prompt) },
            scale = scale
        )
    }
}

@Composable
private fun AIMessage(
    message: Message,
    onRegenerate: (String) -> Unit,
    onCopyAnswer: (String) -> Unit,
    scale: Float,
    showTextOptions: Boolean,
    onShowTextOptionsChanged: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFFFF3E0),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { onShowTextOptionsChanged(true) },
                        onPress = { onShowTextOptionsChanged(false) }
                    )
                }
        ) {
            SelectionContainer {
                Text(
                    text = "DISHA AI: ${message.response}",
                    fontSize = 16.sp,
                    color = Color.Black,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        if (showTextOptions) {
            TextOptionsMenu(
                message = message,
                onDismiss = { onShowTextOptionsChanged(false) },
                onCopyAnswer = onCopyAnswer
            )
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.End
    ) {
        AnimatedButton(
            text = "Regenerate",
            onClick = { onRegenerate(message.prompt) },
            scale = scale
        )
        Spacer(modifier = Modifier.width(8.dp))
        AnimatedButton(
            text = "Copy Answer",
            onClick = { onCopyAnswer(message.response) },
            scale = scale
        )
    }
}

@Composable
private fun TextOptionsMenu(
    message: Message,
    onDismiss: () -> Unit,
    onCopyAnswer: (String) -> Unit
) {
    DropdownMenu(
        expanded = true,
        onDismissRequest = onDismiss,
        modifier = Modifier.background(Color.White),
        properties = PopupProperties(focusable = true)
    ) {
        DropdownMenuItem(
            text = { Text("Copy") },
            onClick = {
                onCopyAnswer(message.response)
                onDismiss()
            },
            leadingIcon = {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = "Copy"
                )
            }
        )
        DropdownMenuItem(
            text = { Text("Select All") },
            onClick = onDismiss,
            leadingIcon = {
                Icon(
                    Icons.Default.SelectAll,
                    contentDescription = "Select All"
                )
            }
        )
    }
}

@Composable
private fun ChatInputField(
    chatInput: String,
    onChatInputChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(24.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = chatInput,
            onValueChange = onChatInputChange,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp),
            textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
            decorationBox = { innerTextField ->
                Box {
                    if (chatInput.isEmpty()) {
                        Text(
                            text = "Ask DISHA AI",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                    innerTextField()
                }
            }
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = onSend) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                tint = if (chatInput.isNotBlank()) Color(0xFF1E88E5) else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun AnimatedButton(
    text: String,
    onClick: () -> Unit,
    scale: Float
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .scale(scale)
            .padding(horizontal = 4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF1E88E5),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text = text, fontSize = 14.sp)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun DishaAIScreenPreview() {
    val navController = rememberNavController()
    DishaAIScreen(navController = navController)
}

// Non-composable functions
data class Message(val prompt: String, val response: String, val isUser: Boolean)

fun sendMessage(prompt: String, currentMessages: List<Message>, updateMessages: (List<Message>) -> Unit) {
    val newMessages = currentMessages.toMutableList()
    newMessages.add(Message(prompt, "", true))
    val response = fetchGeminiResponse(prompt)
    newMessages.add(Message(prompt, response, false))
    updateMessages(newMessages)
}

fun fetchGeminiResponse(prompt: String): String {
    return "This is a mock response from DISHA AI for the prompt: \"$prompt\""
}

fun copyToClipboard(context: Context, text: String, toastMessage: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Copied Text", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
}