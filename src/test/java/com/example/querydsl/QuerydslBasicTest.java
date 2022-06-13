package com.example.querydsl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.querydsl.entity.Member;
import com.example.querydsl.entity.QMember;
import com.example.querydsl.entity.Team;
import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import static com.example.querydsl.entity.QMember.*;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {
	
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
	public void startJPQL() {
		Member findByMemberJPQL =em.createQuery("select m from Member m where m.username = :username",Member.class)
				.setParameter("username", "member1").getSingleResult();
		
		assertThat(findByMemberJPQL.getUsername()).isEqualTo("member1");
	}
	
	@Test
	public void startQuerydsl() {
		//QMember m = new QMember("m");
		//QMember m = QMember.member;
		
		Member findMember = queryFactory.select(member).from(member).where(member.username.eq("member1")).fetchOne();
		
		assertThat(findMember.getUsername()).isEqualTo("member1");
		
	}
	
	@Test
	public void search() {
		Member findMember = queryFactory.selectFrom(member).where(member.username.eq("member1").and(member.age.eq(10))).fetchOne();
		
		assertThat(findMember.getUsername()).isEqualTo("member1");
	}
	
	@Test 
	public void resultFetchTest() {
		List<Member> fetch = queryFactory.selectFrom(member).fetch();
		
		Member fetchOne = queryFactory.selectFrom(QMember.member).fetchOne();
		
		// limit(1).fetch()
		Member fetchFirst = queryFactory.selectFrom(QMember.member).fetchFirst();
		/* querydsl 5.0이상 버전에서 deprecated
		 total count query + select query 두개 실행
		QueryResults<Member> fetchResult = queryFactory.selectFrom(QMember.member).fetchResults();
		 count값 
		fetchResult.getTotal();
		 select 내용
		List<Member> content = fetchResult.getResults();
		*/
		// 대안
		List<Member> fetchResult = queryFactory.selectFrom(QMember.member).fetch();
		
		// querydsl 5.0이상 버전에서 deprecated 
		//queryFactory.selectFrom(QMember.member).fetchCount();
		int totalSize = queryFactory.selectFrom(QMember.member).fetch().size();
		// => 결론 : count query, select 쿼리 별도로 해서 실행 
	}
	/*
	 * 정렬 순서
	 * 1. 회원 나이 desc
	 * 2. 회원 이름 asc
	 * 단, 2에서 회원 이름이 없으면 마지막에 출력
	 * */
	@Test 
	public void sort() {
		em.persist(new Member(null,100));
		em.persist(new Member("member5",100));
		em.persist(new Member("member6",100));
		
		List<Member> result = queryFactory.selectFrom(member).where(member.age.eq(100)).orderBy(member.age.desc(),member.username.asc().nullsLast()).fetch();
		
		Member member5 = result.get(0);
		Member member6 = result.get(1);
		Member memberNull = result.get(2);
		assertThat(member5.getUsername()).isEqualTo("member5");
		assertThat(member6.getUsername()).isEqualTo("member6");
		assertThat(memberNull.getUsername()).isNull();
		
		
	}
}
