package com.vistara.tourist_tracking_system.service;

import com.vistara.tourist_tracking_system.model.Booking;
import org.springframework.stereotype.Service;

@Service
public class MpesaService {
    public void initiatePayment(Booking booking) {
        // TODO: implement M-Pesa STK push
        System.out.println("Initiating M-Pesa payment for booking: " + booking.getBookingReference());
    }
}