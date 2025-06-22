package com.backendspa;

import com.backendspa.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class BackendSpaApplicationTests {

	@Mock
	private EmailService emailService;

	@Configuration
	static class TestConfig {
		@Bean
		EmailService emailService() {
			return null; // Este bean será reemplazado por el mock
		}
	}

	@BeforeEach
	void setUp() {
	}

	@Test
	void contextLoads() {
	}

	@Test
	void testEmailServiceInteraction() throws IOException {
		// Simular una llamada a sendEmailWithAttachment
		String toEmail = "test@example.com";
		String subject = "Test Subject";
		String body = "Test Body";
		String attachmentBase64 = "data:application/pdf;base64,SGVsbG8=";
		String attachmentName = "test.pdf";

		emailService.sendEmailWithAttachment(toEmail, subject, body, attachmentBase64, attachmentName);

		// Verificar que el metodo fue llamado con los parametros correctos
		verify(emailService, times(1)).sendEmailWithAttachment(toEmail, subject, body, attachmentBase64, attachmentName);
	}
}