package com.backendspa.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Value("${sendgrid.api-key}")
    private String sendGridApiKey;

    @Value("${sendgrid.from-email}")
    private String fromEmail;

    public void sendEmailWithAttachment(String toEmail, String subject, String body, String attachmentBase64, String attachmentName) throws IOException {
        if (sendGridApiKey == null || sendGridApiKey.trim().isEmpty()) {
            throw new IllegalStateException("La clave API de SendGrid no está configurada.");
        }
        if (fromEmail == null || fromEmail.trim().isEmpty()) {
            throw new IllegalStateException("El email de origen de SendGrid no está configurado.");
        }

        Email from = new Email(fromEmail);
        Email to = new Email(toEmail);
        Content content = new Content("text/plain", body);
        Mail mail = new Mail(from, subject, to, content);

        // Añadir el adjunto (PDF)
        Attachments attachments = new Attachments();
        attachments.setContent(attachmentBase64);
        attachments.setType("application/pdf");
        attachments.setFilename(attachmentName);
        attachments.setDisposition("attachment");
        mail.addAttachments(attachments);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            if (response.getStatusCode() >= 400) {
                logger.error("Error al enviar correo a {}: Código {}, Cuerpo: {}", toEmail, response.getStatusCode(), response.getBody());
                throw new IOException("Error enviando correo: " + response.getBody());
            }
            logger.info("Correo enviado exitosamente a {} con estado {}", toEmail, response.getStatusCode());
        } catch (IOException e) {
            logger.error("Error al enviar correo a {}: {}", toEmail, e.getMessage(), e);
            throw new IOException("No se pudo enviar el correo: " + e.getMessage(), e);
        }
    }
}