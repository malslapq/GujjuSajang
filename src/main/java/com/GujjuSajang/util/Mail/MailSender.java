package com.GujjuSajang.util.Mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
@RequiredArgsConstructor
public class MailSender {

    private final JavaMailSender javaMailSender;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    @Value("${spring.mail.subject}")
    private String subject;
    @Value("${spring.mail.content}")
    private String content;
    @Value("${spring.mail.codeLength}")
    private int codeLength;

    public String sendVerifiedMail(long id, String mail) throws MessagingException {
        String code = getVerifiedCode(codeLength);
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(mail);
        helper.setSubject(subject);
        helper.setText(content + id + "&code=" + code + "\">클릭</a>", true);
        javaMailSender.send(message);
        return code;
    }


    private String getVerifiedCode(int codeLength) {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder(codeLength);
        for (int i = 0; i < codeLength; i++) {
            code.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return code.toString();
    }
}
