package com.example.querydsl;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.querydsl.dto.MemberDto;
import com.example.querydsl.dto.UserDto;
import com.example.querydsl.entity.Member;
import com.example.querydsl.entity.QMember;
import com.example.querydsl.entity.QTeam;
import com.example.querydsl.entity.Team;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import static com.example.querydsl.entity.QMember.*;
import static com.example.querydsl.entity.QTeam.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

@SpringBootTest
@Transactional
public class QuerydslBasicTest4 {

	@Autowired
	EntityManager em;

	JPAQueryFactory queryFactory;

	@BeforeEach
	public void before() {
		queryFactory = new JPAQueryFactory(em);
		Team teamA = new Team("teamA");
		Team teamB = new Team("teamB");
		em.persist(teamA);
		em.persist(teamB);

		Member member1 = new Member("member1", 10, teamA);
		Member member2 = new Member("member2", 20, teamB);
		Member member3 = new Member("member3", 30, teamA);
		Member member4 = new Member("member4", 40, teamB);

		em.persist(member1);
		em.persist(member2);
		em.persist(member3);
		em.persist(member4);

	}

	@Test
	public void simpleProjection() {
		// projection 대상이 1개 일 경우
		List<String> result = queryFactory.select(member.username).from(member).fetch();

		for (String s : result) {
			System.out.println(s);
		}
	}

	@Test
	public void tupleProjection() {
		List<Tuple> result = queryFactory.select(member.username, member.age).from(member).fetch();
		for (Tuple tuple : result) {
			String username = tuple.get(member.username);
			Integer age = tuple.get(member.age);
			System.out.println("username :" + username);
			System.out.println("age:" + age);
		}
	}

	/*
	 * dto 조회 시 new 명령어 사용 필수 생성자 방식만 지원
	 */
	@Test
	public void findDtoByJPQL() {
		List<MemberDto> result = em
				.createQuery("select new com.example.querydsl.dto.MemberDto(m.username,m.age) from Member m",
						MemberDto.class)
				.getResultList();
		for (MemberDto memberDto : result) {
			System.out.println("memberDto = " + memberDto);
		}
	}

	// setter 접근방법
	@Test
	public void findDtoBySetter() {
		List<MemberDto> result = queryFactory.select(Projections.bean(MemberDto.class, member.username, member.age))
				.from(member).fetch();
		for (MemberDto memberDto : result) {
			System.out.println("memberDto :" + memberDto);
		}
	}

	// field 접근방법
	@Test
	public void findDtoByField() {
		List<MemberDto> result = queryFactory.select(Projections.fields(MemberDto.class, member.username, member.age))
				.from(member).fetch();
		for (MemberDto memberDto : result) {
			System.out.println("memberDto :" + memberDto);
		}
	}

	// field 접근방법 -> property의 이름이 다른 경우 alias 주는 방법
	@Test
	public void findUserDtoByField() {
		
		QMember memberSub = new QMember("memberSub");
		
		List<UserDto> result = queryFactory.select(Projections.fields(UserDto.class, 
				member.username.as("name"), 
				// memberSub.age.max()).from(memberSub), "age" -> subquery에 alias 주기
				ExpressionUtils.as(JPAExpressions.select(memberSub.age.max()).from(memberSub), "age")))
				.from(member).fetch();
		for (UserDto userDto : result) {
			System.out.println("memberDto :" + userDto);
		}
	}

	// 생성자 접근방법
	@Test
	public void findDtoByConstructor() {
		List<MemberDto> result = queryFactory
				.select(Projections.constructor(MemberDto.class, member.username, member.age)).from(member).fetch();
		for (MemberDto memberDto : result) {
			System.out.println("memberDto :" + memberDto);
		}
	}
}
