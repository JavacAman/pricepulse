package com.pricepulse.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendPriceDropAlert(String toEmail,
                                   String productName,
                                   Double targetPrice,
                                   Double currentPrice) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("PricePulse Alert: "
                    + productName + " price dropped!");
            message.setText(
                    "Hello!\n\n" +
                            "Great news! The price of " + productName +
                            " has dropped below your target price.\n\n" +
                            "Your Target Price: ₹" + targetPrice + "\n" +
                            "Current Price: ₹" + currentPrice + "\n\n" +
                            "Login to PricePulse to grab this deal!\n\n" +
                            "Happy Shopping!\n" +
                            "PricePulse Team"
            );
            mailSender.send(message);
            log.info("Email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}",
                    toEmail, e.getMessage());
        }
    }
}