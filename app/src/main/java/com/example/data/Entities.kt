package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String, // e.g. "user_1", "user_2"
    val name: String,
    val email: String,
    val role: String, // "Freelancer" or "Client"
    val profileTitle: String = "",
    val bio: String = "",
    val skills: String = "", // comma-separated list
    val isVerified: Boolean = false,
    val nidNumber: String = "",
    val mobileNumber: String = "",
    val walletBalance: Double = 0.0,
    val rating: Double = 5.0,
    val completedProjectsCount: Int = 0
) : Serializable

@Entity(tableName = "gigs")
data class Gig(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val price: Double,
    val deliveryDays: Int,
    val category: String,
    val tags: String = "",
    val sellerId: String,
    val sellerName: String,
    val sellerRating: Double = 5.0
) : Serializable

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val budget: Double,
    val category: String,
    val clientId: String,
    val clientName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "Open", // "Open", "Ongoing", "Completed"
    val selectedFreelancerId: String? = null
) : Serializable

@Entity(tableName = "bids")
data class Bid(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectId: Int,
    val projectTitle: String,
    val freelancerId: String,
    val freelancerName: String,
    val freelancerRating: Double = 5.0,
    val bidAmount: Double,
    val proposalText: String,
    val deliveryDays: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "Pending" // "Pending", "Accepted", "Rejected"
) : Serializable

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val type: String, // "GIG_PURCHASE" or "PROJECT_CONTRACT"
    val refId: Int, // either gigId or projectId
    val price: Double,
    val clientName: String, // who buys
    val clientId: String,
    val freelancerName: String, // who executes
    val freelancerId: String,
    val status: String = "Pending", // "Pending", "Ongoing", "Completed", "Cancelled"
    val description: String = "",
    val submissionNote: String = "", // submitted deliverables description
    val timestamp: Long = System.currentTimeMillis(),
    val paymentStatus: String = "Escrowed" // "Escrowed", "Released", "Refunded"
) : Serializable

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
) : Serializable

@Entity(tableName = "transactions")
data class WalletTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val type: String, // "Deposit", "Withdraw", "Payment", "Earnings", "Refund", "EscrowHold"
    val amount: Double,
    val gateway: String, // "bKash", "Nagad", "Rocket", "Upay", "Binance", "USDT"
    val accountNumberOrTxId: String, // detail info
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "Success" // "Success", "Pending"
) : Serializable
