package com.GujjuSajang.member.event;

import com.GujjuSajang.core.dto.CreateMemberEventDto;
import com.GujjuSajang.member.entity.Member;
import com.GujjuSajang.member.util.EventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class MemberEventProducer {

    private final EventProducer eventProducer;
    @Value("${spring.mail.randomCode}")
    private String CHARACTERS;
    @Value("${spring.mail.codeLength}")
    private int codeLength;

    // 회원 생성 이벤트 발행
    public void createMember(Member member) {

        eventProducer.sendEvent("create-member",
                CreateMemberEventDto.builder()
                        .id(member.getId())
                        .mail(member.getMail())
                        .code(getVerifiedCode(codeLength))
                        .build()
        );

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
