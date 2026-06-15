package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.User
import com.example.data.WalletTransaction
import com.example.ui.theme.BkashCrimson
import com.example.ui.theme.CobaltSecondary
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.OrangeNagad
import com.example.ui.theme.RocketViolet
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WalletScreen(
    user: User?,
    transactions: List<WalletTransaction>,
    onDeposit: (Double, String, String) -> Unit,
    onWithdraw: (Double, String, String, (Boolean) -> Unit) -> Unit
) {
    if (user == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    var selectedActionTab by remember { mutableStateOf("Logs") } // "Logs", "Deposit", "Withdraw"
    var selectedGateway by remember { mutableStateOf("bKash") }
    var actionAmount by remember { mutableStateOf("") }
    var walletOrTxIdInput by remember { mutableStateOf("") }
    var walletMessageAlert by remember { mutableStateOf<String?>(null) }
    var errorAlertMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Balance premium card with brush gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary,
                            Color(0xFF0F2027)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TRUSTGIG DIGITAL ESCROW BALANCES",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = "Safe escrow badges",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = "৳${user.walletBalance.toInt()}",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Available to Spend or Withdraw",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation Tabs for Wallet Controls
        ScrollableTabRow(
            selectedTabIndex = when(selectedActionTab) {
                "Logs" -> 0
                "Deposit" -> 1
                else -> 2
            },
            edgePadding = 0.dp,
            divider = {},
            modifier = Modifier.fillMaxWidth().testTag("wallet_tabs")
        ) {
            Tab(
                selected = selectedActionTab == "Logs",
                onClick = { selectedActionTab = "Logs" },
                text = { Text("Crypt Ledger logs") }
            )
            Tab(
                selected = selectedActionTab == "Deposit",
                onClick = { selectedActionTab = "Deposit" },
                text = { Text("Add Funds (বিকাশ/Nagad/USDT)") },
                modifier = Modifier.testTag("deposit_tab")
            )
            Tab(
                selected = selectedActionTab == "Withdraw",
                onClick = { selectedActionTab = "Withdraw" },
                text = { Text("Withdraw") },
                modifier = Modifier.testTag("withdraw_tab")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dynamic views based on active action tab
        when(selectedActionTab) {
            "Logs" -> {
                Text(
                    text = "Account Statements Ledger",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (transactions.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text("No transaction histories logs recorded.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).testTag("transactions_list"),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(transactions) { tx ->
                            TransactionItemRow(tx = tx)
                        }
                    }
                }
            }

            "Deposit" -> {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Fund checkout payment gateway simulation (Sandbox)",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Gateways choices grid
                    Text("Select Payment Processor:", fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("bKash", "Nagad", "Rocket", "Upay", "Binance", "USDT").forEach { gateway ->
                            val isSelected = selectedGateway == gateway
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(55.dp)
                                    .clickable { selectedGateway = gateway },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) {
                                        when(gateway) {
                                            "bKash" -> BkashCrimson.copy(alpha = 0.15f)
                                            "Nagad" -> OrangeNagad.copy(alpha = 0.15f)
                                            "Rocket" -> RocketViolet.copy(alpha = 0.15f)
                                            "USDT", "Binance" -> EmeraldPrimary.copy(alpha = 0.15f)
                                            else -> CobaltSecondary.copy(alpha = 0.15f)
                                        }
                                    } else MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) {
                                        when(gateway) {
                                            "bKash" -> BkashCrimson
                                            "Nagad" -> OrangeNagad
                                            "Rocket" -> RocketViolet
                                            "USDT", "Binance" -> EmeraldPrimary
                                            else -> CobaltSecondary
                                        }
                                    } else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = gateway,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 10.sp,
                                        color = when(gateway) {
                                            "bKash" -> if (isSelected) BkashCrimson else MaterialTheme.colorScheme.onSurface
                                            "Nagad" -> if (isSelected) OrangeNagad else MaterialTheme.colorScheme.onSurface
                                            "Rocket" -> if (isSelected) RocketViolet else MaterialTheme.colorScheme.onSurface
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = actionAmount,
                        onValueChange = { actionAmount = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text("Amount standardly (৳ BDT equivalent)") },
                        modifier = Modifier.fillMaxWidth().testTag("wallet_deposit_amount"),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = walletOrTxIdInput,
                        onValueChange = { walletOrTxIdInput = it },
                        label = {
                            Text(
                                if(selectedGateway == "USDT" || selectedGateway == "Binance") "Binance Pay ID / Crypto Tx Hash"
                                else "Mobile Operator Number ( bKash/Nagad account )"
                            )
                        },
                        modifier = Modifier.fillMaxWidth().testTag("wallet_deposit_ref"),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = {
                            val amt = actionAmount.toDoubleOrNull() ?: 0.0
                            if (amt > 0 && walletOrTxIdInput.isNotEmpty()) {
                                onDeposit(amt, selectedGateway, walletOrTxIdInput)
                                walletMessageAlert = "৳$amt successfully credited standardly into TrustWallet."
                                actionAmount = ""
                                walletOrTxIdInput = ""
                                selectedActionTab = "Logs"
                            } else {
                                errorAlertMessage = "Format error. Please submit complete fields."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("confirm_deposit_button")
                    ) {
                        Text("Simulate Secure Deposit Checkout", fontWeight = FontWeight.Bold)
                    }
                }
            }

            "Withdraw" -> {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Withdraw your freelancer earnings securely. Payments are transferred standardly to local wallets in Bangladesh.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Gateway Choice UI
                    Text("Select Cashout Route:", fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("bKash", "Nagad", "Rocket", "Upay", "Binance").forEach { gateway ->
                            val isSelected = selectedGateway == gateway
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(55.dp)
                                    .clickable { selectedGateway = gateway },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) {
                                        when(gateway) {
                                            "bKash" -> BkashCrimson.copy(alpha = 0.15f)
                                            "Nagad" -> OrangeNagad.copy(alpha = 0.15f)
                                            else -> CobaltSecondary.copy(alpha = 0.15f)
                                        }
                                    } else MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) {
                                        when(gateway) {
                                            "bKash" -> BkashCrimson
                                            "Nagad" -> OrangeNagad
                                            else -> CobaltSecondary
                                        }
                                    } else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(text = gateway, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp)
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = actionAmount,
                        onValueChange = { actionAmount = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text("Cashout Amount (৳ BDT)") },
                        modifier = Modifier.fillMaxWidth().testTag("wallet_withdrawal_amount"),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = walletOrTxIdInput,
                        onValueChange = { walletOrTxIdInput = it },
                        label = { Text("Receiver Wallet Address or phone number") },
                        modifier = Modifier.fillMaxWidth().testTag("wallet_withdrawal_ref"),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = {
                            val amt = actionAmount.toDoubleOrNull() ?: 0.0
                            if (amt > 0 && walletOrTxIdInput.isNotEmpty()) {
                                onWithdraw(amt, selectedGateway, walletOrTxIdInput) { ok ->
                                    if (ok) {
                                        walletMessageAlert = "Cashout of ৳$amt started successfully. Under review."
                                        actionAmount = ""
                                        walletOrTxIdInput = ""
                                        selectedActionTab = "Logs"
                                    } else {
                                        errorAlertMessage = "Insufficient account balance to cashier BDT $amt!"
                                    }
                                }
                            } else {
                                errorAlertMessage = "Please complete withdrawal fields properly."
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("confirm_withdraw_button")
                    ) {
                        Text("Process Secure Cashout", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Action Alerts messages
        AnimatedVisibility(visible = walletMessageAlert != null) {
            Snackbar(
                action = {
                    TextButton(onClick = { walletMessageAlert = null }) {
                        Text("COOL")
                    }
                },
                modifier = Modifier.padding(bottom = 80.dp)
            ) {
                Text(walletMessageAlert ?: "")
            }
        }

        AnimatedVisibility(visible = errorAlertMessage != null) {
            Snackbar(
                containerColor = MaterialTheme.colorScheme.error,
                action = {
                    TextButton(onClick = { errorAlertMessage = null }) {
                        Text("Dismiss", color = Color.White)
                    }
                },
                modifier = Modifier.padding(bottom = 80.dp)
            ) {
                Text(errorAlertMessage ?: "")
            }
        }
    }
}

@Composable
fun TransactionItemRow(tx: WalletTransaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circle visual indicator
                val isAddition = tx.type == "Deposit" || tx.type == "Earnings" || tx.type == "Refund"
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = when (tx.type) {
                                "Deposit", "Earnings", "Refund" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                "EscrowHold" -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                                else -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                            },
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when(tx.type) {
                            "Deposit" -> Icons.Default.Add
                            "Earnings" -> Icons.Default.MonetizationOn
                            "Refund" -> Icons.Default.Undo
                            "EscrowHold" -> Icons.Default.Lock
                            else -> Icons.Default.ArrowOutward
                        },
                        contentDescription = null,
                        tint = when (tx.type) {
                            "Deposit", "Earnings", "Refund" -> MaterialTheme.colorScheme.primary
                            "EscrowHold" -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.error
                        },
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Text(
                        text = if(tx.type == "EscrowHold") "Escrow Cargo Locked" else tx.type,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Ref: ${tx.accountNumberOrTxId} [${tx.gateway}]",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                val sign = if (tx.type == "Deposit" || tx.type == "Earnings" || tx.type == "Refund") "+" else "-"
                val color = if (tx.type == "Deposit" || tx.type == "Earnings" || tx.type == "Refund") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                
                Text(
                    text = "$sign৳${tx.amount.toInt()}",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = color
                )

                val sdf = SimpleDateFormat("HH:mm, dd MMM", Locale.getDefault())
                Text(
                    text = sdf.format(Date(tx.timestamp)),
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
