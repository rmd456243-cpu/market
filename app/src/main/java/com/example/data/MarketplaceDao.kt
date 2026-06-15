package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MarketplaceDao {
    // --- User Queries ---
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserByIdFlow(userId: String): Flow<User?>

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<User>>

    // --- Gig Queries ---
    @Query("SELECT * FROM gigs ORDER BY id DESC")
    fun getAllGigsFlow(): Flow<List<Gig>>

    @Query("SELECT * FROM gigs WHERE sellerId = :sellerId ORDER BY id DESC")
    fun getGigsBySellerFlow(sellerId: String): Flow<List<Gig>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGig(gig: Gig)

    @Query("DELETE FROM gigs WHERE id = :gigId")
    suspend fun deleteGig(gigId: Int)

    // --- Project Queries ---
    @Query("SELECT * FROM projects ORDER BY timestamp DESC")
    fun getAllProjectsFlow(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectById(id: Int): Project?

    @Query("SELECT * FROM projects WHERE clientId = :clientId ORDER BY timestamp DESC")
    fun getProjectsByClientFlow(clientId: String): Flow<List<Project>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: Project): Long

    // --- Bid Queries ---
    @Query("SELECT * FROM bids WHERE projectId = :projectId ORDER BY timestamp DESC")
    fun getBidsForProjectFlow(projectId: Int): Flow<List<Bid>>

    @Query("SELECT * FROM bids WHERE freelancerId = :freelancerId ORDER BY timestamp DESC")
    fun getBidsForFreelancerFlow(freelancerId: String): Flow<List<Bid>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBid(bid: Bid)

    @Query("UPDATE bids SET status = :status WHERE id = :bidId")
    suspend fun updateBidStatus(bidId: Int, status: String)

    // --- Order Queries ---
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrdersFlow(): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE clientId = :clientId ORDER BY timestamp DESC")
    fun getOrdersForClientFlow(clientId: String): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE freelancerId = :freelancerId ORDER BY timestamp DESC")
    fun getOrdersForFreelancerFlow(freelancerId: String): Flow<List<Order>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order): Long

    @Query("UPDATE orders SET status = :status WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Int, status: String)

    @Query("UPDATE orders SET submissionNote = :submissionNote, status = 'Delivered' WHERE id = :orderId")
    suspend fun submitOrderWork(orderId: Int, submissionNote: String)

    @Query("UPDATE orders SET paymentStatus = :paymentStatus, status = :status WHERE id = :orderId")
    suspend fun finalizeOrderPayment(orderId: Int, paymentStatus: String, status: String)

    // --- Notification Queries ---
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun getNotificationsForUserFlow(userId: String): Flow<List<Notification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: Notification)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markNotificationsAsRead(userId: String)

    // --- WalletTransaction Queries ---
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getTransactionsForUserFlow(userId: String): Flow<List<WalletTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: WalletTransaction)
}
