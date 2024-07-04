package com.GujjuSajang.member.repository;

import com.GujjuSajang.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByMail(String mail);
    boolean existsById(Long id);
}
