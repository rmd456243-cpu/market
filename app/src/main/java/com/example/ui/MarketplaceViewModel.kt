package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MarketplaceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MarketplaceRepository
    
    // --- State Streams ---
    val allUsers: StateFlow<List<User>>
    val allGigs: StateFlow<List<Gig>>
    val allProjects: StateFlow<List<Project>>
    val allOrders: StateFlow<List<Order>>
    val allNotifications: StateFlow<List<Notification>>
    val allTransactions: StateFlow<List<WalletTransaction>>

    // Current working user state
    private val _currentUserId = MutableStateFlow("user_client_sabbir")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    val currentUser: StateFlow<User?> = _currentUserId
        .flatMapLatest { userId -> repository.getUserFlow(userId) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    init {
        val database = AppDatabase.getDatabase(application)
        val dao = database.marketplaceDao()
        repository = MarketplaceRepository(dao)

        allUsers = repository.getAllUsers()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allGigs = repository.getAllGigs()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allProjects = repository.getAllProjects()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allOrders = repository.getAllOrders()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Stream notifications for the active user
        allNotifications = _currentUserId
            .flatMapLatest { userId -> repository.getNotificationsForUser(userId) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Stream transactions for the active user
        allTransactions = _currentUserId
            .flatMapLatest { userId -> repository.getTransactionsForUser(userId) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Seed default database values if empty
        viewModelScope.launch {
            seedInitialData()
        }
    }

    fun switchUser(userId: String) {
        _currentUserId.value = userId
    }

    private suspend fun seedInitialData() {
        val seededUsers = repository.getAllUsers().first().isNotEmpty()
        if (seededUsers) return // Already seeded

        // 1. Seed Users (Freelancers & Clients)
        val clientUser = User(
            id = "user_client_sabbir",
            name = "Sabbir Hasan",
            email = "sabbir@client.com",
            role = "Client",
            profileTitle = "Tech Entrepreneur",
            bio = "Always looking for experienced Kotlin and Compose developers to build local utility apps.",
            isVerified = true,
            nidNumber = "4602910394",
            mobileNumber = "01712345678",
            walletBalance = 35000.0,
            rating = 4.9
        )
        val freelancerAnik = User(
            id = "user_free_anik",
            name = "Anik Rahman",
            email = "anik@freelancer.com",
            role = "Freelancer",
            profileTitle = "Senior Android Developer",
            bio = "Expert in Jetpack Compose, Clean Architecture, Room Database, and API integrations.",
            skills = "Kotlin, Jetpack Compose, Room DB, Retrofit",
            isVerified = true,
            nidNumber = "9901824718",
            mobileNumber = "01911223344",
            walletBalance = 7500.0,
            rating = 5.0,
            completedProjectsCount = 12
        )
        val freelancerMorshed = User(
            id = "user_free_morshed",
            name = "Morshed Alam",
            email = "morshed@design.com",
            role = "Freelancer",
            profileTitle = "UI/UX Designer",
            bio = "Passionate designer specializing in minimalist dark mode themes, Material 3, and Web layouts.",
            skills = "Figma, Adobe XD, Material Design, Logo Design",
            isVerified = false,
            nidNumber = "",
            mobileNumber = "",
            walletBalance = 0.0,
            rating = 4.7,
            completedProjectsCount = 3
        )

        repository.saveUser(clientUser)
        repository.saveUser(freelancerAnik)
        repository.saveUser(freelancerMorshed)

        // 2. Seed Gigs
        repository.createGig(
            Gig(
                title = "High-performance bKash/Nagad Android Library Integration",
                description = "I will write a clean, secure local bKash and Nagad wrapper with full error-handling and custom UI overlays for Jetpack Compose apps.",
                price = 3500.0,
                deliveryDays = 3,
                category = "App Development",
                tags = "bKash, Nagad, Compose, Payment",
                sellerId = "user_free_anik",
                sellerName = "Anik Rahman"
            )
        )
        repository.createGig(
            Gig(
                title = "Premium Modern Material 3 UI/UX Design Mockups",
                description = "I'll prototype a beautiful modern dark-mode application UI in Figma based on Material 3 design directives. Ready for handover as vector designs.",
                price = 2500.0,
                deliveryDays = 5,
                category = "Design",
                tags = "UI/UX, Figma, Dark Mode, M3",
                sellerId = "user_free_morshed",
                sellerName = "Morshed Alam"
            )
        )

        // 3. Seed Projects
        repository.createProject(
            Project(
                title = "Bangla Ride Sharing App (Compose Interface)",
                description = "We need an elegant, edge-to-edge Compose ride booking and tracking screen with clean animations, maps API support, and local payment checkout.",
                budget = 15000.0,
                category = "Mobile Dev",
                clientId = "user_client_sabbir",
                clientName = "Sabbir Hasan"
            )
        )
        repository.createProject(
            Project(
                title = "E-Commerce Cart System with Offline Room Support",
                description = "Build a localized shopping cart with local persistence (Room Database) that retains items across sessions, manages counters reactive, and syncs easily.",
                budget = 8000.0,
                category = "App Development",
                clientId = "user_client_sabbir",
                clientName = "Sabbir Hasan"
            )
        )

        // 4. Seed initial notifications & transactions
        repository.sendSystemNotification(
            userId = "user_client_sabbir",
            title = "Welcome to TrustGig!",
            message = "Explore top rated Bangladeshi developers or post a custom project to review bids."
        )
        repository.sendSystemNotification(
            userId = "user_free_anik",
            title = "Welcome! Active Gig",
            message = "Your bKash & Nagad integration gig has been published and is visible to buyers."
        )
        repository.sendSystemNotification(
            userId = "user_free_morshed",
            title = "Verification Reminder",
            message = "Get verified today with your NID / National ID card to boost trust score and apply to premium bids."
        )

        repository.depositFunds("user_client_sabbir", 35000.0, "Nagad", "Deposited at setup")
        repository.depositFunds("user_free_anik", 7500.0, "Binance", "Freelanced earnings setup")
    }

    // --- Action Methods ---

    // Gig direct purchase
    fun buyGig(gig: Gig, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.buyGigDirectly(gig, _currentUserId.value)
            onResult(result)
        }
    }

    // Project creation
    fun postProject(title: String, description: String, budget: Double, category: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val user = currentUser.value
            if (user == null || user.role != "Client") {
                onResult(false)
                return@launch
            }
            val project = Project(
                title = title,
                description = description,
                budget = budget,
                category = category,
                clientId = user.id,
                clientName = user.name
            )
            val result = repository.createProject(project)
            onResult(result)
        }
    }

    // Submit bid
    fun submitBid(projectId: Int, projectTitle: String, amount: Double, proposal: String, days: Int, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val user = currentUser.value
            if (user == null || user.role != "Freelancer") {
                onResult(false)
                return@launch
            }
            val bid = Bid(
                projectId = projectId,
                projectTitle = projectTitle,
                freelancerId = user.id,
                freelancerName = user.name,
                freelancerRating = user.rating,
                bidAmount = amount,
                proposalText = proposal,
                deliveryDays = days
            )
            val result = repository.submitBid(bid)
            onResult(result)
        }
    }

    // Get live bids of a project reactive
    fun getBidsForProject(projectId: Int): Flow<List<Bid>> {
        return repository.getBidsForProject(projectId)
    }

    // Accept bid
    fun acceptBid(bidId: Int, projectId: Int, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.acceptProjectBid(bidId, projectId, _currentUserId.value)
            onResult(result)
        }
    }

    // Submit deliverables (Freelancer)
    fun submitDeliverable(orderId: Int, submissionNote: String) {
        viewModelScope.launch {
            repository.submitOrderWork(orderId, submissionNote, _currentUserId.value)
        }
    }

    // Approve & complete order (Client releases payment)
    fun approveOrder(orderId: Int, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.releaseEscrowAndCompleteOrder(orderId, _currentUserId.value)
            onResult(result)
        }
    }

    // Reject / Cancel project (Client refunds payment)
    fun cancelOrder(orderId: Int, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.cancelOrderAndRefund(orderId, _currentUserId.value)
            onResult(result)
        }
    }

    // Deposit Virtual Cash
    fun deposit(amount: Double, gateway: String, reference: String) {
        viewModelScope.launch {
            repository.depositFunds(_currentUserId.value, amount, gateway, reference)
        }
    }

    // Withdraw Cash
    fun withdraw(amount: Double, gateway: String, targetAccount: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.withdrawFunds(_currentUserId.value, amount, gateway, targetAccount)
            onResult(result)
        }
    }

    // NID verification
    fun verifyNid(nid: String, mobile: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.verifyUserNid(_currentUserId.value, nid, mobile)
            onResult(result)
        }
    }

    // Mark notification read
    fun readNotifications() {
        viewModelScope.launch {
            repository.clearNotifications(_currentUserId.value)
        }
    }
}
