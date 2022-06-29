package com.example.querydsl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import com.example.querydsl.dto.MemberSearchCondition;
import com.example.querydsl.dto.MemberTeamDto;
import com.example.querydsl.entity.Member;
import com.example.querydsl.entity.Team;
import com.example.querydsl.repository.MemberRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;

@SpringBootTest
@Transactional
public class MemberRepositoryTest {

	@Autowired
	EntityManager em;

	@Autowired
	MemberRepository memberRepository;
	

	@Test
	public void basicTest() {
		Member member = new Member("member1", 10);
		memberRepository.save(member);

		Member findMember = memberRepository.findById(member.getId()).get();

		assertThat(findMember).isEqualTo(member);

		List<Member> result1 = memberRepository.findAll();

		assertThat(result1).containsExactly(member);

		List<Member> result2 = memberRepository.findByUsername("member1");
		assertThat(result2).containsExactly(member);
	}

	@Test
	public void basicQueryTest() {
		Member member = new Member("member1", 10);
		memberRepository.save(member);

		Member findMember = memberRepository.findById(member.getId()).get();

		assertThat(findMember).isEqualTo(member);

		List<Member> result1 = memberRepository.findAll();

		assertThat(result1).containsExactly(member);

		List<Member> result2 = memberRepository.findByUsername("member1");
		assertThat(result2).containsExactly(member);
	}
	
	@Test
	public void searchCustomTest() {

		MemberSearchCondition condition = new MemberSearchCondition();
		condition.setAgeGoe(35);
		condition.setAgeLoe(40);
		condition.setTeamName("teamB");

		List<MemberTeamDto> result = memberRepository.search(condition);

		assertThat(result).extracting("username").containsExactly("member4");
	}
	
	@Test
	public void searchPageSimpleTest() {

		MemberSearchCondition condition = new MemberSearchCondition();
		PageRequest pageRequest =PageRequest.of(0, 3);
		
		Page<MemberTeamDto> result =memberRepository.searchPageSimple(condition, pageRequest);

		assertThat(result.getSize()).isEqualTo(3);
		assertThat(result.getContent()).extracting("username").containsExactly("member1");
		
	}
	
}
