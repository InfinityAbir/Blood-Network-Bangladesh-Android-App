package com.bloodnetwork.bangladesh.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.bloodnetwork.bangladesh.data.network.toDisplayMessage
import com.bloodnetwork.bangladesh.ui.i18n.tr
import com.bloodnetwork.bangladesh.ui.theme.BloodRed
import kotlinx.coroutines.launch

private const val MAX_DETAILS = 500

/**
 * "Report this user" dialog (Play user-generated-content policy: users must be able to flag
 * abusive people/content in-app). [onSubmit] performs the network call; failures are shown
 * inline so the user can retry without losing what they typed.
 */
@Composable
fun ReportDialog(
    subjectName: String,
    onDismiss: () -> Unit,
    onSubmit: suspend (reason: String, details: String?) -> Unit,
) {
    // Stable English reason keys go to the moderation queue; labels are localized for the user.
    val reasons = listOf(
        "Fake or misleading request" to tr("Fake or misleading request", "ভুয়া বা বিভ্রান্তিকর অনুরোধ"),
        "Asking for money" to tr("Asking for money or payment", "টাকা বা পেমেন্ট চাইছে"),
        "Harassment or abuse" to tr("Harassment or abuse", "হয়রানি বা অসদাচরণ"),
        "Wrong contact or identity" to tr("Wrong contact info or fake identity", "ভুল যোগাযোগ বা ভুয়া পরিচয়"),
        "Other" to tr("Something else", "অন্য কিছু"),
    )
    var selected by remember { mutableStateOf<String?>(null) }
    var details by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var done by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = {
            Text(
                if (done) tr("Report sent", "রিপোর্ট পাঠানো হয়েছে")
                else tr("Report $subjectName", "$subjectName কে রিপোর্ট করুন"),
            )
        },
        text = {
            if (done) {
                Text(
                    tr(
                        "Thank you. Our team will review this report. You don't need to do anything else.",
                        "ধন্যবাদ। আমাদের টিম রিপোর্টটি পর্যালোচনা করবে। আপনাকে আর কিছু করতে হবে না।",
                    ),
                )
            } else {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        tr("What's the problem?", "সমস্যাটি কী?"),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    reasons.forEach { (key, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(selected = selected == key, role = Role.RadioButton, enabled = !isSubmitting) { selected = key; error = null },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = selected == key, onClick = null, enabled = !isSubmitting, modifier = Modifier.padding(end = 8.dp, top = 8.dp, bottom = 8.dp))
                            Text(label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    OutlinedTextField(
                        value = details,
                        onValueChange = { details = it.take(MAX_DETAILS) },
                        label = { Text(tr("Details (optional)", "বিস্তারিত (ঐচ্ছিক)")) },
                        minLines = 2,
                        maxLines = 4,
                        enabled = !isSubmitting,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    )
                    error?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        confirmButton = {
            if (done) {
                TextButton(onClick = onDismiss) { Text(tr("Close", "বন্ধ করুন")) }
            } else {
                val failedMsg = tr("Couldn't send the report. Please try again.", "রিপোর্ট পাঠানো যায়নি। আবার চেষ্টা করুন।")
                Button(
                    enabled = selected != null && !isSubmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = BloodRed),
                    onClick = {
                        val reason = selected ?: return@Button
                        isSubmitting = true
                        error = null
                        scope.launch {
                            runCatching { onSubmit(reason, details.trim().ifBlank { null }) }
                                .onSuccess { done = true }
                                .onFailure { error = it.toDisplayMessage(failedMsg) }
                            isSubmitting = false
                        }
                    },
                ) {
                    if (isSubmitting) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    else Text(tr("Send report", "রিপোর্ট পাঠান"))
                }
            }
        },
        dismissButton = {
            if (!done) TextButton(onClick = onDismiss, enabled = !isSubmitting) { Text(tr("Cancel", "বাতিল করুন")) }
        },
    )
}
