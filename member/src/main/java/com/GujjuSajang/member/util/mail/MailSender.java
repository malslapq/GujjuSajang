package com.GujjuSajang.member.util.mail;

import com.GujjuSajang.core.exception.ErrorCode;
import com.GujjuSajang.core.exception.MemberException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
@RequiredArgsConstructor
public class MailSender {

    private final JavaMailSender javaMailSender;
    @Value("${spring.mail.randomCode}")
    private String CHARACTERS;
    @Value("${spring.mail.subject}")
    private String subject;
    @Value("${spring.mail.content}")
    private String content;
    @Value("${spring.mail.codeLength}")
    private int codeLength;

    public String sendVerifiedMail(Long id, String mail) {
        String code = getVerifiedCode(codeLength);
        String completeLink = content + id + "&code=" + code + "\">클릭 안하면 지상렬</a>";
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(mail);
            helper.setSubject(subject);
            helper.setText(completeLink, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new MemberException(ErrorCode.FAIL_SEND_MAIL, e);
        }
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
