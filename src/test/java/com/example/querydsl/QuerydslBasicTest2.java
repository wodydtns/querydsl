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
import com.example.querydsl.entity.QTeam;
import com.example.querydsl.entity.Team;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import static com.example.querydsl.entity.QMember.*;
import static com.example.querydsl.entity.QTeam.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

@SpringBootTest
@Transactional
public class QuerydslBasicTest2 {
	
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
	public void paging1() {
		List<Member> result = queryFactory.selectFrom(member).orderBy(member.username.desc()).offset(1).limit(2).fetch();
		
		assertThat(result.size()).isEqualTo(2);
	}
	
	@Test
	public void aggregation() {
		List<Tuple> result = queryFactory.select(member.count(), member.age.sum(),member.age.avg(),member.age.max(),member.age.min()).from(member).fetch();
		
		Tuple tuple = result.get(0);
		assertThat(tuple.get(member.count())).isEqualTo(4);
		assertThat(tuple.get(member.age.sum())).isEqualTo(100);
		assertThat(tuple.get(member.age.avg())).isEqualTo(25);
		assertThat(tuple.get(member.age.min())).isEqualTo(10);
	}
	
	/*
	 * 팀의 이름과 각 팀의 평균 연령 구하기
	 * */
	/*
	@Test
	public void groupBy() {
		List<Tuple> result = queryFactory.select(QTeam.team.name,member.age.avg()).from(member).join(member.team, team).groupBy(team.name).fetch();
		
		Tuple teamA = result.get(0);
		Tuple teamB = result.get(1);
		
		assertThat(teamA.get(team.name)).isEqualTo("teamA");
		assertThat(teamA.get(member.age.avg())).isEqualTo(20);
		
		assertThat(teamB.get(team.name)).isEqualTo("teamB");
		assertThat(teamB.get(member.age.avg())).isEqualTo(35);
	}
	*/
	@Test
	public void Join() {
		//inner join
		//List<Member> result =queryFactory.selectFrom(member).join(member.team,QTeam.team).where(team.name.eq("teamA")).fetch();
		// left outer join
		List<Member> result =queryFactory.selectFrom(member).leftJoin(member.team,QTeam.team).where(team.name.eq("teamA")).fetch();
		
		assertThat(result).extracting("username").containsExactly("member1","member3");
	}
	/*
	 * 세타 조인
	 * 회원의 이름이 팀 이름과 같은 회원 조회
	 * */
	@Test
	public void thetaJoin() {
		em.persist(new Member("teamA"));
		em.persist(new Member("teamB"));
		em.persist(new Member("teamC"));
		//theta join의 경우 outer join 불가능
		//hibernate 최신 버전의 경우 outer join 가능 - 연관관계 없는 테이블의 경우
		List<Member> result = queryFactory.select(member).from(member,team).where(member.username.eq(team.name)).fetch();
		
		assertThat(result).extracting("username").containsExactly("teamA","teamB");
	}
	/*
	 * 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
	 * */
	@Test
	public void joinOnFiltering() {
		List<Tuple> result = queryFactory.select(member,team).from(member).leftJoin(member.team,team).on(team.name.eq("teamA")).fetch();
		
		for (Tuple tuple : result) {
			System.out.println(tuple);
		}
	}
	/*
	 * 연관관계 없는 엔티티 외부 조인
	 * 회원이 이름이 팀 이름과 같은 대상 외부 조인
	 * */
	@Test
	public void joinOnNoRelation() {
		em.persist(new Member("teamA"));
		em.persist(new Member("teamB"));
		em.persist(new Member("teamC"));
		// team
		List<Tuple> result = queryFactory.select(member,team).from(member).leftJoin(team).on(member.username.eq(team.name)).fetch();
		
		assertThat(result).extracting("username").containsExactly("teamA","teamB");
	}
	
	@PersistenceUnit
	EntityManagerFactory emf;
	
	@Test
	public void fetchJoinNo() {
		em.flush();
		em.clear();
		
		Member findMember = queryFactory.selectFrom(member).where(member.username.eq("member1")).fetchOne();
		
		boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
		assertThat(loaded).as("페치 조인 미적용").isFalse();
	}
	
	@Test
	public void fetchJoinUse() {
		em.flush();
		em.clear();
		
		Member findMember = queryFactory.selectFrom(member).join(member.team,team).fetchJoin().where(member.username.eq("member1")).fetchOne();
		
		boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
		assertThat(loaded).as("페치 조인 미적용").isTrue();
	}
	
}
