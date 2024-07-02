package com.GujjuSajang.member.util.mail;

import com.GujjuSajang.core.dto.CreateMemberEventDto;
import com.GujjuSajang.core.exception.ErrorCode;
import com.GujjuSajang.core.exception.MemberException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MailSender {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.subject}")
    private String subject;
    @Value("${spring.mail.content}")
    private String content;

    public void sendVerifiedMail(Long id, String mail, String code) {

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
    }

}
