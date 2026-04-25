package com.alert_engine.service.notification;

import com.alert_engine.config.MailConfig;
import com.alert_engine.model.enums.NotificationChannel;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Delivers alerts via SMTP (configured for Mailtrap in dev).
 * <p>
 * When MailConfig.enabled is false, logs what it WOULD have sent instead of calling
 * SMTP. Useful for iterating without hitting Mailtrap, or if your sandbox is down.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationChannel implements NotificationChannelHandler {

    private final JavaMailSender mailSender;
    private final MailConfig mailConfig;

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm:ss a z", Locale.US)
                    .withZone(ZoneId.systemDefault());

    private volatile String cachedTemplate;

    @Override
    public NotificationChannel getChannelType() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public ChannelResult send(AlertContext context) {
        if (!mailConfig.isEnabled()) {
            log.info("Email disabled (MAIL_ENABLED=false). Would have sent to {}: {} {} {}",
                    context.userEmail(), context.ticker(), context.direction(), context.currentPrice());
            return ChannelResult.ok();
        }

        try {
            String body = renderTemplate(context);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom(mailConfig.getFrom());
            helper.setTo(context.userEmail());
            helper.setSubject(buildSubject(context));
            helper.setText(body, true);   // true = HTML

            mailSender.send(message);
            log.info("Email sent to {} for alert config {}", context.userEmail(), context.configId());
            return ChannelResult.ok();

        } catch (MessagingException | MailException e) {
            log.warn("Email send failed for config {} to {}: {}",
                    context.configId(), context.userEmail(), e.getMessage());
            return ChannelResult.fail("Email error: " + e.getMessage());
        } catch (IOException e) {
            log.error("Could not load email template", e);
            return ChannelResult.fail("Template load error: " + e.getMessage());
        }
    }

    private String buildSubject(AlertContext ctx) {
        return String.format("[Alert] %s crossed %s $%s",
                ctx.ticker(), ctx.direction(), ctx.thresholdPrice());
    }

    private String renderTemplate(AlertContext ctx) throws IOException {
        String template = getOrLoadTemplate();

        String displayName = ctx.userDisplayName() != null ? ctx.userDisplayName() : "trader";

        return template
                .replace("{{TICKER}}", ctx.ticker())
                .replace("{{DISPLAY_NAME}}", displayName)
                .replace("{{DIRECTION}}", ctx.direction())
                .replace("{{CURRENT_PRICE}}", ctx.currentPrice().toPlainString())
                .replace("{{THRESHOLD_PRICE}}", ctx.thresholdPrice().toPlainString())
                .replace("{{TRIGGERED_AT}}", TIMESTAMP_FORMAT.format(ctx.triggeredAt()));
    }

    private String getOrLoadTemplate() throws IOException {
        String local = cachedTemplate;
        if (local == null) {
            synchronized (this) {
                local = cachedTemplate;
                if (local == null) {
                    ClassPathResource resource = new ClassPathResource("templates/alert-email.html");
                    local = StreamUtils.copyToString(
                            resource.getInputStream(), StandardCharsets.UTF_8);
                    cachedTemplate = local;
                }
            }
        }
        return local;
    }
}