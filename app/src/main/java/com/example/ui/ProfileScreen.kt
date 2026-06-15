package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.example.data.User

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    user: User?,
    allUsers: List<User>,
    selectedUserId: String,
    onUserSwitched: (String) -> Unit,
    onVerifyNid: (String, String, (Boolean) -> Unit) -> Unit
) {
    if (user == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val scrollState = rememberScrollState()
    var showNidScanner by remember { mutableStateOf(false) }
    var nidNumberInput by remember { mutableStateOf("") }
    var mobileNumInput by remember { mutableStateOf("") }
    var alertSuccessMsg by remember { mutableStateOf<String?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Switch Demo Identity Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Demo Testing Identities",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allUsers.forEach { u ->
                        val isSelected = u.id == selectedUserId
                        Button(
                            onClick = { onUserSwitched(u.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("select_user_${u.id}"),
                            contentPadding = PaddingValues(horizontal = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant) else null
                        ) {
                            Text(text = u.name.substringBefore(" "), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Main User Card Info
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Profile Icon placeholder
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .background(
                                color = if (user.role == "Freelancer") MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (user.role == "Freelancer") Icons.Default.Work else Icons.Default.BusinessCenter,
                            contentDescription = "User Avatar",
                            modifier = Modifier.size(32.dp),
                            tint = if (user.role == "Freelancer") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = user.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (user.isVerified) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "NID Verified Account",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Text(
                            text = "${user.role}  •  ${user.profileTitle}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Rating row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Star",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = String.format("%.1f", user.rating),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            if (user.role == "Freelancer") {
                                Text(
                                    text = " (${user.completedProjectsCount} projects completed)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                // Bio
                Text(
                    text = "Contact Email",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = user.email,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Biography & Introduction",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = user.bio.ifEmpty { "No biography written yet." },
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Skills List Section (Freelancer Only)
        if (user.role == "Freelancer") {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Verified Professional Skills",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val skillsList = remember(user.skills) {
                        user.skills.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    }

                    if (skillsList.isEmpty()) {
                        Text(
                            text = "No skills specified yet. Add skills to bid on relevant developer projects.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            skillsList.forEach { skill ->
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(text = skill, fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // NID verification card
        if (!user.isVerified) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Unverified Alert",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Column {
                            Text(
                                text = "NID Verification Required",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "Submit your National ID card to verify your client or freelancer status.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (!showNidScanner) {
                        Button(
                            onClick = { showNidScanner = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("verify_account_button")
                        ) {
                            Text(text = "Start NID Smart Checking", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Divider(color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
                            
                            Text(
                                text = "Verify Identity Card (বাংলাদেশ জাতীয় পরিচয়পত্র)",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            OutlinedTextField(
                                value = nidNumberInput,
                                onValueChange = { nidNumberInput = it },
                                label = { Text("10 or 17 digit NID / Smart Card Number") },
                                leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("nid_number_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )

                            OutlinedTextField(
                                value = mobileNumInput,
                                onValueChange = { mobileNumInput = it },
                                label = { Text("Bangladeshi Mobile Number (Nagad/bKash linked)") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("verify_mobile_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )

                            // Document attachment simulator visual
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                    Icon(
                                        imageVector = Icons.Default.DocumentScanner,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(44.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "ID Verification Scanning",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "Automatic NID card scanner active\nFront & back face analysis initialized",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showNidScanner = false },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Discard")
                                }

                                Button(
                                    onClick = {
                                        if (nidNumberInput.length >= 10 && mobileNumInput.length >= 11) {
                                            errorMsg = null
                                            onVerifyNid(nidNumberInput, mobileNumInput) { ok ->
                                                if (ok) {
                                                    alertSuccessMsg = "NID Verification approved standardly! Auto-notified."
                                                    nidNumberInput = ""
                                                    mobileNumInput = ""
                                                    showNidScanner = false
                                                } else {
                                                    errorMsg = "Verification transaction failed."
                                                }
                                            }
                                        } else {
                                            errorMsg = "Please enter standard NID card (10+ digits) and standard DB linked phone number."
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("submit_nid_verification"),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Submit Verified")
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Already verified checklist screen
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Verified Checked logo",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = "NID Profile Verified Status",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Congratulations! Verified with NID standard number: ${user.nidNumber}. Secure withdrawals enabled standardly.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Action Alerts messages
        AnimatedVisibility(visible = alertSuccessMsg != null) {
            Snackbar(
                action = {
                    TextButton(onClick = { alertSuccessMsg = null }) {
                        Text("OK", color = MaterialTheme.colorScheme.inversePrimary)
                    }
                },
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(alertSuccessMsg ?: "")
            }
        }

        AnimatedVisibility(visible = errorMsg != null) {
            Snackbar(
                containerColor = MaterialTheme.colorScheme.error,
                action = {
                    TextButton(onClick = { errorMsg = null }) {
                        Text("Dismiss", color = Color.White)
                    }
                },
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(errorMsg ?: "")
            }
        }
    }
}
