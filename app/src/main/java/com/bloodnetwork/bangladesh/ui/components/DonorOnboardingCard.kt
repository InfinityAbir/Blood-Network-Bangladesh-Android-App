package com.bloodnetwork.bangladesh.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bloodnetwork.bangladesh.ui.i18n.tr
import com.bloodnetwork.bangladesh.ui.navigation.Routes
import com.bloodnetwork.bangladesh.ui.viewmodel.DonorOnboardingState
import com.bloodnetwork.bangladesh.ui.viewmodel.OnboardingStep

/**
 * Dashboard card that walks a new donor through eligibility check -> donor profile ->
 * availability, so it's obvious what to do after registering. Hidden once everything is done.
 */
@Composable
fun DonorOnboardingCard(
    state: DonorOnboardingState,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)),
    ) {
        if (state.loading && !state.known) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Text(
                    tr("Checking your donor setup…", "আপনার দাতা সেটআপ দেখা হচ্ছে…"),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            return@Card
        }

        val step = state.step
        val stepNumber = when (step) {
            OnboardingStep.Eligibility -> 1
            OnboardingStep.Profile -> 2
            else -> 3
        }

        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    tr("Step $stepNumber of 3", "ধাপ $stepNumber / ৩"),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    tr("Get started as a donor", "দাতা হিসেবে শুরু করুন"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            StepRow(1, tr("Check your eligibility", "আপনার যোগ্যতা যাচাই করুন"), done = step != OnboardingStep.Eligibility, current = step == OnboardingStep.Eligibility)
            StepRow(2, tr("Create your donor profile", "দাতা প্রোফাইল তৈরি করুন"), done = step == OnboardingStep.Availability || step == OnboardingStep.Done, current = step == OnboardingStep.Profile)
            StepRow(3, tr("Turn on availability", "প্রাপ্যতা চালু করুন"), done = step == OnboardingStep.Done, current = step == OnboardingStep.Availability)

            val (message, button, route) = when {
                state.notEligibleYet -> Triple(
                    tr(
                        "Based on your answers you can't donate right now. Your situation may change — you can check again at any time.",
                        "আপনার উত্তর অনুযায়ী এখন রক্ত দেওয়া সম্ভব নয়। পরিস্থিতি বদলাতে পারে — যেকোনো সময় আবার যাচাই করতে পারেন।",
                    ),
                    tr("Check again", "আবার যাচাই করুন"),
                    Routes.ELIGIBILITY,
                )
                step == OnboardingStep.Eligibility -> Triple(
                    tr(
                        "Start here: answer a few health questions. It takes about a minute and tells you whether you can donate right now.",
                        "এখান থেকে শুরু করুন: কয়েকটি স্বাস্থ্য-প্রশ্নের উত্তর দিন। প্রায় এক মিনিট লাগে, আর জানা যায় এখন রক্ত দিতে পারবেন কিনা।",
                    ),
                    tr("Start eligibility check", "যোগ্যতা যাচাই শুরু করুন"),
                    Routes.ELIGIBILITY,
                )
                step == OnboardingStep.Profile -> Triple(
                    tr(
                        "You're eligible! Add your blood group and area so people who need blood can find you.",
                        "আপনি যোগ্য! রক্তের গ্রুপ ও এলাকা যোগ করুন, যাতে রক্তপ্রার্থীরা আপনাকে খুঁজে পান।",
                    ),
                    tr("Create donor profile", "দাতা প্রোফাইল তৈরি করুন"),
                    Routes.DONOR_PROFILE,
                )
                else -> Triple(
                    tr(
                        "Your profile is ready. Set yourself as available to get match alerts.",
                        "আপনার প্রোফাইল তৈরি। ম্যাচ অ্যালার্ট পেতে নিজেকে \"প্রাপ্য\" হিসেবে সেট করুন।",
                    ),
                    tr("Set availability", "প্রাপ্যতা সেট করুন"),
                    Routes.DONOR_PROFILE,
                )
            }

            Text(message, style = MaterialTheme.typography.bodyMedium)
            PrimaryButton(text = button, onClick = { onNavigate(route) })
        }
    }
}

@Composable
private fun StepRow(number: Int, label: String, done: Boolean, current: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        val primary = MaterialTheme.colorScheme.primary
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(
                    color = when {
                        done -> primary
                        current -> primary.copy(alpha = 0.18f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (done) {
                Icon(Icons.Filled.Check, contentDescription = tr("Done", "সম্পন্ন"), tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
            } else {
                Text(
                    "$number",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (current) primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (current) FontWeight.Bold else FontWeight.Normal,
            color = if (done || current) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
