package com.bloodnetwork.bangladesh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bloodnetwork.bangladesh.data.model.AdminEligibilityQuestionDto
import com.bloodnetwork.bangladesh.data.model.SaveEligibilityQuestionRequest
import com.bloodnetwork.bangladesh.ui.LocalVmFactory
import com.bloodnetwork.bangladesh.ui.components.LabeledTextField
import com.bloodnetwork.bangladesh.ui.components.RowChips
import com.bloodnetwork.bangladesh.ui.components.SkeletonCard
import com.bloodnetwork.bangladesh.ui.i18n.tr
import com.bloodnetwork.bangladesh.ui.theme.BloodRed
import com.bloodnetwork.bangladesh.ui.viewmodel.AdminEligibilityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEligibilityQuestionsScreen(onBack: () -> Unit) {
    val factory = LocalVmFactory.current
    val vm: AdminEligibilityViewModel = viewModel(factory = factory)
    val state by vm.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var editingQuestion by remember { mutableStateOf<AdminEligibilityQuestionDto?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<AdminEligibilityQuestionDto?>(null) }

    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar(it); vm.clearMessages() }
    }
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { snackbarHostState.showSnackbar(it); vm.clearMessages() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tr("Eligibility Questions", "যোগ্যতা প্রশ্নাবলী")) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = tr("Back", "পেছনে"))
                    }
                },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Filled.Add, contentDescription = tr("Add question", "প্রশ্ন যোগ করুন"))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        if (state.isLoading && state.questions.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                repeat(4) { Card(modifier = Modifier.fillMaxWidth()) { SkeletonCard() } }
            }
        } else if (state.questions.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(tr("No questions yet", "এখনও কোনো প্রশ্ন নেই"), style = MaterialTheme.typography.titleMedium)
                Text(tr("Tap + to add the first eligibility question.", "প্রথম যোগ্যতা প্রশ্ন যোগ করতে + বাটনে ট্যাপ করুন।"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.questions, key = { it.id }) { q ->
                    QuestionAdminCard(
                        q = q,
                        modifier = Modifier.animateItem(),
                        onEdit = { editingQuestion = q },
                        onToggleActive = { vm.toggleActive(q.id, !q.isActive) },
                        onDelete = { deleteTarget = q },
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        EligibilityQuestionEditDialog(
            existing = null,
            isSaving = state.isSaving,
            onDismiss = { showCreateDialog = false },
            onSave = { request -> vm.create(request) { success -> if (success) showCreateDialog = false } },
        )
    }

    editingQuestion?.let { q ->
        EligibilityQuestionEditDialog(
            existing = q,
            isSaving = state.isSaving,
            onDismiss = { editingQuestion = null },
            onSave = { request -> vm.update(q.id, request) { success -> if (success) editingQuestion = null } },
        )
    }

    deleteTarget?.let { q ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text(tr("Delete this question?", "এই প্রশ্নটি মুছে ফেলবেন?")) },
            text = { Text("\"${q.questionEn}\" " + tr("will be permanently removed. This can't be undone.", "স্থায়ীভাবে মুছে ফেলা হবে। এই কাজটি আর ফেরানো যাবে না।")) },
            confirmButton = {
                TextButton(onClick = { vm.delete(q.id); deleteTarget = null }) {
                    Text(tr("Delete", "মুছুন"), color = BloodRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text(tr("Cancel", "বাতিল")) }
            },
        )
    }
}

@Composable
private fun QuestionAdminCard(
    q: AdminEligibilityQuestionDto,
    onEdit: () -> Unit,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "#${q.displayOrder}  ${q.questionEn}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                StatusPill(if (q.isActive) tr("Active", "সক্রিয়") else tr("Inactive", "নিষ্ক্রিয়"), q.isActive)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TagPill(if (q.questionType == "number") "${tr("Number", "সংখ্যা")}${q.unit?.let { " ($it)" } ?: ""}" else tr("Yes / No", "হ্যাঁ / না"))
                if (q.isCritical) TagPill(tr("Critical", "জরুরি"), BloodRed)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(if (q.isActive) tr("Deactivate", "নিষ্ক্রিয় করুন") else tr("Activate", "সক্রিয় করুন"), style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(end = 4.dp))
                Switch(checked = q.isActive, onCheckedChange = { onToggleActive() })
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = tr("Edit", "সম্পাদনা")) }
                IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = tr("Delete", "মুছুন"), tint = BloodRed) }
            }
        }
    }
}

@Composable
private fun StatusPill(text: String, active: Boolean) {
    val bg = if (active) Color(0xFF2E7D32).copy(alpha = 0.1f) else Color(0xFFC62828).copy(alpha = 0.1f)
    val fg = if (active) Color(0xFF2E7D32) else Color(0xFFC62828)
    Box(modifier = Modifier.clip(RoundedCornerShape(50)).background(bg).padding(horizontal = 8.dp, vertical = 4.dp)) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = fg, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun TagPill(text: String, color: Color = Color(0xFF616161)) {
    Box(modifier = Modifier.clip(RoundedCornerShape(50)).background(color.copy(alpha = 0.1f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun EligibilityQuestionEditDialog(
    existing: AdminEligibilityQuestionDto?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (SaveEligibilityQuestionRequest) -> Unit,
) {
    var questionEn by remember { mutableStateOf(existing?.questionEn ?: "") }
    var questionBn by remember { mutableStateOf(existing?.questionBn ?: "") }
    var questionBanglish by remember { mutableStateOf(existing?.questionBanglish ?: "") }
    var questionType by remember { mutableStateOf(existing?.questionType ?: "yesno") }
    var unit by remember { mutableStateOf(existing?.unit ?: "") }
    var minValue by remember { mutableStateOf(existing?.minValue?.toString() ?: "") }
    var maxValue by remember { mutableStateOf(existing?.maxValue?.toString() ?: "") }
    var passOnYes by remember { mutableStateOf(existing?.passOnYes ?: false) }
    var isCritical by remember { mutableStateOf(existing?.isCritical ?: false) }
    var displayOrder by remember { mutableStateOf((existing?.displayOrder ?: 0).toString()) }
    var passMessageEn by remember { mutableStateOf(existing?.passMessageEn ?: "") }
    var passMessageBn by remember { mutableStateOf(existing?.passMessageBn ?: "") }
    var failMessageEn by remember { mutableStateOf(existing?.failMessageEn ?: "") }
    var failMessageBn by remember { mutableStateOf(existing?.failMessageBn ?: "") }

    val canSave = questionEn.isNotBlank() && questionBn.isNotBlank() && questionBanglish.isNotBlank() &&
        passMessageEn.isNotBlank() && passMessageBn.isNotBlank() && failMessageEn.isNotBlank() && failMessageBn.isNotBlank()

    val isDirty = questionEn != (existing?.questionEn ?: "") ||
        questionBn != (existing?.questionBn ?: "") ||
        questionBanglish != (existing?.questionBanglish ?: "") ||
        questionType != (existing?.questionType ?: "yesno") ||
        unit != (existing?.unit ?: "") ||
        minValue != (existing?.minValue?.toString() ?: "") ||
        maxValue != (existing?.maxValue?.toString() ?: "") ||
        passOnYes != (existing?.passOnYes ?: false) ||
        isCritical != (existing?.isCritical ?: false) ||
        displayOrder != (existing?.displayOrder ?: 0).toString() ||
        passMessageEn != (existing?.passMessageEn ?: "") ||
        passMessageBn != (existing?.passMessageBn ?: "") ||
        failMessageEn != (existing?.failMessageEn ?: "") ||
        failMessageBn != (existing?.failMessageBn ?: "")

    var showDiscardConfirm by remember { mutableStateOf(false) }
    val requestClose = { if (isDirty) showDiscardConfirm = true else onDismiss() }

    if (showDiscardConfirm) {
        AlertDialog(
            onDismissRequest = { showDiscardConfirm = false },
            title = { Text(tr("Discard changes?", "পরিবর্তন বাতিল করবেন?")) },
            text = { Text(tr("Your edits to this question haven't been saved.", "এই প্রশ্নে করা পরিবর্তনগুলো সংরক্ষিত হয়নি।")) },
            confirmButton = {
                TextButton(onClick = { showDiscardConfirm = false; onDismiss() }) {
                    Text(tr("Discard", "বাতিল করুন"), color = BloodRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardConfirm = false }) { Text(tr("Keep Editing", "সম্পাদনা চালিয়ে যান")) }
            },
        )
    }

    Dialog(
        onDismissRequest = requestClose,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.92f).padding(16.dp).imePadding(),
            shape = RoundedCornerShape(20.dp),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    if (existing == null) tr("Add Question", "প্রশ্ন যোগ করুন") else tr("Edit Question", "প্রশ্ন সম্পাদনা করুন"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(20.dp),
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    LabeledTextField(questionEn, { questionEn = it }, tr("Question (English)", "প্রশ্ন (ইংরেজি)"), singleLine = false)
                    LabeledTextField(questionBn, { questionBn = it }, tr("Question (Bengali script)", "প্রশ্ন (বাংলা লিপি)"), singleLine = false)
                    LabeledTextField(questionBanglish, { questionBanglish = it }, tr("Question (Banglish)", "প্রশ্ন (বাংলিশ)"), singleLine = false)

                    Text(tr("Answer type", "উত্তরের ধরন"), style = MaterialTheme.typography.labelLarge)
                    val numberTypeLabel = tr("Number", "সংখ্যা")
                    val yesNoTypeLabel = tr("Yes / No", "হ্যাঁ / না")
                    RowChips(
                        options = listOf("number", "yesno"),
                        selected = questionType,
                        labelOf = { if (it == "number") numberTypeLabel else yesNoTypeLabel },
                        onSelect = { questionType = it },
                    )

                    if (questionType == "number") {
                        LabeledTextField(unit, { unit = it }, tr("Unit (optional, e.g. kg)", "একক (ঐচ্ছিক, যেমন কেজি)"))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            LabeledTextField(minValue, { minValue = it.filter { c -> c.isDigit() } }, tr("Min to pass (optional)", "পাসের সর্বনিম্ন মান (ঐচ্ছিক)"), keyboardType = KeyboardType.Number, modifier = Modifier.weight(1f))
                            LabeledTextField(maxValue, { maxValue = it.filter { c -> c.isDigit() } }, tr("Max to pass (optional)", "পাসের সর্বোচ্চ মান (ঐচ্ছিক)"), keyboardType = KeyboardType.Number, modifier = Modifier.weight(1f))
                        }
                    } else {
                        Text(tr("Which answer passes?", "কোন উত্তরে পাস হবে?"), style = MaterialTheme.typography.labelLarge)
                        val yesPassesLabel = tr("\"Yes\" passes", "\"হ্যাঁ\" দিলে পাস")
                        val noPassesLabel = tr("\"No\" passes", "\"না\" দিলে পাস")
                        RowChips(
                            options = listOf(false, true),
                            selected = passOnYes,
                            labelOf = { if (it) yesPassesLabel else noPassesLabel },
                            onSelect = { passOnYes = it },
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(tr("Critical", "জরুরি"), style = MaterialTheme.typography.titleSmall)
                            Text(tr("Failing this alone makes the donor ineligible", "শুধু এতে ব্যর্থ হলেও দাতা অযোগ্য বিবেচিত হবেন"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = isCritical, onCheckedChange = { isCritical = it })
                    }

                    LabeledTextField(displayOrder, { displayOrder = it.filter { c -> c.isDigit() } }, tr("Display order", "প্রদর্শনের ক্রম"), keyboardType = KeyboardType.Number)

                    Text(tr("Messages shown to the donor", "দাতাকে দেখানো বার্তা"), style = MaterialTheme.typography.labelLarge)
                    LabeledTextField(passMessageEn, { passMessageEn = it }, tr("Pass message (English)", "পাসের বার্তা (ইংরেজি)"), singleLine = false)
                    LabeledTextField(passMessageBn, { passMessageBn = it }, tr("Pass message (Bengali)", "পাসের বার্তা (বাংলা)"), singleLine = false)
                    LabeledTextField(failMessageEn, { failMessageEn = it }, tr("Fail message (English) — use {value} for the answer", "ব্যর্থতার বার্তা (ইংরেজি) — উত্তরের জন্য {value} ব্যবহার করুন"), singleLine = false)
                    LabeledTextField(failMessageBn, { failMessageBn = it }, tr("Fail message (Bengali) — use {value} for the answer", "ব্যর্থতার বার্তা (বাংলা) — উত্তরের জন্য {value} ব্যবহার করুন"), singleLine = false)
                    Spacer(Modifier.height(4.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    OutlinedButton(onClick = requestClose, enabled = !isSaving) { Text(tr("Cancel", "বাতিল")) }
                    Spacer(Modifier.width(12.dp))
                    Button(
                        enabled = canSave && !isSaving,
                        colors = ButtonDefaults.buttonColors(containerColor = BloodRed),
                        onClick = {
                            onSave(
                                SaveEligibilityQuestionRequest(
                                    questionEn = questionEn.trim(),
                                    questionBn = questionBn.trim(),
                                    questionBanglish = questionBanglish.trim(),
                                    questionType = questionType,
                                    unit = unit.trim().ifBlank { null },
                                    minValue = if (questionType == "number") minValue.toIntOrNull() else null,
                                    maxValue = if (questionType == "number") maxValue.toIntOrNull() else null,
                                    passOnYes = if (questionType == "yesno") passOnYes else null,
                                    isCritical = isCritical,
                                    displayOrder = displayOrder.toIntOrNull() ?: 0,
                                    passMessageEn = passMessageEn.trim(),
                                    passMessageBn = passMessageBn.trim(),
                                    failMessageEn = failMessageEn.trim(),
                                    failMessageBn = failMessageBn.trim(),
                                ),
                            )
                        },
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.padding(2.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text(tr("Save", "সংরক্ষণ করুন"))
                        }
                    }
                }
            }
        }
    }
}
