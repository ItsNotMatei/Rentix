package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    public EmailService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSenderProvider = mailSenderProvider;
    }

    @Value("${rentix.mail.from:rentix@oficial.com}")
    private String fromAddress;

    @Value("${rentix.mail.enabled:false}")
    private boolean mailEnabled;

    public void sendTwoFactorCode(String toEmail, String code) {
        String subject = "Codul tău de verificare Rentix";
        String body = """
                Salut,

                Codul tău de autentificare Rentix este: %s

                Codul expiră în 10 minute. Dacă nu ai încercat să te conectezi, ignoră acest email.

                — Echipa Rentix
                rentix@oficial.com
                """.formatted(code);
        send(toEmail, subject, body);
    }

    public void sendPasswordResetLink(String toEmail, String resetUrl) {
        String subject = "Resetare parolă Rentix";
        String body = """
                Salut,

                Ai solicitat resetarea parolei. Apasă linkul de mai jos pentru a seta o parolă nouă (valabil 1 oră):

                %s

                Dacă nu ai solicitat resetarea, ignoră acest email.

                — Echipa Rentix
                rentix@oficial.com
                """.formatted(resetUrl);
        send(toEmail, subject, body);
    }

    private void send(String to, String subject, String body) {
        if (!mailEnabled) {
            log.info("[Rentix Email - dev] To: {} | Subject: {} | Body:\n{}", to, subject, body);
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null) {
            log.warn("[Rentix Email] JavaMailSender indisponibil — mesajul nu a fost trimis.");
            return;
        }
        sender.send(message);
    }
}
