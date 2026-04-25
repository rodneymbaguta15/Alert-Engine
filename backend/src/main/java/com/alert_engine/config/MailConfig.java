package com.alert_engine.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * Builds a JavaMailSender configured for Mailtrap (or whatever SMTP provider
 * properties you supply). The actual sending wrapper also checks MAIL_ENABLED —
 * when false, the email channel logs instead of sending.
 * <p>
 * Kept separate from Spring Boot's auto-configured mail sender so we can
 * customize STARTTLS settings for Mailtrap's sandbox.
 */
@Configuration
@ConfigurationProperties(prefix = "mail")
@Getter
@Setter
public class MailConfig {

    private String host;
    private int port;
    private String username;
    private String password;
    private String from;
    private boolean enabled = true;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setPort(port);
        sender.setUsername(username);
        sender.setPassword(password);

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        // STARTTLS on port 587 (Mailtrap's recommended TLS port).

        props.put("mail.smtp.starttls.enable", String.valueOf(port == 587 || port == 465));   // Enable STARTTLS if using a TLS port (587 or 465).
        // 10s connect / read timeout so a hung SMTP doesn't stall the async pool.
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        return sender;
    }
}