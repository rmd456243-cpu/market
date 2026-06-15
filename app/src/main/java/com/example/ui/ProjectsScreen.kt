package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Bid
import com.example.data.Project
import com.example.data.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull

@Composable
fun ProjectsScreen(
    projects: List<Project>,
    currentUser: User?,
    onPostProject: (String, String, Double, String, (Boolean) -> Unit) -> Unit,
    onSubmitBid: (Int, String, Double, String, Int, (Boolean) -> Unit) -> Unit,
    getProjectBids: (Int) -> Flow<List<Bid>>,
    onAcceptBid: (Int, Int, (Boolean) -> Unit) -> Unit
) {
    var showPostDialog by remember { mutableStateOf(false) }
    var selectedProjectForDetails by remember { mutableStateOf<Project?>(null) }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Projects Bidding Board",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Post contracts, place live bids, start safe escrows",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (currentUser?.role == "Client") {
                    Button(
                        onClick = { showPostDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("post_project_fab")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add project")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Post", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (projects.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AssignmentLate,
                            contentDescription = "Empty projects",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No active projects listed currently.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).testTag("projects_list"),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(projects) { project ->
                        var bidCount by remember { mutableIntStateOf(0) }
                        
                        // Reactively count bids
                        LaunchedEffect(project.id) {
                            getProjectBids(project.id).collect {
                                bidCount = it.size
                            }
                        }

                        ProjectItemCard(
                            project = project,
                            bidCount = bidCount,
                            onClick = { selectedProjectForDetails = project }
                        )
                    }
                }
            }
        }

        // Project details overlay modal
        if (selectedProjectForDetails != null) {
            ProjectDetailsModal(
                project = selectedProjectForDetails!!,
                currentUser = currentUser,
                getProjectBids = { getProjectBids(selectedProjectForDetails!!.id) },
                onSubmitBid = { amount, proposal, days, cb ->
                    onSubmitBid(selectedProjectForDetails!!.id, selectedProjectForDetails!!.title, amount, proposal, days) { ok ->
                        if (ok) {
                            toastMessage = "Your bid proposal has been submitted successfully!"
                        } else {
                            toastMessage = "Error: Bidding unsuccessful."
                        }
                        cb(ok)
                    }
                },
                onAcceptBid = { bidId ->
                    onAcceptBid(bidId, selectedProjectForDetails!!.id) { ok ->
                        if (ok) {
                            toastMessage = "Bid accepted standardly! Secure cargo escrow locked."
                            selectedProjectForDetails = null
                        } else {
                            toastMessage = "Insufficient cash in wallet to fund this contract!"
                        }
                    }
                },
                onDismiss = { selectedProjectForDetails = null }
            )
        }

        // Post Project Dialog form
        if (showPostDialog) {
            PostProjectDialog(
                onConfirmPost = { title, desc, budget, category ->
                    onPostProject(title, desc, budget, category) { ok ->
                        showPostDialog = false
                        if (ok) {
                            toastMessage = "Your requirement posted standardly! Developers auto-notified."
                        } else {
                            toastMessage = "An error occurred posting requirement."
                        }
                    }
                },
                onDismiss = { showPostDialog = false }
            )
        }

        // Toast Snackbar
        AnimatedVisibility(
            visible = toastMessage != null,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 88.dp)
        ) {
            Snackbar(
                action = {
                    TextButton(onClick = { toastMessage = null }) {
                        Text("Dismiss")
                    }
                }
            ) {
                Text(toastMessage ?: "")
            }
        }
    }
}

@Composable
fun ProjectItemCard(
    project: Project,
    bidCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("project_card_${project.id}")
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Category & Badge status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = project.category.uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                
                Surface(
                    color = when(project.status) {
                        "Open" -> MaterialTheme.colorScheme.primaryContainer
                        "Ongoing" -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = project.status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when(project.status) {
                            "Open" -> MaterialTheme.colorScheme.onPrimaryContainer
                            "Ongoing" -> MaterialTheme.colorScheme.onSecondaryContainer
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = project.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Description summary
            Text(
                text = project.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

            Spacer(modifier = Modifier.height(10.dp))

            // Budget, Client and Bid counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "CLIENT BUDGET", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    Text(text = "৳${project.budget.toInt()}", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.secondary)
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "BIDS RECEIVED", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                        Text(text = "$bidCount bidding proposals", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Details",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
fun ProjectDetailsModal(
    project: Project,
    currentUser: User?,
    getProjectBids: () -> Flow<List<Bid>>,
    onSubmitBid: (Double, String, Int, (Boolean) -> Unit) -> Unit,
    onAcceptBid: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val bidsList by getProjectBids().collectAsState(initial = emptyList())
    var proposalText by remember { mutableStateOf("") }
    var bidAmount by remember { mutableStateOf("") }
    var deliveryDays by remember { mutableStateOf("") }
    var biddingActiveForm by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Assignment, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                Text(text = project.title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Info block
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Budget Required", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("৳${project.budget.toInt()}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                    Card(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Platform Protection", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Trust Escrow", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Text("Requirement Description:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                Text(project.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Divider()

                // Client Identity Info
                Text("Posted by Client:", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(24.dp))
                    Text(project.clientName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Divider()

                // BIDS LIST / STATUS block
                Text("Submitted Bid Proposals (${bidsList.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                if (bidsList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                        Text("No bids placed on this project yet.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    bidsList.forEach { bid ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(bid.freelancerName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(12.dp))
                                            Text(" ${bid.freelancerRating}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("৳${bid.bidAmount.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                                        Text("${bid.deliveryDays} days delivery", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(bid.proposalText, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                // Accept bid for client
                                if (currentUser?.role == "Client" && project.clientId == currentUser.id && project.status == "Open") {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { onAcceptBid(bid.id) },
                                        modifier = Modifier.fillMaxWidth().height(32.dp).testTag("accept_bid_${bid.id}"),
                                        contentPadding = PaddingValues(0.dp),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text("Accept Bid & Secure BDT ${bid.bidAmount.toInt()} Escrow", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Place BID mechanism for Freelancers
                if (currentUser?.role == "Freelancer" && project.status == "Open") {
                    val alreadyBid = bidsList.any { it.freelancerId == currentUser.id }
                    if (alreadyBid) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("You have already bid on this project standardly.", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else if (!biddingActiveForm) {
                        Button(
                            onClick = { biddingActiveForm = true },
                            modifier = Modifier.fillMaxWidth().height(44.dp).testTag("open_bid_form_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Place Bid on Project", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(top = 10.dp)
                        ) {
                            Divider()
                            Text("Bid Submission Proposal Terminal", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)

                            OutlinedTextField(
                                value = bidAmount,
                                onValueChange = { bidAmount = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                label = { Text("Your Bid Price (৳ BDT)") },
                                modifier = Modifier.fillMaxWidth().testTag("bid_price_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )

                            OutlinedTextField(
                                value = deliveryDays,
                                onValueChange = { deliveryDays = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                label = { Text("Delivery Estimate (Days)") },
                                modifier = Modifier.fillMaxWidth().testTag("bid_days_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )

                            OutlinedTextField(
                                value = proposalText,
                                onValueChange = { proposalText = it },
                                label = { Text("Briefly introduce custom solution proposal") },
                                modifier = Modifier.fillMaxWidth().height(90.dp).testTag("proposal_speech_input"),
                                shape = RoundedCornerShape(8.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { biddingActiveForm = false },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Discard")
                                }

                                Button(
                                    onClick = {
                                        val amt = bidAmount.toDoubleOrNull() ?: 0.0
                                        val days = deliveryDays.toIntOrNull() ?: 0
                                        if (amt > 0 && days > 0 && proposalText.isNotEmpty()) {
                                            onSubmitBid(amt, proposalText, days) { ok ->
                                                if (ok) {
                                                    biddingActiveForm = false
                                                    proposalText = ""
                                                    bidAmount = ""
                                                    deliveryDays = ""
                                                }
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).testTag("submit_bid_button")
                                ) {
                                    Text("Confirm Bid")
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun PostProjectDialog(
    onConfirmPost: (String, String, Double, String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("App Development") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Post Project Requirements", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Project Title") },
                    modifier = Modifier.fillMaxWidth().testTag("post_project_title"),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("What needs to be done? List requirements") },
                    modifier = Modifier.fillMaxWidth().height(100.dp).testTag("post_project_desc"),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = budget,
                    onValueChange = { budget = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("Maximum Budget (৳ BDT)") },
                    modifier = Modifier.fillMaxWidth().testTag("post_project_budget"),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                // Simple Category Choice
                Text("Select Technical Domain Category:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("App Development", "Design", "Web Dev").forEach { c ->
                        val isSelected = category == c
                        Button(
                            onClick = { category = c },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f).height(32.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(text = c, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val bgt = budget.toDoubleOrNull() ?: 0.0
                    if (title.isNotEmpty() && description.isNotEmpty() && bgt > 0) {
                        onConfirmPost(title, description, bgt, category)
                    }
                },
                enabled = title.isNotEmpty() && description.isNotEmpty() && (budget.toDoubleOrNull() ?: 0.0) > 0,
                modifier = Modifier.testTag("submit_project_button")
            ) {
                Text("Publish Requirements")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Discard")
            }
        }
    )
}
