package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.example.data.Order
import com.example.data.User

@Composable
fun DashboardScreen(
    orders: List<Order>,
    currentUser: User?,
    onSubmitWork: (Int, String) -> Unit,
    onApproveOrder: (Int, (Boolean) -> Unit) -> Unit,
    onCancelOrder: (Int, (Boolean) -> Unit) -> Unit
) {
    if (currentUser == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val isFreelancer = currentUser.role == "Freelancer"
    val filteredOrders = remember(orders, currentUser) {
        if (isFreelancer) {
            orders.filter { it.freelancerId == currentUser.id }
        } else {
            orders.filter { it.clientId == currentUser.id }
        }
    }

    var selectedOrderForDeliverable by remember { mutableStateOf<Order?>(null) }
    var deliverableNoteInput by remember { mutableStateOf("") }
    var actionToastMessage by remember { mutableStateOf<String?>(null) }

    // Aggregate statistics
    val totalVolumeAmount = remember(filteredOrders) {
        filteredOrders.sumOf { it.price }
    }
    val completedVolumeAmount = remember(filteredOrders) {
        filteredOrders.filter { it.status == "Completed" }.sumOf { it.price }
    }
    val escrowedVolumeAmount = remember(filteredOrders) {
        filteredOrders.filter { it.status == "Ongoing" || it.status == "Delivered" }.sumOf { it.price }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Income / Spent Tracker Dashboard Widgets
            Text(
                text = if (isFreelancer) "Tech Freelancer Terminal" else "Client Escrow Command Center",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isFreelancer) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                    else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f)
                ),
                border = BorderStroke(1.dp, if (isFreelancer) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isFreelancer) "TRACK EARNED INCOME" else "TRACK HIRED SPENDING",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isFreelancer) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                        )
                        Icon(
                            imageVector = if (isFreelancer) Icons.Default.TrendingUp else Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = if (isFreelancer) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "৳${completedVolumeAmount.toInt()}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Settled & Completed",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "৳${escrowedVolumeAmount.toInt()}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Locked in Escrow Safe",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Order Contract List (${filteredOrders.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredOrders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.WorkHistory,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No trackable orders found.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).testTag("orders_list"),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredOrders) { order ->
                        OrderItemCard(
                            order = order,
                            isFreelancer = isFreelancer,
                            onSubmitRequested = { selectedOrderForDeliverable = order },
                            onApproveRequested = {
                                onApproveOrder(order.id) { success ->
                                    if (success) {
                                        actionToastMessage = "Order approved! Escrow balance released successfully to freelancer."
                                    } else {
                                        actionToastMessage = "Failed to release escrow payment."
                                    }
                                }
                            },
                            onCancelRequested = {
                                onCancelOrder(order.id) { success ->
                                    if (success) {
                                        actionToastMessage = "Project contract cancelled, funds fully refunded to client."
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        // DELIVERABLE SUBMISSION POPUP FORM (Freelancer Only)
        if (selectedOrderForDeliverable != null) {
            AlertDialog(
                onDismissRequest = { selectedOrderForDeliverable = null },
                title = { Text("Submit Completed Work Deliverables") },
                text = {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Contract: ${selectedOrderForDeliverable!!.title}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(
                            text = "Describe your completed work, share codebase links, design mockups assets, or APK attachments reference so client can verify and trigger escrow payment release.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = deliverableNoteInput,
                            onValueChange = { deliverableNoteInput = it },
                            placeholder = { Text("Describe the work details standardly...") },
                            modifier = Modifier.fillMaxWidth().height(100.dp).testTag("submission_note_input"),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (deliverableNoteInput.isNotEmpty()) {
                                onSubmitWork(selectedOrderForDeliverable!!.id, deliverableNoteInput)
                                actionToastMessage = "Deliverable submitted standardly! Client notified."
                                selectedOrderForDeliverable = null
                                deliverableNoteInput = ""
                            }
                        },
                        enabled = deliverableNoteInput.isNotEmpty(),
                        modifier = Modifier.testTag("submit_deliverable_confirm")
                    ) {
                        Text("Handover Work")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedOrderForDeliverable = null }) {
                        Text("Discard")
                    }
                }
            )
        }

        // Action status Toast message
        AnimatedVisibility(
            visible = actionToastMessage != null,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 88.dp)
        ) {
            Snackbar(
                action = {
                    TextButton(onClick = { actionToastMessage = null }) {
                        Text("Dismiss")
                    }
                }
            ) {
                Text(actionToastMessage ?: "")
            }
        }
    }
}

@Composable
fun OrderItemCard(
    order: Order,
    isFreelancer: Boolean,
    onSubmitRequested: () -> Unit,
    onApproveRequested: () -> Unit,
    onCancelRequested: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("order_card_${order.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Contract type & badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.type,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Payment Escrow marker
                    Surface(
                        color = when(order.paymentStatus) {
                            "Released" -> MaterialTheme.colorScheme.primaryContainer
                            "Refunded" -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.tertiaryContainer
                        },
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = order.paymentStatus.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = when(order.paymentStatus) {
                                "Released" -> MaterialTheme.colorScheme.onPrimaryContainer
                                "Refunded" -> MaterialTheme.colorScheme.onErrorContainer
                                else -> MaterialTheme.colorScheme.onTertiaryContainer
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Status Marker
                    Surface(
                        color = when(order.status) {
                            "Completed" -> MaterialTheme.colorScheme.primaryContainer
                            "Ongoing" -> MaterialTheme.colorScheme.secondaryContainer
                            "Delivered" -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = order.status,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when(order.status) {
                                "Completed" -> MaterialTheme.colorScheme.onPrimaryContainer
                                "Ongoing" -> MaterialTheme.colorScheme.onSecondaryContainer
                                "Delivered" -> MaterialTheme.colorScheme.onTertiaryContainer
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Contract / Project Title
            Text(
                text = order.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isFreelancer) "Buyer: ${order.clientName}" else "Developer: ${order.freelancerName}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Price highlight box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Deposited Amount", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("৳${order.price.toInt()}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                }
            }

            // Submission content if delivered or completed
            if (order.status == "Delivered" || order.status == "Completed") {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Text("Submitted Deliverable Note:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = order.submissionNote, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Interactive Actions Section
            if (isFreelancer && order.status == "Ongoing") {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onSubmitRequested,
                    modifier = Modifier.fillMaxWidth().height(38.dp).testTag("open_submit_deliverable_${order.id}"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Upload Work Submission", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            if (!isFreelancer && (order.status == "Delivered" || order.status == "Ongoing")) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Cancel order
                    OutlinedButton(
                        onClick = onCancelRequested,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                        modifier = Modifier.weight(1f).height(38.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Cancel Contract", fontSize = 11.sp)
                    }

                    // Release payment
                    Button(
                        onClick = onApproveRequested,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1.2f).height(38.dp).testTag("release_escrow_${order.id}"),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Approve & Release Pay", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
