package com.example.vistaraapp.database

import com.example.vistaraapp.data.SessionManager

//this  file defines all the possible things the user can do with the contact/ profile data
//It’s a list of commands that the user or the screen can send to the “brain” (ViewModel).
sealed interface ContactEvent {
    data class Logout(val sessionManager: SessionManager, val onLogoutComplete: () -> Unit) : ContactEvent
    data object SaveContact : ContactEvent
    data object HideDialog : ContactEvent
    data object ShowDialog : ContactEvent
    data object PrepareEditProfile : ContactEvent

    data class SetFullName(val fullName: String) : ContactEvent
    data class SetEmail(val email: String) : ContactEvent
    data class SetPhoneNumber(val phoneNumber: String) : ContactEvent
    data class SetIdNumber(val idNumber: String) : ContactEvent
    data class SetEmergencyNumber(val emergencyNumber: String) : ContactEvent

    // Useful if you ever need to toggle sorting order in your UI
    data class SortContacts(val sortType: SortType) : ContactEvent
    data class DeleteContact(val contact: Contact) : ContactEvent


    // BOOKING FORM EVENTS
    data class EnteredCheckInDate(val checkInDate: String) : ContactEvent
    data class EnteredCheckOutDate(val checkOutDate: String) : ContactEvent
    data class EnteredGroupSize(val groupSize: Int) : ContactEvent
    data class EnteredVehicleRegistration(val vehicleRegistration: String) : ContactEvent
    data class EnteredPaymentMethod(val paymentMethod: String) : ContactEvent
    data class EnteredAmount(val amount: Double) : ContactEvent



    data class EnteredPhoneNumber(val phoneNumber: String) : ContactEvent
    // ACTIONS
    data object ResetBookingState : ContactEvent
    data object CreateBooking : ContactEvent
    data object DismissPaymentDialog : ContactEvent
    data class ConfirmBookingPayment(val phoneNumber: String) : ContactEvent
}