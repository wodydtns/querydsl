package com.example.querydsl;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.querydsl.entity.Member;
import com.example.querydsl.entity.Team;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import static com.example.querydsl.entity.QMember.*;

import java.util.List;

@SpringBootTest
@Transactional
public class QuerydslBasicTest7 {

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

	/*
	 * SQL function은 JPA와 같이 Dialect에 등록된 내용만 호출 할 수 있음
	 */
	@Test
	public void sqlFunction() {
		List<String> result = queryFactory
				.select(Expressions.stringTemplate("function('replace',{0},{1},{2})", member.username, "member", "M"))
				.from(member).fetch();
		for (String string : result) {
			System.out.println("string:" + string);
		}

	}
	
	@Test
	public void sqlFunction2() {
		List<String> result = queryFactory.select(member.username)
				.from(member)
				//.where(member.username.eq(Expressions.stringTemplate("function('lower',{0})", member.username)))
				.where(member.username.eq(member.username.lower()))
				.fetch();
		for (String string : result) {
			System.out.println("string:" + string);
		}
	}
	
	
}
