package com.example.querydsl.repository;

import static com.example.querydsl.entity.QMember.member;
import static com.example.querydsl.entity.QTeam.team;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import com.example.querydsl.dto.MemberSearchCondition;
import com.example.querydsl.dto.MemberTeamDto;
import com.example.querydsl.dto.QMemberTeamDto;
import com.example.querydsl.entity.Member;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

public class MemberRepositoryImpl implements MemberRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	public MemberRepositoryImpl(JPAQueryFactory queryFactory) {
		this.queryFactory = queryFactory;
	}

	@Override
	public List<MemberTeamDto> search(MemberSearchCondition condition) {
		return queryFactory
				.select(new QMemberTeamDto(member.id.as("memberId"), member.username, member.age,
						team.id.as("teamId"), team.name.as("teamName")))
				.from(member).leftJoin(member.team, team)
				.where(usernameEq(condition.getUserName()), teamNameEq(condition.getTeamName()),
						ageGoe(condition.getAgeGoe()), ageLoe(condition.getAgeLoe()))
				.fetch();
	}

	private BooleanExpression usernameEq(String username) {
		return StringUtils.hasText(username) ? null : member.username.eq(username);
	}

	private BooleanExpression teamNameEq(String teamName) {
		return StringUtils.hasText(teamName) ? null : team.name.eq(teamName);
	}

	private BooleanExpression ageGoe(Integer ageGoe) {
		return ageGoe == null ? null : member.age.goe(ageGoe);
	}

	private BooleanExpression ageLoe(Integer ageLoe) {
		return ageLoe == null ? null : member.age.loe(ageLoe);
	}

	@Override
	public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition,
			Pageable pageable) {
		QueryResults<MemberTeamDto> results = queryFactory
				.select(new QMemberTeamDto(member.id, member.username, member.age, team.id,
						team.name))
				.from(member).leftJoin(member.team, team)
				.where(usernameEq(condition.getUserName()), teamNameEq(condition.getTeamName()),
						ageGoe(condition.getAgeGoe()), ageLoe(condition.getAgeLoe()))
				.offset(pageable.getOffset()).limit(pageable.getPageSize()).fetchResults();
		List<MemberTeamDto> content = results.getResults();
		long total = results.getTotal();
		return new PageImpl<>(content, pageable, total);
	}

	@Override
	public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition,
			Pageable pageable) {
		List<MemberTeamDto> content = queryFactory
				.select(new QMemberTeamDto(member.id.as("memberId"), member.username, member.age,
						team.id.as("teamId"), team.name.as("teamName")))
				.from(member).leftJoin(member.team, team)
				.where(usernameEq(condition.getUserName()), teamNameEq(condition.getTeamName()),
						ageGoe(condition.getAgeGoe()), ageLoe(condition.getAgeLoe()))
				.offset(pageable.getOffset()).limit(pageable.getPageSize()).fetch();
		JPAQuery<Member> countQuery = queryFactory.select(member).from(member)
				.leftJoin(member.team, team).where(usernameEq(condition.getUserName()),
						teamNameEq(condition.getTeamName()), ageGoe(condition.getAgeGoe()),
						ageLoe(condition.getAgeLoe()));
		/*
		 * count 최적화 case 1. 페이지 시작이면서 컨텐츠 사이즈가 페이지 사이즈보다 작을 때 ex) content의 total count가
		 * 3 이고 paging size가 5인 경우 case 2. 마지막 페이지 일 때 (offset + 컨텐츠 사이즈를 더해서 전체 사이즈 구함)
		 * ex) 마지막 페이지
		 */
		return PageableExecutionUtils.getPage(content, pageable, () -> countQuery.fetchCount());
		// return new PageImpl<>(content, pageable, total);
	}

}
