package com.ssafy.yamyam.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ssafy.yamyam.model.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // 로그인 시 이메일로 조회
    Optional<Member> findByEmail(String email);

    // 회원가입 시 이메일 중복 체크
    boolean existsByEmail(String email);
}