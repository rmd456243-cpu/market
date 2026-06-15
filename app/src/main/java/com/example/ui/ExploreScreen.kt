package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Gig
import com.example.data.User

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExploreScreen(
    gigs: List<Gig>,
    currentUser: User?,
    onBuyGig: (Gig, (Boolean) -> Unit) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedGigForPurchase by remember { mutableStateOf<Gig?>(null) }
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    val filteredGigs = remember(gigs, searchQuery) {
        if (searchQuery.isBlank()) gigs
        else gigs.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.category.contains(searchQuery, ignoreCase = true) ||
            it.description.contains(searchQuery, ignoreCase = true)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Search Header Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search developer or design gigs...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("gig_search_bar"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Top Header Label
            Text(
                text = "Secure On-Demand Freelance Services",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredGigs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = "No results icon",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No gigs matched your criteria.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("gigs_list"),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredGigs) { gig ->
                        GigItemCard(
                            gig = gig,
                            currentUser = currentUser,
                            onPurchaseRequested = {
                                selectedGigForPurchase = gig
                                showCheckoutDialog = true
                            }
                        )
                    }
                }
            }
        }

        // Checkout secure dialog
        if (showCheckoutDialog && selectedGigForPurchase != null) {
            CheckoutSecuredDialog(
                gig = selectedGigForPurchase!!,
                currentUser = currentUser,
                onConfirmPayment = {
                    onBuyGig(selectedGigForPurchase!!) { success ->
                        showCheckoutDialog = false
                        if (success) {
                            toastMessage = "Gig purchased successfully! Funds locked in TrustEscrow."
                        } else {
                            toastMessage = "Purchase failed! Please check your wallet balance."
                        }
                    }
                },
                onDismiss = {
                    showCheckoutDialog = false
                    selectedGigForPurchase = null
                }
            )
        }

        // Notification Toast
        AnimatedVisibility(
            visible = toastMessage != null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 88.dp)
        ) {
            Snackbar(
                action = {
                    TextButton(onClick = { toastMessage = null }) {
                        Text("Dismiss", color = Color.White)
                    }
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Text(toastMessage ?: "")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GigItemCard(
    gig: Gig,
    currentUser: User?,
    onPurchaseRequested: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("gig_card_${gig.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Category & rating row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = gig.category.uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Star rating",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = String.format(" %.1f", gig.sellerRating),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = gig.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Description
            Text(
                text = gig.description,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Tags flow
            if (gig.tags.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    gig.tags.split(",").forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = tag.trim(), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

            Spacer(modifier = Modifier.height(12.dp))

            // Seller, Cost and Order Button Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Seller profile summary mini
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    Text(
                        text = gig.sellerName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Pricing summary and purchase activation
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "STARTING AT", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                        Text(text = "৳${gig.price.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    }

                    if (currentUser?.role == "Client") {
                        Button(
                            onClick = onPurchaseRequested,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(36.dp).testTag("purchase_gig_button_${gig.id}")
                        ) {
                            Text("Hire", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CheckoutSecuredDialog(
    gig: Gig?,
    currentUser: User?,
    onConfirmPayment: () -> Unit,
    onDismiss: () -> Unit
) {
    if (gig == null || currentUser == null) return

    var selectedPaymentGateGroup by remember { mutableStateOf("bKash") }
    var checkoutNumberInput by remember { mutableStateOf("") }
    var pinAddressInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Security, contentDescription = "Secure lock", tint = MaterialTheme.colorScheme.primary)
                Text("TrustGig Secure Escrow Checkout", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Contract: ${gig.title}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Price detail panel
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Service contract amount", fontSize = 12.sp)
                            Text("Platform security fee (0%)", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("৳${gig.price}", fontWeight = FontWeight.Bold)
                            Text("৳0.0", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                // Balance review
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Your Account Balance:", fontSize = 13.sp)
                    Text(
                        "৳${currentUser.walletBalance}",
                        fontWeight = FontWeight.Bold,
                        color = if (currentUser.walletBalance >= gig.price) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error,
                        fontSize = 13.sp
                    )
                }

                Divider()

                // Payments selective gateways
                Text("Choose Gateway to Escrow Funds:", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("bKash", "Nagad", "Rocket", "Upay", "TealCrypto").forEach { m ->
                        val isSelected = selectedPaymentGateGroup == m
                        Button(
                            onClick = { selectedPaymentGateGroup = m },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(text = m, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (selectedPaymentGateGroup == "TealCrypto") {
                    Text("Digital USDT & Binance secure deposit auto-settlement. 1 USDT = ৳115", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text("Secured direct transaction of local Bangladeshi Taka.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Details Input fields
                OutlinedTextField(
                    value = checkoutNumberInput,
                    onValueChange = { checkoutNumberInput = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text(if (selectedPaymentGateGroup == "TealCrypto") "Binance Pay ID / USDT Wallet TRC20" else "$selectedPaymentGateGroup Wallet Number") },
                    modifier = Modifier.fillMaxWidth().testTag("checkout_account_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = pinAddressInput,
                    onValueChange = { pinAddressInput = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    label = { Text(if (selectedPaymentGateGroup == "TealCrypto") "USDT Transaction Hash / Note" else "$selectedPaymentGateGroup Transfer PIN Reference") },
                    modifier = Modifier.fillMaxWidth().testTag("checkout_pin_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmPayment()
                },
                enabled = currentUser.walletBalance >= gig.price && checkoutNumberInput.isNotEmpty() && pinAddressInput.isNotEmpty(),
                modifier = Modifier.testTag("confirm_checkout_button"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Authorize Escrow Contract")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Cancel")
            }
        }
    )
}
