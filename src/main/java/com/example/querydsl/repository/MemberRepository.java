package com.example.querydsl.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.querydsl.entity.Member;

public interface MemberRepository  extends JpaRepository<Member, Long> , MemberRepositoryCustom{
	
	//select m from Member m where username :=username
	List<Member> findByUsername(String username);

}
