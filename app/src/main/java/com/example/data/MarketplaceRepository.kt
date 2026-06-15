package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.io.Serializable

class MarketplaceRepository(private val dao: MarketplaceDao) {

    // --- Users ---
    fun getUserFlow(userId: String): Flow<User?> = dao.getUserByIdFlow(userId)
    
    suspend fun getUser(userId: String): User? = dao.getUserById(userId)

    suspend fun saveUser(user: User) {
        dao.insertUser(user)
    }

    fun getAllUsers(): Flow<List<User>> = dao.getAllUsersFlow()

    // Verification - NID verification
    suspend fun verifyUserNid(userId: String, nid: String, mobile: String): Boolean {
        val user = dao.getUserById(userId) ?: return false
        val updatedUser = user.copy(
            isVerified = true,
            nidNumber = nid,
            mobileNumber = mobile
        )
        dao.insertUser(updatedUser)
        
        // Auto Notification
        sendSystemNotification(
            userId = userId,
            title = "NID Verification Successful",
            message = "Congratulations! Your profile has been verified successfully. NID: $nid"
        )
        return true
    }

    // --- Gigs ---
    fun getAllGigs(): Flow<List<Gig>> = dao.getAllGigsFlow()
    
    fun getGigsBySeller(sellerId: String): Flow<List<Gig>> = dao.getGigsBySellerFlow(sellerId)

    suspend fun createGig(gig: Gig) {
        dao.insertGig(gig)
        sendSystemNotification(
            userId = gig.sellerId,
            title = "Gig Published",
            message = "Your gig '${gig.title}' is now live for buyers to purchase."
        )
    }

    suspend fun removeGig(gigId: Int, sellerId: String) {
        dao.deleteGig(gigId)
    }

    // --- Projects ---
    fun getAllProjects(): Flow<List<Project>> = dao.getAllProjectsFlow()
    
    fun getProjectsByClient(clientId: String): Flow<List<Project>> = dao.getProjectsByClientFlow(clientId)

    suspend fun createProject(project: Project): Boolean {
        dao.insertProject(project)
        sendSystemNotification(
            userId = project.clientId,
            title = "Project Posted",
            message = "Your project '${project.title}' with budget ৳${project.budget} has been posted."
        )
        return true
    }

    // --- Bids ---
    fun getBidsForProject(projectId: Int): Flow<List<Bid>> = dao.getBidsForProjectFlow(projectId)
    
    fun getBidsForFreelancer(freelancerId: String): Flow<List<Bid>> = dao.getBidsForFreelancerFlow(freelancerId)

    suspend fun submitBid(bid: Bid): Boolean {
        dao.insertBid(bid)
        // Notify Client
        val project = dao.getProjectById(bid.projectId)
        if (project != null) {
            sendSystemNotification(
                userId = project.clientId,
                title = "New Bid on Your Project",
                message = "Freelancer ${bid.freelancerName} has bid ৳${bid.bidAmount} on '${project.title}'"
            )
        }
        return true
    }

    // Accept a Bid -> Create Ongoing Project Contract Order
    suspend fun acceptProjectBid(bidId: Int, projectId: Int, clientId: String): Boolean {
        val project = dao.getProjectById(projectId) ?: return false
        val bids = dao.getBidsForProjectFlow(projectId).firstOrNull() ?: emptyList()
        val acceptedBid = bids.find { it.id == bidId } ?: return false
        
        val client = dao.getUserById(clientId) ?: return false
        
        // Escrow Check: Ensure client has enough balance
        if (client.walletBalance < acceptedBid.bidAmount) {
            sendSystemNotification(
                userId = clientId,
                title = "Insufficient Balance to Accept Bid",
                message = "You need at least ৳${acceptedBid.bidAmount} in your wallet to accept this bid."
            )
            return false
        }

        // 1. Deduct client balance (Escrow Hold)
        val updatedClient = client.copy(walletBalance = client.walletBalance - acceptedBid.bidAmount)
        dao.insertUser(updatedClient)

        // 2. Add Escrow Hold transaction
        dao.insertTransaction(
            WalletTransaction(
                userId = clientId,
                type = "EscrowHold",
                amount = acceptedBid.bidAmount,
                gateway = "Virtual Escrow",
                accountNumberOrTxId = "Escrow for project #${project.id}",
                status = "Success"
            )
        )

        // 3. Update Project status
        val updatedProject = project.copy(
            status = "Ongoing",
            selectedFreelancerId = acceptedBid.freelancerId
        )
        dao.insertProject(updatedProject)

        // 4. Update Bid status
        dao.updateBidStatus(bidId, "Accepted")
        
        // Reject all other bids for this project
        bids.forEach { bid ->
            if (bid.id != bidId) {
                dao.updateBidStatus(bid.id, "Rejected")
                sendSystemNotification(
                    userId = bid.freelancerId,
                    title = "Bid Rejected",
                    message = "Your bid on '${project.title}' was unsuccessful."
                )
            }
        }

        // 5. Create active Order
        dao.insertOrder(
            Order(
                title = project.title,
                type = "PROJECT_CONTRACT",
                refId = project.id,
                price = acceptedBid.bidAmount,
                clientName = client.name,
                clientId = clientId,
                freelancerName = acceptedBid.freelancerName,
                freelancerId = acceptedBid.freelancerId,
                status = "Ongoing",
                description = project.description,
                paymentStatus = "Escrowed"
            )
        )

        // 6. Notify both parts
        sendSystemNotification(
            userId = clientId,
            title = "Project Started",
            message = "You started ongoing contract with ${acceptedBid.freelancerName} for ৳${acceptedBid.bidAmount}. Funds held in escrow securely."
        )
        sendSystemNotification(
            userId = acceptedBid.freelancerId,
            title = "Bid Accepted!",
            message = "Congratulations! Client ${client.name} has accepted your bid on '${project.title}' for ৳${acceptedBid.bidAmount}."
        )

        return true
    }

    // --- Order Operations ---
    fun getAllOrders(): Flow<List<Order>> = dao.getAllOrdersFlow()
    fun getOrdersForClient(clientId: String): Flow<List<Order>> = dao.getOrdersForClientFlow(clientId)
    fun getOrdersForFreelancer(freelancerId: String): Flow<List<Order>> = dao.getOrdersForFreelancerFlow(freelancerId)

    // Purchase a Gig directly
    suspend fun buyGigDirectly(gig: Gig, clientId: String): Boolean {
        val client = dao.getUserById(clientId) ?: return false
        
        // Check escrow balance
        if (client.walletBalance < gig.price) {
            sendSystemNotification(
                userId = clientId,
                title = "Purchase Failed",
                message = "Insufficient balance to purchase gig: '${gig.title}'. Please deposit money first."
            )
            return false
        }

        // 1. Deduct client balance
        val updatedClient = client.copy(walletBalance = client.walletBalance - gig.price)
        dao.insertUser(updatedClient)

        // 2. Add Escrow Hold transaction
        dao.insertTransaction(
            WalletTransaction(
                userId = clientId,
                type = "EscrowHold",
                amount = gig.price,
                gateway = "Virtual Escrow",
                accountNumberOrTxId = "Escrow for Gig purchase",
                status = "Success"
            )
        )

        // 3. Create active Order
        dao.insertOrder(
            Order(
                title = gig.title,
                type = "GIG_PURCHASE",
                refId = gig.id,
                price = gig.price,
                clientName = client.name,
                clientId = clientId,
                freelancerName = gig.sellerName,
                freelancerId = gig.sellerId,
                status = "Ongoing",
                description = gig.description,
                paymentStatus = "Escrowed"
            )
        )

        // 4. Notifications
        sendSystemNotification(
            userId = clientId,
            title = "Gig Purchased Successfully",
            message = "You purchased '${gig.title}' for ৳${gig.price}. Funds are now secure in TrustEscrow."
        )
        sendSystemNotification(
            userId = gig.sellerId,
            title = "New Gig Order Alert!",
            message = "Client ${client.name} has purchased your gig '${gig.title}' for ৳${gig.price}. Please start working!"
        )

        return true
    }

    // Freelancer submits work
    suspend fun submitOrderWork(orderId: Int, submissionNote: String, freelancerId: String) {
        dao.submitOrderWork(orderId, submissionNote)
        
        // Notify the client
        val orders = dao.getOrdersForFreelancerFlow(freelancerId).firstOrNull() ?: return
        val order = orders.find { it.id == orderId } ?: return
        
        sendSystemNotification(
            userId = order.clientId,
            title = "Work Submitted for Review",
            message = "Freelancer ${order.freelancerName} has submitted work for '${order.title}'. Please review and approve."
        )
    }

    // Client approves work -> Releases funds from escrow to freelancer
    suspend fun releaseEscrowAndCompleteOrder(orderId: Int, clientId: String): Boolean {
        val orders = dao.getOrdersForClientFlow(clientId).firstOrNull() ?: return false
        val order = orders.find { it.id == orderId } ?: return false
        
        if (order.status == "Completed") return false

        val freelancer = dao.getUserById(order.freelancerId) ?: return false

        // 1. Release payment: Credit Freelancer
        val updatedFreelancer = freelancer.copy(
            walletBalance = freelancer.walletBalance + order.price,
            completedProjectsCount = freelancer.completedProjectsCount + 1
        )
        dao.insertUser(updatedFreelancer)

        // 2. Finalize order status in db
        dao.finalizeOrderPayment(orderId, paymentStatus = "Released", status = "Completed")

        // 3. Add transaction logs for freelancer
        dao.insertTransaction(
            WalletTransaction(
                userId = order.freelancerId,
                type = "Earnings",
                amount = order.price,
                gateway = "Virtual Escrow",
                accountNumberOrTxId = "Earnings from Order #${order.id}",
                status = "Success"
            )
        )

        // If it was a project, update project status to Completed
        if (order.type == "PROJECT_CONTRACT") {
            val project = dao.getProjectById(order.refId)
            if (project != null) {
                dao.insertProject(project.copy(status = "Completed"))
            }
        }

        // 4. Send notifications
        sendSystemNotification(
            userId = clientId,
            title = "Order Approved & Completed",
            message = "You have approved the work for '${order.title}'. ৳${order.price} has been paid securely."
        )
        sendSystemNotification(
            userId = order.freelancerId,
            title = "Payment Released! ৳${order.price}",
            message = "Good job! Client has approved your work for '${order.title}'. ৳${order.price} has been credited to your wallet."
        )

        return true
    }

    // Cancel order -> Refund Client
    suspend fun cancelOrderAndRefund(orderId: Int, clientId: String): Boolean {
        val orders = dao.getOrdersForClientFlow(clientId).firstOrNull() ?: return false
        val order = orders.find { it.id == orderId } ?: return false
        
        if (order.status == "Completed" || order.status == "Cancelled") return false

        val client = dao.getUserById(clientId) ?: return false

        // Refund client
        val updatedClient = client.copy(walletBalance = client.walletBalance + order.price)
        dao.insertUser(updatedClient)

        // Update order status
        dao.finalizeOrderPayment(orderId, paymentStatus = "Refunded", status = "Cancelled")

        // Record refund log
        dao.insertTransaction(
            WalletTransaction(
                userId = clientId,
                type = "Refund",
                amount = order.price,
                gateway = "Virtual Escrow",
                accountNumberOrTxId = "Escrow Refund for order #${order.id}",
                status = "Success"
            )
        )

        // Notification
        sendSystemNotification(
            userId = clientId,
            title = "Order Cancelled & Refunded",
            message = "Order '${order.title}' has been cancelled. ৳${order.price} has been returned to your wallet balance."
        )
        sendSystemNotification(
            userId = order.freelancerId,
            title = "Order Cancelled by Client",
            message = "Order '${order.title}' was cancelled, escrow refunded to client."
        )

        return true
    }

    // --- Notifications ---
    fun getNotificationsForUser(userId: String): Flow<List<Notification>> = dao.getNotificationsForUserFlow(userId)

    suspend fun sendSystemNotification(userId: String, title: String, message: String) {
        dao.insertNotification(
            Notification(
                userId = userId,
                title = title,
                message = message
            )
        )
    }

    suspend fun clearNotifications(userId: String) {
        dao.markNotificationsAsRead(userId)
    }

    // --- Wallet Transactions ---
    fun getTransactionsForUser(userId: String): Flow<List<WalletTransaction>> = dao.getTransactionsForUserFlow(userId)

    suspend fun depositFunds(userId: String, amount: Double, gateway: String, senderAccountOrTx: String): Boolean {
        val user = dao.getUserById(userId) ?: return false
        val updatedUser = user.copy(walletBalance = user.walletBalance + amount)
        dao.insertUser(updatedUser)

        dao.insertTransaction(
            WalletTransaction(
                userId = userId,
                type = "Deposit",
                amount = amount,
                gateway = gateway,
                accountNumberOrTxId = senderAccountOrTx,
                status = "Success"
            )
        )

        sendSystemNotification(
            userId = userId,
            title = "Deposit Successful",
            message = "Fund of ${if(gateway == "Binance" || gateway == "USDT") "$$amount (~৳${amount * 115})" else "৳$amount"} deposited successfully via $gateway."
        )
        return true
    }

    suspend fun withdrawFunds(userId: String, amount: Double, gateway: String, receiverAccount: String): Boolean {
        val user = dao.getUserById(userId) ?: return false
        
        if (user.walletBalance < amount) {
            sendSystemNotification(
                userId = userId,
                title = "Withdrawal Failed",
                message = "You do not have enough balance to withdraw ৳$amount."
            )
            return false
        }

        val updatedUser = user.copy(walletBalance = user.walletBalance - amount)
        dao.insertUser(updatedUser)

        dao.insertTransaction(
            WalletTransaction(
                userId = userId,
                type = "Withdraw",
                amount = amount,
                gateway = gateway,
                accountNumberOrTxId = receiverAccount,
                status = "Success"
            )
        )

        sendSystemNotification(
            userId = userId,
            title = "Withdrawal Requested",
            message = "Your request to withdraw ৳$amount to $receiverAccount ($gateway) is under process. Transfer completes standardly within 1 hour."
        )
        return true
    }
}
