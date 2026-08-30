package com.bloodnetwork.bangladesh.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bloodnetwork.bangladesh.data.model.EligibilityQuestionDto
import com.bloodnetwork.bangladesh.data.prefs.AppLanguage
import com.bloodnetwork.bangladesh.ui.LocalVmFactory
import com.bloodnetwork.bangladesh.ui.components.LabeledTextField
import com.bloodnetwork.bangladesh.ui.components.PrimaryButton
import com.bloodnetwork.bangladesh.ui.components.RowChips
import com.bloodnetwork.bangladesh.ui.components.FormFieldSkeleton
import com.bloodnetwork.bangladesh.ui.i18n.LocalAppLanguage
import com.bloodnetwork.bangladesh.ui.i18n.tr
import com.bloodnetwork.bangladesh.ui.navigation.Routes
import com.bloodnetwork.bangladesh.ui.theme.AvailableGreen
import com.bloodnetwork.bangladesh.ui.theme.BloodRed
import com.bloodnetwork.bangladesh.ui.viewmodel.AuthViewModel
import com.bloodnetwork.bangladesh.ui.viewmodel.EligibilityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EligibilityScreen(onNavigate: (String) -> Unit, onBack: () -> Unit) {
    val factory = LocalVmFactory.current
    val vm: EligibilityViewModel = viewModel(factory = factory)
    val authVm: AuthViewModel = viewModel(factory = factory)
    val state by vm.uiState.collectAsStateWithLifecycle()
    val authState by authVm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { vm.loadQuestions() }

    val allAnswered = state.questions.isNotEmpty() && state.questions.all { state.answers.containsKey(it.id) }
    // hasChanges is kept only for a subtle stale-result hint; it no longer gates the Check button.
    // Button stays enabled whenever all questions are answered so user can re-check their score
    // and the persisted result remains visible across navigation / process death.
    val hasChanges = state.lastCheckedAnswers == null || state.answers != state.lastCheckedAnswers
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar(it) }
    }

    LaunchedEffect(state.result) {
        if (state.result != null) {
            // Result card lands right after the instruction line, disclaimer, questions, and button.
            listState.animateScrollToItem(state.questions.size + 3)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tr("Donation Eligibility", "রক্তদানের যোগ্যতা")) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = tr("Back", "পেছনে"))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        if (state.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                repeat(5) { FormFieldSkeleton() }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                item {
                    Text(
                        tr("Answer these questions to check if you can donate blood.", "আপনি রক্ত দিতে পারবেন কিনা তা যাচাই করতে এই প্রশ্নগুলোর উত্তর দিন।"),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                item { MedicalDisclaimerCard() }
                itemsIndexed(state.questions, key = { _, q -> q.id }) { _, q ->
                    QuestionCard(q, state.answers[q.id]) { answer ->
                        vm.setAnswer(q.id, answer)
                    }
                }
                item {
                    PrimaryButton(text = tr("Check Eligibility", "যোগ্যতা যাচাই করুন"), onClick = { vm.checkEligibility() }, loading = state.isChecking, enabled = allAnswered)
                }
                state.result?.let { result ->
                    item {
                        ResultCard(result, onBecomeDonor = {
                            val isLoggedIn = authState.isLoggedIn || authState.user != null
                            if (isLoggedIn) {
                                onNavigate(Routes.DONOR_PROFILE)
                            } else {
                                authVm.pendingRedirectRoute = Routes.DONOR_PROFILE
                                onNavigate(Routes.LOGIN)
                            }
                        })
                    }
                    // Keep result visible; allow re-check without requiring a change.
                    // Show a subtle hint only when answers have changed since last check.
                    if (hasChanges && state.lastCheckedAnswers != null) {
                        item {
                            Text(
                                tr(
                                    "Answers changed — tap Check Eligibility again to update your score.",
                                    "উত্তর পরিবর্তিত হয়েছে — স্কোর হালনাগাদ করতে আবার \"যোগ্যতা যাচাই করুন\"-এ চাপুন।",
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MedicalDisclaimerCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                Icons.Filled.LocalHospital,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
            Text(
                tr(
                    "This self-check is not a medical diagnosis and isn't approved by a medical authority. " +
                        "For an accurate assessment, please visit a hospital or consult a doctor.",
                    "এই স্ব-যাচাই কোনো চিকিৎসা নির্ণয় নয় এবং কোনো চিকিৎসা কর্তৃপক্ষ কর্তৃক অনুমোদিত নয়। " +
                        "সঠিক মূল্যায়নের জন্য, দয়া করে হাসপাতালে যান বা একজন ডাক্তারের সাথে পরামর্শ করুন।",
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun QuestionCard(q: EligibilityQuestionDto, current: String?, onAnswer: (String) -> Unit) {
    val language = LocalAppLanguage.current
    val questionText = if (language == AppLanguage.Bangla && q.questionBn.isNotBlank()) q.questionBn else q.questionEn
    val yesLabel = tr("Yes", "হ্যাঁ")
    val noLabel = tr("No", "না")
    val answerLabel = tr("Answer", "উত্তর")
    val numberLabel = tr("number", "সংখ্যা")
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(questionText, style = MaterialTheme.typography.titleSmall)
            if (q.questionType == "yesno") {
                RowChips(
                    options = listOf("Yes", "No"),
                    selected = current ?: "",
                    labelOf = { if (it == "Yes") yesLabel else noLabel },
                    onSelect = onAnswer,
                )
            } else {
                LabeledTextField(
                    value = current ?: "",
                    onValueChange = onAnswer,
                    label = "$answerLabel (${q.unit ?: numberLabel})",
                    keyboardType = KeyboardType.Number,
                )
            }
        }
    }
}

@Composable
fun ResultCard(result: com.bloodnetwork.bangladesh.data.model.EligibilityResultDto, onBecomeDonor: (() -> Unit)? = null) {
    val language = LocalAppLanguage.current
    val recommendation = if (language == AppLanguage.Bangla && result.recommendationBn.isNotBlank()) result.recommendationBn else result.recommendationEn
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = if (result.isEligible) tr("You are eligible to donate", "আপনি রক্ত দেওয়ার যোগ্য") else tr("Not eligible right now", "এই মুহূর্তে যোগ্য নন"),
                style = MaterialTheme.typography.titleMedium,
                color = if (result.isEligible) AvailableGreen else BloodRed,
            )
            Text("${tr("Score", "স্কোর")}: ${result.score}", style = MaterialTheme.typography.bodyMedium)
            if (recommendation.isNotBlank()) {
                Text(recommendation, style = MaterialTheme.typography.bodyMedium)
            }
            if (result.isEligible && onBecomeDonor != null) {
                PrimaryButton(
                    text = tr("Create My Donor Profile", "আমার ডোনার প্রোফাইল তৈরি করুন"),
                    onClick = onBecomeDonor,
                )
            }
        }
    }
}
