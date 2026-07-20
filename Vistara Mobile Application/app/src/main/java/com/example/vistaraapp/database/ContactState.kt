package com.example.vistaraapp.database

// This file holds all the data that describes what the screen looks like at any moment
data class ContactState(
    // EXISTING CONTACT/PROFILE FIELDS
    val contacts: List<Contact> = emptyList(),
    val userProfile: Contact? = null,   // Always the single profile row (id = 1)
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val idNumber: String = "",
    val emergencyNumber: String = "",
    val isAddingContact: Boolean = false, // Useful for showing/hiding an input dialog
    val sortType: SortType = SortType.FULL_NAME, // Tracks how the list is currently sorted
    val isSyncing: Boolean = false,

    // BOOKING FORM FIELDS
    val checkInDate: String = "",
    val checkOutDate: String = "",
    val groupSize: Int = 1,
    val vehicleRegistration: String = "",
    val paymentMethod: String = "MPESA", // Default to MPESA
    val amount: Double = 0.0,

    // STATE TRACKERS
    val bookingReference: String? = null,
    val showPaymentDialog: Boolean = false,
    val isBookingLoading: Boolean = false,
    val bookingErrorMessage: String? = null,
    val isBookingSuccessful: Boolean = false,
    val isBookingFailed: Boolean = false
)