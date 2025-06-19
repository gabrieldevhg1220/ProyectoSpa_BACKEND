package com.backendspa.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;

@Service
public class EmailService {

    @Value("${sendgrid.api-key}")
    private String sendGridApiKey;

    public void sendEmailWithAttachment(String toEmail, String subject, String body, byte[] attachment, String attachmentName) throws IOException {
        Email from = new Email("mdssentirsebien@gmail.com"); // Cambia por el correo verificado en SendGrid
        Email to = new Email(toEmail);
        Content content = new Content("text/plain", body);
        Mail mail = new Mail(from, subject, to, content);

        // Añadir el adjunto (PDF)
        Attachments attachments = new Attachments();
        attachments.setContent(Base64.getEncoder().encodeToString(attachment));
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
                throw new IOException("Error enviando correo: " + response.getBody());
            }
        } catch (IOException e) {
            throw new IOException("No se pudo enviar el correo: " + e.getMessage(), e);
        }
    }
}
