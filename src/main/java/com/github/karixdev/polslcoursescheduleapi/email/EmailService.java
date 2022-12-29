package com.github.karixdev.polslcoursescheduleapi.email;

import com.github.karixdev.polslcoursescheduleapi.email.exception.SendingEmailException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    @Value("${email-sender.sender}")
    private String sender;

    @Async
    public void sendEmailToUser(String recipientEmail, String topic, String body) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper =
                new MimeMessageHelper(mimeMessage, "utf-8");

        try {
            helper.setFrom(sender);
            helper.setTo(recipientEmail);
            helper.setSubject(topic);
            helper.setText(body, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            log.error("Error while sending email", e);
            throw new SendingEmailException();
        }
    }

    public String getMailTemplate(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);

        return templateEngine.process(templateName, context);
    }
}
