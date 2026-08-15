package helen.com.authservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Sends transactional emails (password reset, MFA notices, etc).
 *
 * SMTP is disabled by default in dev (app.mail.enabled=false), in which case
 * the email content is only logged instead of actually being sent - useful
 * for local development without a real mail server configured.
 */
@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.mail.from:no-reply@auth-service.local}")
    private String from;

    public EmailService(org.springframework.beans.factory.ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSender = mailSenderProvider.getIfAvailable();
    }

    public void sendPasswordResetEmail(String to, String resetLink) {
        String subject = "Redefinição de senha";
        String body = "Recebemos uma solicitação para redefinir sua senha.\n\n"
                + "Clique no link abaixo para criar uma nova senha (válido por 1 hora):\n"
                + resetLink
                + "\n\nSe você não solicitou isso, ignore este email.";
        send(to, subject, body);
    }

    public void sendMfaEnabledEmail(String to) {
        send(to, "MFA ativado",
                "A autenticação multifator foi ativada na sua conta. "
                        + "Se você não fez essa alteração, entre em contato com o suporte imediatamente.");
    }

    private void send(String to, String subject, String body) {
        if (!mailEnabled || mailSender == null) {
            log.info("[EmailService] (mail desabilitado - apenas log) to={}, subject={}, body={}",
                    to, subject, body);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        try {
            mailSender.send(message);
        } catch (Exception ex) {
            log.error("Falha ao enviar email para {}: {}", to, ex.getMessage());
        }
    }
}
