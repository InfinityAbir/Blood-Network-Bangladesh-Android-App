package com.bloodnetwork.bangladesh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.bloodnetwork.bangladesh.ui.i18n.tr
import com.bloodnetwork.bangladesh.ui.theme.BloodRed
import com.bloodnetwork.bangladesh.ui.viewmodel.ChatUiMessage
import com.bloodnetwork.bangladesh.ui.viewmodel.ChatbotViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatbotScreen(
    onBack: () -> Unit,
    vm: ChatbotViewModel,
    onSignIn: () -> Unit = {},
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    var input by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()

    // The user may sign in and come straight back to this screen: re-check on every resume.
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) { vm.refreshSignInState() }
    }

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) listState.animateScrollToItem(state.messages.lastIndex)
    }

    val canSend = input.isNotBlank() && !state.isLoading && state.cooldownSeconds == 0 && state.isSignedIn
    val send = {
        if (canSend) {
            vm.sendMessage(input)
            input = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tr("AI Assistant", "এআই সহকারী")) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = tr("Back", "পেছনে"))
                    }
                },
                actions = {
                    if (state.messages.isNotEmpty()) {
                        IconButton(onClick = { vm.clearChat() }, enabled = !state.isLoading) {
                            Icon(Icons.Filled.DeleteSweep, contentDescription = tr("Clear chat", "চ্যাট মুছুন"))
                        }
                    }
                },
            )
        },
        bottomBar = {
            if (state.isSignedIn) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                ) {
                    Text(
                        tr(
                            "AI assistant, not a doctor. Don't share personal or medical details.",
                            "এআই সহকারী, ডাক্তার নয়। ব্যক্তিগত বা চিকিৎসা-সংক্রান্ত তথ্য দেবেন না।",
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedTextField(
                            value = input,
                            onValueChange = { input = it.take(ChatbotViewModel.MAX_MESSAGE_LENGTH) },
                            modifier = Modifier.weight(1f).padding(vertical = 4.dp),
                            placeholder = {
                                Text(
                                    if (state.cooldownSeconds > 0) {
                                        tr("Wait ${state.cooldownSeconds}s…", "${state.cooldownSeconds} সেকেন্ড অপেক্ষা করুন…")
                                    } else {
                                        tr("Ask about blood donation...", "রক্তদান সম্পর্কে জিজ্ঞাসা করুন...")
                                    },
                                )
                            },
                            supportingText = if (input.length > ChatbotViewModel.MAX_MESSAGE_LENGTH - 100) {
                                { Text("${input.length}/${ChatbotViewModel.MAX_MESSAGE_LENGTH}") }
                            } else null,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = { send() }),
                            maxLines = 4,
                        )
                        IconButton(onClick = { send() }, enabled = canSend) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = tr("Send", "পাঠান"),
                                tint = if (canSend) BloodRed else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                            )
                        }
                    }
                }
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                !state.isSignedIn -> SignInPrompt(onSignIn)
                state.messages.isEmpty() -> ChatWelcome(onSuggestion = { vm.sendMessage(it) })
                else -> LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.messages, key = { it.id }) { msg ->
                        ChatBubble(
                            msg = msg,
                            errorText = state.error,
                            onRetry = { vm.retryLast() },
                            canRetry = !state.isLoading && state.cooldownSeconds == 0,
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SignInPrompt(onSignIn: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AssistantBadge(56)
        Spacer(Modifier.height(16.dp))
        Text(
            tr("Sign in to chat", "চ্যাট করতে সাইন ইন করুন"),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            tr(
                "The AI assistant is available to signed-in members so we can keep it free and fast for everyone.",
                "সবার জন্য সেবাটি বিনামূল্যে ও দ্রুত রাখতে এআই সহকারী শুধু সাইন-ইন করা সদস্যদের জন্য।",
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onSignIn,
            colors = ButtonDefaults.buttonColors(containerColor = BloodRed),
            shape = RoundedCornerShape(50),
        ) { Text(tr("Sign in", "সাইন ইন")) }
    }
}

@Composable
private fun ChatWelcome(onSuggestion: (String) -> Unit) {
    val suggestions = listOf(
        tr("Can I donate blood?", "আমি কি রক্ত দিতে পারি?"),
        tr("How often can I donate?", "কত দিন পরপর রক্ত দেওয়া যায়?"),
        tr("Which blood groups are compatible?", "কোন রক্তের গ্রুপ কার সাথে মেলে?"),
        tr("How do I request blood here?", "এখানে কীভাবে রক্তের অনুরোধ করব?"),
    )
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AssistantBadge(56)
        Spacer(Modifier.height(16.dp))
        Text(
            tr(
                "Hello! I'm the Blood Network assistant. Ask me about blood donation, eligibility, or finding a donor.",
                "হ্যালো! আমি ব্লাড নেটওয়ার্কের সহকারী। রক্তদান, যোগ্যতা বা রক্তদাতা খুঁজে পাওয়া নিয়ে জিজ্ঞাসা করতে পারেন।",
            ),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        suggestions.forEach { s ->
            SuggestionChip(
                onClick = { onSuggestion(s) },
                label = { Text(s) },
                modifier = Modifier.padding(vertical = 3.dp),
            )
        }
    }
}

@Composable
private fun AssistantBadge(size: Int) {
    Box(
        modifier = Modifier.size(size.dp).clip(CircleShape).background(BloodRed.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Filled.SmartToy, contentDescription = null, tint = BloodRed, modifier = Modifier.size((size * 0.5f).dp))
    }
}

@Composable
fun ChatBubble(
    msg: ChatUiMessage,
    modifier: Modifier = Modifier,
    errorText: String? = null,
    onRetry: () -> Unit = {},
    canRetry: Boolean = true,
) {
    val isUser = msg.role == "user"
    if (msg.isError) {
        ErrorBubble(errorText, onRetry, canRetry, modifier)
        return
    }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp,
            ),
            color = if (isUser) BloodRed else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.widthIn(max = 320.dp),
        ) {
            if (msg.loading) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    CircularProgressIndicator(color = BloodRed, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                    Text(
                        tr("Thinking…", "ভাবছি…"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                SelectionContainer {
                    Text(
                        text = msg.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorBubble(errorText: String?, onRetry: () -> Unit, canRetry: Boolean, modifier: Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                errorText ?: tr("Couldn't get a reply.", "উত্তর পাওয়া যায়নি।"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onRetry, enabled = canRetry) {
                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.size(4.dp))
                Text(tr("Retry", "আবার চেষ্টা"))
            }
        }
    }
}
