package com.vistara.tourist_tracking_system.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.vistara.tourist_tracking_system.model.Booking;  // ✅ ADD THIS IMPORT
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@Slf4j
@Service
public class QRCodeService {

    /**
     * Generate QR code as Base64 string
     */
    public String generateQRCodeBase64(String data, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (WriterException | IOException e) {
            log.error("Error generating QR code: {}", e.getMessage());
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }

    /**
     * Generate QR code for a booking
     */
    public String generateBookingQRCode(Long bookingId, String bookingReference) {
        // Create QR data with booking information
        String qrData = String.format(
                "VISTARA|BOOKING|%s|%s",
                bookingId,
                bookingReference
        );
        return generateQRCodeBase64(qrData, 300, 300);
    }

    /**
     * Generate QR code with full booking details
     */
    public String generateBookingQRCodeFull(Booking booking) {
        // Create QR data with all booking information
        String qrData = String.format(
                "VISTARA|BOOKING|%d|%s|%s|%s|%s|%s|%s|%d",
                booking.getId(),
                booking.getBookingReference(),
                booking.getUser().getFullName(),
                booking.getUser().getEmail(),
                booking.getUser().getPhoneNumber(),
                booking.getCheckInDate().toString(),
                booking.getCheckOutDate().toString(),
                booking.getGroupSize()
        );
        return generateQRCodeBase64(qrData, 300, 300);
    }

    /**
     * Generate QR Code for a booking
     */
    public String generateQRCode(Booking booking) {
        try {
            // Create QR code data
            String qrData = String.format(
                    "{\"bookingId\":%d,\"reference\":\"%s\",\"visitor\":\"%s\",\"checkIn\":\"%s\",\"checkOut\":\"%s\"}",
                    booking.getId(),
                    booking.getBookingReference(),
                    booking.getUser().getFullName(),
                    booking.getCheckInDate().toString(),
                    booking.getCheckOutDate().toString()
            );

            // Generate QR code
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrData, BarcodeFormat.QR_CODE, 300, 300);

            // Convert to Base64
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            return Base64.getEncoder().encodeToString(outputStream.toByteArray());

        } catch (WriterException | IOException e) {
            log.error("Failed to generate QR Code for booking {}: {}", booking.getBookingReference(), e.getMessage());
            throw new RuntimeException("Failed to generate QR Code", e);
        }
    }
}