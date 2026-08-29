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
import com.bloodnetwork.bangladesh.ui.LocalVmFactory
import com.bloodnetwork.bangladesh.ui.components.LabeledTextField
import com.bloodnetwork.bangladesh.ui.components.PrimaryButton
import com.bloodnetwork.bangladesh.ui.components.RowChips
import com.bloodnetwork.bangladesh.ui.components.FormFieldSkeleton
import com.bloodnetwork.bangladesh.ui.theme.AvailableGreen
import com.bloodnetwork.bangladesh.ui.theme.BloodRed
import com.bloodnetwork.bangladesh.ui.viewmodel.EligibilityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EligibilityScreen(onNavigate: (String) -> Unit, onBack: () -> Unit) {
    val factory = LocalVmFactory.current!!
    val vm: EligibilityViewModel = viewModel(factory = factory)
    val state by vm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { vm.loadQuestions() }

    val allAnswered = state.questions.isNotEmpty() && state.questions.all { state.answers.containsKey(it.id) }
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
                title = { Text("Donation Eligibility") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                        "Answer these questions to check if you can donate blood.",
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
                    PrimaryButton(text = "Check Eligibility", onClick = { vm.checkEligibility() }, loading = state.isChecking, enabled = allAnswered && hasChanges)
                }
                state.result?.let { result ->
                    item {
                        ResultCard(result)
                    }
                    if (!hasChanges) {
                        item {
                            Text(
                                "Change any answer above to check again.",
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
                "This self-check is not a medical diagnosis and isn't approved by a medical authority. " +
                    "For an accurate assessment, please visit a hospital or consult a doctor.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun QuestionCard(q: EligibilityQuestionDto, current: String?, onAnswer: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(q.questionEn, style = MaterialTheme.typography.titleSmall)
            if (q.questionType == "yesno") {
                RowChips(
                    options = listOf("Yes", "No"),
                    selected = current ?: "",
                    labelOf = { it },
                    onSelect = onAnswer,
                )
            } else {
                LabeledTextField(
                    value = current ?: "",
                    onValueChange = onAnswer,
                    label = "Answer (${q.unit ?: "number"})",
                    keyboardType = KeyboardType.Number,
                )
            }
        }
    }
}

@Composable
fun ResultCard(result: com.bloodnetwork.bangladesh.data.model.EligibilityResultDto) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (result.isEligible) "You are eligible to donate" else "Not eligible right now",
                style = MaterialTheme.typography.titleMedium,
                color = if (result.isEligible) AvailableGreen else BloodRed,
            )
            Text("Score: ${result.score}", style = MaterialTheme.typography.bodyMedium)
            if (result.recommendationEn.isNotBlank()) {
                Text(result.recommendationEn, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
