package com.pricepulse.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // JUnit5: enables Mockito annotations
class EmailServiceTest {

    // Mockito: mocking JavaMailSender — no real SMTP server needed in unit tests
    @Mock private JavaMailSender mailSender;

    // Mockito: @InjectMocks creates EmailService and injects the mocked JavaMailSender
    @InjectMocks private EmailService emailService;

    // Mockito: ArgumentCaptor captures the exact SimpleMailMessage passed to mailSender.send()
    // so we can inspect its fields (to, subject, text) in assertions
    @Captor private ArgumentCaptor<SimpleMailMessage> messageCaptor;

    // ─────────────────────────────────────────────────────────────────────────
    // sendPriceDropAlert() tests
    // ─────────────────────────────────────────────────────────────────────────

    // JUnit5: testing sendPriceDropAlert() - email sent with correct content
    // Mockito: mocking JavaMailSender.send; using ArgumentCaptor to verify message content
    // AAA Pattern: Arrange-Act-Assert
    @Test
    @DisplayName("sendPriceDropAlert() - should send email with correct recipient, subject, and body")
    void sendPriceDropAlert_validInputs_sendsEmailWithCorrectContent() {
        // ── Arrange ──────────────────────────────────────────────────────────
        // doNothing is the default for void methods on mocks, but explicit is clearer
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // ── Act ──────────────────────────────────────────────────────────────
        emailService.sendPriceDropAlert(
                "user@example.com",
                "iPhone 15",
                999.0,
                849.99
        );

        // ── Assert ───────────────────────────────────────────────────────────
        // Capture the SimpleMailMessage that was actually passed to send()
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();

        // Verify recipient
        assertNotNull(sentMessage.getTo());
        assertEquals("user@example.com", sentMessage.getTo()[0]);

        // Verify subject contains the product name
        assertNotNull(sentMessage.getSubject());
        assertTrue(sentMessage.getSubject().contains("iPhone 15"),
                "Subject should mention the product name");

        // Verify email body contains price details
        assertNotNull(sentMessage.getText());
        assertTrue(sentMessage.getText().contains("999.0"),
                "Body should contain the target price");
        assertTrue(sentMessage.getText().contains("849.99"),
                "Body should contain the current (dropped) price");
        assertTrue(sentMessage.getText().contains("iPhone 15"),
                "Body should mention the product name");
    }

    // JUnit5: testing sendPriceDropAlert() - verifies email is sent exactly once
    // Mockito: verifying send() is called exactly one time with any SimpleMailMessage
    // AAA Pattern: Arrange-Act-Assert
    @Test
    @DisplayName("sendPriceDropAlert() - should call mailSender.send() exactly once per invocation")
    void sendPriceDropAlert_calledOnce_invokesMailSenderOnce() {
        // ── Arrange ──────────────────────────────────────────────────────────
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // ── Act ──────────────────────────────────────────────────────────────
        emailService.sendPriceDropAlert("a@b.com", "Laptop", 1200.0, 999.0);

        // ── Assert ───────────────────────────────────────────────────────────
        // times(1) verifies the mock was called exactly once — not zero, not twice
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    // JUnit5: testing sendPriceDropAlert() - graceful failure when SMTP throws
    // Mockito: mocking send() to throw RuntimeException; verifying no exception propagates
    // AAA Pattern: Arrange-Act-Assert
    @Test
    @DisplayName("sendPriceDropAlert() - should swallow exception and NOT throw when mail server fails")
    void sendPriceDropAlert_smtpError_doesNotPropagateException() {
        // ── Arrange ──────────────────────────────────────────────────────────
        // Simulate SMTP server being down or refusing connection
        doThrow(new RuntimeException("SMTP connection refused"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // ── Act & Assert ──────────────────────────────────────────────────────
        // EmailService catches all exceptions internally and logs them.
        // The caller should NEVER receive an exception from a failed email.
        assertDoesNotThrow(() ->
                emailService.sendPriceDropAlert("user@example.com", "iPhone", 999.0, 849.0),
                "Email failures should be logged, not propagated to the caller"
        );

        // mailSender.send() WAS called — the failure happened inside send(), not before
        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}
