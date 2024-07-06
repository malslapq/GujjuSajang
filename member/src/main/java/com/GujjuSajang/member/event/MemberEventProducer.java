package com.GujjuSajang.member.event;

import com.GujjuSajang.core.dto.CreateMemberEventDto;
import com.GujjuSajang.member.util.EventProducer;
import com.GujjuSajang.member.dto.MemberSignUpDto;
import com.GujjuSajang.member.entity.Member;
import com.GujjuSajang.member.repository.MemberRepository;
import com.GujjuSajang.member.util.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class MemberEventProducer {

    private final PasswordEncoder passwordEncoder;
    private final EventProducer eventProducer;
    private final MemberRepository memberRepository;
    @Value("${spring.mail.randomCode}")
    private String CHARACTERS;
    @Value("${spring.mail.codeLength}")
    private int codeLength;

    // 회원 가입
    @Transactional
    public MemberSignUpDto signUp(MemberSignUpDto memberSignUpDto) {

        String encodedPassword = passwordEncoder.encode(memberSignUpDto.getPassword());
        Member member;

        member = memberRepository.save(Member.from(memberSignUpDto, encodedPassword));

        eventProducer.sendEvent("create-member",
                CreateMemberEventDto.builder()
                        .id(member.getId())
                        .mail(member.getMail())
                        .code(getVerifiedCode(codeLength))
                        .build()
        );

        return MemberSignUpDto.from(member);
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
