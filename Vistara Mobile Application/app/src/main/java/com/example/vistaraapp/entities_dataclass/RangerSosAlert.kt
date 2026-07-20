package com.example.vistaraapp.entities_dataclass

data class RangerSosAlert(
    val id: String,
    val visitorName: String,
    val alertType: String,
    val status: String, // "Unresolved", "Assigned", "Resolved"
    val assignedRanger: String? = null
)
