package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.User
import com.example.ui.theme.MyApplicationTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(
    viewModel: MarketplaceViewModel,
    isDarkTheme: Boolean,
    onToggleDarkTheme: () -> Unit
) {
    var activeTab by remember { mutableStateOf("Gigs") } // "Gigs", "Bids", "Dashboard", "Wallet", "Profile"

    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val currentUserId by viewModel.currentUserId.collectAsStateWithLifecycle()

    val gigs by viewModel.allGigs.collectAsStateWithLifecycle()
    val projects by viewModel.allProjects.collectAsStateWithLifecycle()
    val orders by viewModel.allOrders.collectAsStateWithLifecycle()
    val notifications by viewModel.allNotifications.collectAsStateWithLifecycle()
    val transactions by viewModel.allTransactions.collectAsStateWithLifecycle()

    val unreadNotificationsCount = remember(notifications) {
        notifications.count { !it.isRead }
    }

    MyApplicationTheme(darkTheme = isDarkTheme) {
        Scaffold(
            topBar = {
                Column {
                    // Main System Top Bar
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "TrustGig",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        },
                        actions = {
                            // Dark Mode Toggle Switch
                            IconButton(
                                onClick = onToggleDarkTheme,
                                modifier = Modifier.testTag("dark_mode_toggle")
                            ) {
                                Icon(
                                    imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                    contentDescription = "Toggle Dark Mode",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Notification Quick Badge Icon
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .clickable { activeTab = "Notifications" }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications list",
                                    tint = if (unreadNotificationsCount > 0) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onBackground
                                )
                                if (unreadNotificationsCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .background(MaterialTheme.colorScheme.error, CircleShape)
                                            .align(Alignment.TopEnd),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$unreadNotificationsCount",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    )

                    // Role & Active Switcher Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(
                                        if (currentUser?.isVerified == true) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.error,
                                        CircleShape
                                    )
                            )
                            Text(
                                text = "Logged: ",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = currentUser?.name ?: "Loading...",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Surface(
                                color = if (currentUser?.role == "Freelancer") MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = currentUser?.role?.uppercase() ?: "",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (currentUser?.role == "Freelancer") MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }

                        // Hotkey profile switcher text button for demo
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { activeTab = "Profile" }
                                .padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = "Ref verification or change ID",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.Cached,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            },
            bottomBar = {
                NavigationBar(
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    NavigationBarItem(
                        selected = activeTab == "Gigs",
                        onClick = { activeTab = "Gigs" },
                        icon = { Icon(Icons.Default.Explore, contentDescription = "Explore Gigs") },
                        label = { Text("Gigs", fontSize = 10.sp) },
                        modifier = Modifier.testTag("tab_gigs")
                    )

                    NavigationBarItem(
                        selected = activeTab == "Bids",
                        onClick = { activeTab = "Bids" },
                        icon = { Icon(Icons.Default.Gavel, contentDescription = "Bids Board") },
                        label = { Text("Bidding", fontSize = 10.sp) },
                        modifier = Modifier.testTag("tab_bidding")
                    )

                    NavigationBarItem(
                        selected = activeTab == "Dashboard",
                        onClick = { activeTab = "Dashboard" },
                        icon = { Icon(Icons.Default.AssignmentLate, contentDescription = "Work Center") },
                        label = { Text("Dashboard", fontSize = 10.sp) },
                        modifier = Modifier.testTag("tab_dashboard")
                    )

                    NavigationBarItem(
                        selected = activeTab == "Wallet",
                        onClick = { activeTab = "Wallet" },
                        icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Safe Wallet") },
                        label = { Text("Wallet", fontSize = 10.sp) },
                        modifier = Modifier.testTag("tab_wallet")
                    )

                    NavigationBarItem(
                        selected = activeTab == "Profile",
                        onClick = { activeTab = "Profile" },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Details") },
                        label = { Text("Profile", fontSize = 10.sp) },
                        modifier = Modifier.testTag("tab_profile")
                    )
                }
            },
            contentWindowInsets = WindowInsets.safeDrawing
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when(activeTab) {
                    "Gigs" -> {
                        ExploreScreen(
                            gigs = gigs,
                            currentUser = currentUser,
                            onBuyGig = { gig, cb -> viewModel.buyGig(gig, cb) }
                        )
                    }

                    "Bids" -> {
                        ProjectsScreen(
                            projects = projects,
                            currentUser = currentUser,
                            onPostProject = { t, d, b, c, cb -> viewModel.postProject(t, d, b, c, cb) },
                            onSubmitBid = { pid, ptitle, amt, prop, days, cb ->
                                viewModel.submitBid(pid, ptitle, amt, prop, days, cb)
                            },
                            getProjectBids = { pid -> viewModel.getBidsForProject(pid) },
                            onAcceptBid = { bidId, projectId, cb -> viewModel.acceptBid(bidId, projectId, cb) }
                        )
                    }

                    "Dashboard" -> {
                        DashboardScreen(
                            orders = orders,
                            currentUser = currentUser,
                            onSubmitWork = { oid, note -> viewModel.submitDeliverable(oid, note) },
                            onApproveOrder = { oid, cb -> viewModel.approveOrder(oid, cb) },
                            onCancelOrder = { oid, cb -> viewModel.cancelOrder(oid, cb) }
                        )
                    }

                    "Wallet" -> {
                        WalletScreen(
                            user = currentUser,
                            transactions = transactions,
                            onDeposit = { amount, gateway, ref -> viewModel.deposit(amount, gateway, ref) },
                            onWithdraw = { amount, gateway, ref, cb -> viewModel.withdraw(amount, gateway, ref, cb) }
                        )
                    }

                    "Profile" -> {
                        ProfileScreen(
                            user = currentUser,
                            allUsers = allUsers,
                            selectedUserId = currentUserId,
                            onUserSwitched = { uid -> viewModel.switchUser(uid) },
                            onVerifyNid = { nid, mobile, cb -> viewModel.verifyNid(nid, mobile, cb) }
                        )
                    }

                    "Notifications" -> {
                        NotificationScreen(
                            notifications = notifications,
                            onClearAll = { viewModel.readNotifications() }
                        )
                    }
                }
            }
        }
    }
}
