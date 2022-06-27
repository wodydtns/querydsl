package com.example.querydsl.repository;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.example.querydsl.dto.MemberDto;
import com.example.querydsl.dto.MemberSearchCondition;
import com.example.querydsl.dto.MemberTeamDto;
import com.example.querydsl.dto.QMemberTeamDto;
import com.example.querydsl.entity.Member;

import static com.example.querydsl.entity.QMember.*;
import static com.example.querydsl.entity.QTeam.*;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

@Repository
public class MemberJpaRepository {

	/*
	 * entitymanager 는 동시성 문제가 없음 entitymanager는 spring에서 proxy를 주입받아 사용해 tranaction
	 * 단위로 다 다른 곳으로 binding 되도록 routing만 수행 => 동시성 문제 발생 x reference -> ORM 표준 JPA 책
	 * 13.1 트랜잭션 범위의 영속성 컨텍스트
	 */
	private final EntityManager em;
	private final JPAQueryFactory queryFactory;

	/* 생성자 주입을 통한 queryfactory 생성 */
	public MemberJpaRepository(EntityManager em) {
		this.em = em;
		this.queryFactory = new JPAQueryFactory(em);
	}

	/*
	 * 의존성 주입 방법 - Application.java 파일에 JPAQueryFactory를 Bean으로 등록 public
	 * MemberJpaRepository(EntityManager em,JPAQueryFactory queryFactory) { this.em
	 * = em; this.queryFactory = new JPAQueryFactory(em); }
	 */
	public void save(Member member) {
		em.persist(member);

	}

	public List<Member> findAllQuerydsl() {
		return queryFactory.selectFrom(member).fetch();
	}

	public Optional<Member> findById(Long id) {
		Member findMember = em.find(Member.class, id);
		return Optional.ofNullable(findMember);
	}

	public List<Member> findAll() {
		return em.createQuery("select m from Member m", Member.class).getResultList();
	}

	public List<Member> findByUsernameQuerydsl(String username) {
		return queryFactory.selectFrom(member).where(member.username.eq(username)).fetch();
	}

	public List<Member> findByUsername(String username) {
		return em.createQuery("select m from Member m where m.username = :username", Member.class)
				.setParameter("username", username).getResultList();
	}

	public List<MemberTeamDto> searchByBuilder(MemberSearchCondition condition) {

		BooleanBuilder builder = new BooleanBuilder();
		if (StringUtils.hasText(condition.getUserName())) {
			builder.and(member.username.eq(condition.getUserName()));
		}
		if (StringUtils.hasText(condition.getTeamName())) {
			builder.and(team.name.eq(condition.getTeamName()));
		}
		if (condition.getAgeGoe() != null) {
			builder.and(member.age.goe(condition.getAgeGoe()));
		}

		if (condition.getAgeLoe() != null) {
			builder.and(member.age.loe(condition.getAgeLoe()));
		}

		return queryFactory
				.select(new QMemberTeamDto(member.id.as("memberId"), member.username, member.age,
						team.id.as("teamId"), team.name.as("teamName")))
				.from(member).leftJoin(member.team, team).where(builder).fetch();
	}

	public List<MemberTeamDto> search(MemberSearchCondition condition) {
		return queryFactory
				.select(new QMemberTeamDto(member.id.as("memberId"), member.username, member.age,
						team.id.as("teamId"), team.name.as("teamName")))
				.from(member).leftJoin(member.team, team)
				.where(usernameEq(condition.getUserName()), teamNameEq(condition.getTeamName()),
						ageGoe(condition.getAgeGoe()), ageLoe(condition.getAgeLoe()))
				.fetch();
	}

	private BooleanExpression ageGoe(Integer ageGoe) {
		return ageGoe == null ? member.age.goe(ageGoe) : null;
	}

	private BooleanExpression ageLoe(Integer ageLoe) {
		return ageLoe == null ? member.age.goe(ageLoe) : null;
	}

	private BooleanExpression teamNameEq(String teamName) {
		return  StringUtils.hasText(teamName) ? team.name.eq(teamName) : null ;
	}

	private BooleanExpression usernameEq(String userName) {
		return StringUtils.hasText(userName) ? team.name.eq(userName) : null ;
	}

	
}
