package com.example.querydsl;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.querydsl.entity.Member;
import com.example.querydsl.entity.QMember;
import com.example.querydsl.entity.QTeam;
import com.example.querydsl.entity.Team;
import com.querydsl.core.Tuple;
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
public class QuerydslBasicTest3 {
	
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
	 * from 절의 서브쿼리 한계
	 *  - from절의 서브쿼리(인라인 뷰)를 지원하지 않음
	 * 문제의 해결방안
	 *  1. 서브쿼리를 join으로 변경
	 *  2. 어플리케이션에서 쿼리를 2번 분리해서 실행
	 *  3. nativeSQL을 사용
	 * */
	/**
	 * 나이가 가장 많은 회원을 조회
	 */
	// JPAExpressions.select(memberSub.age.max()).from(memberSub) <-- subquery
	@Test
	public void subQuery() {
		
		QMember memberSub = new QMember("memberSub");
		
		List<Member> result =queryFactory.selectFrom(member).where(member.age.eq(JPAExpressions.select(memberSub.age.max()).from(memberSub))).fetch();
		
		assertThat(result).extracting("age").containsExactly(40);
	}
	
	/**
	 * 나이가 평균 이상인 회원 조회
	 */
	// JPAExpressions.select(memberSub.age.max()).from(memberSub) <-- subquery
	@Test
	public void subQueryGoe() {
		
		QMember memberSub = new QMember("memberSub");
		
		List<Member> result =queryFactory.selectFrom(member).where(member.age.goe(JPAExpressions.select(memberSub.age.avg()).from(memberSub))).fetch();
		
		assertThat(result).extracting("age").containsExactly(30,40);
	}
	
	@Test
	public void subQueryIn() {
		
		QMember memberSub = new QMember("memberSub");
		
		List<Member> result =queryFactory.selectFrom(member).where(member.age.goe(JPAExpressions.select(memberSub.age).from(memberSub).where(memberSub.age.gt(10)))).fetch();
		
		assertThat(result).extracting("age").containsExactly(30,40);
	}
	
	@Test
	public void selectSubQuery() {
		
		QMember memberSub = new QMember("memberSub");
		
		List<Tuple> result =queryFactory.select(member.username, JPAExpressions.select(memberSub.age.avg()).from(memberSub)).from(member).fetch();
		
		for (Tuple tuple:result) {
			System.out.println(tuple);
		}
		
	}
	
	@Test
	public void basicCase() {
		List<String> result =queryFactory.select(member.age.when(10).then("열살").when(20).then("스무살").otherwise("기타")).from(member).fetch();
		
		for (String s: result) {
			System.out.println("s=" + s);
		}
	}
	@Test
	public void complexCase() {
		List<String> result = queryFactory.select(new CaseBuilder().when(member.age.between(0,20)).then("0~20age").when(member.age.between(21, 30)).then("21~30살").otherwise("기타")).from(member).fetch();
		for (String s: result) {
			System.out.println("s=" + s);
		}
		
	}
	
	/* 상수 , 문자 더하기*/
	@Test
	public void constant() {
		List<Tuple> result = queryFactory.select(member.username, Expressions.constant("A")).from(member).fetch();
		for (Tuple tuple : result) {
			System.out.println(tuple);
		}
		
	}
	
	@Test
	public void concat() {
		// username(string) + "_" + age(int)
		List<String> result = queryFactory.select(member.username.concat("_").concat(member.age.stringValue())).from(member).where(member.username.eq("member1")).fetch();
		
		for (String s : result) {
			System.out.println("s = " + s);
		}
		
	}
	
}
	