package com.github.karixdev.polslcoursescheduleapi.email;

import com.github.karixdev.polslcoursescheduleapi.ContainersEnvironment;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.mail.internet.MimeMessage;
import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmailServiceIT extends ContainersEnvironment {
    @Autowired
    EmailService underTest;

    @RegisterExtension
    static GreenMailExtension greenMail =
            new GreenMailExtension(ServerSetupTest.SMTP)
                    .withConfiguration(GreenMailConfiguration.aConfig()
                            .withUser("greenmail-user", "greenmail-password"))
                    .withPerMethodLifecycle(false);

    @Test
    void shouldSendCorrectEmail() {
        underTest.sendEmailToUser(
                "recipient@email.com",
                "test-mail",
                "<p>test mail</p>"
        );

        await().atMost(2, SECONDS).untilAsserted(() -> {
            MimeMessage[] receivedMessages = greenMail.getReceivedMessages();

            assertThat(receivedMessages).hasSize(1);

            MimeMessage receivedMessage = receivedMessages[0];

            assertThat(receivedMessage.getContent()).isEqualTo("<p>test mail</p>");
            assertThat(receivedMessage.getSubject()).isEqualTo("test-mail");
            assertThat(receivedMessage.getFrom()).hasSize(1);

            String from = receivedMessage.getFrom()[0].toString();
            assertThat(from).isEqualTo("test@polsl-course-schedule-api.com");

            assertThat(receivedMessage.getAllRecipients()).hasSize(1);

            String recipient = receivedMessage.getAllRecipients()[0].toString();
            assertThat(recipient).isEqualTo("recipient@email.com");
        });
    }

    @Test
    void shouldLoadTemplateWithVariables() {
        Map<String, Object> variables = Map.of("name", "test");

        String result = underTest.getMailTemplate("test.html", variables);

        assertThat(result).isEqualTo("<p>test</p>");
    }

}

