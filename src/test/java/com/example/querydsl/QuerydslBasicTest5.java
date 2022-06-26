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
import com.example.querydsl.dto.QMemberDto;
import com.example.querydsl.dto.UserDto;
import com.example.querydsl.entity.Member;
import com.example.querydsl.entity.QMember;
import com.example.querydsl.entity.QTeam;
import com.example.querydsl.entity.Team;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
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
public class QuerydslBasicTest5 {

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

	/**
	 *
	 * boolean builder를 활용해 dynamic query 실행 mybatis 에 <if test=""> 와 동일
	 */
	@Test
	public void dynamicQueryBooleanBuilder() {
		String userNameParam1 = "member1";
		Integer ageParam = null;

		List<Member> result = searchMember1(userNameParam1, ageParam);
		assertThat(result.size()).isEqualTo(1);
	}

	private List<Member> searchMember1(String usernameCond, Integer ageCond) {

		BooleanBuilder builder = new BooleanBuilder();
		if (usernameCond != null) {
			builder.and(member.username.eq(usernameCond));
		}
		if (ageCond != null) {
			builder.and(member.age.eq(ageCond));
		}

		return queryFactory.selectFrom(member).where(builder).fetch();
	}

	/*
	 * where 다중 파라미터 사용해 dynamic query 실행
	 * null 조건은 무시됨 
	 * 메소드를 다른 쿼리에서도 재활용 가능 
	 * 쿼리 가독성이 높아짐
	 */
	@Test
	public void dynamicQueryWhereParam() {
		String userNameParam1 = "member1";
		Integer ageParam = null;

		List<Member> result = searchMember2(userNameParam1, ageParam);
		assertThat(result.size()).isEqualTo(1);
	}

	private List<Member> searchMember2(String usernameCond, Integer ageCond) {

			
		return queryFactory.selectFrom(member).
				where(usernameEq(usernameCond), ageEq(ageCond))
				//where(allEq(usernameCond, ageCond))
				.fetch();
	}

	private BooleanExpression ageEq(Integer ageCond) {
		if ( ageCond == null) {
			return null;
		}
		return member.age.eq(ageCond);
		
	}

	private BooleanExpression usernameEq(String usernameCond) {
		return usernameCond != null ? member.username.eq(usernameCond) : null;
	}
	
	private BooleanExpression allEq(String usernameCond, Integer ageCond) {
		return usernameEq(usernameCond).and(ageEq(ageCond));
	}
	
	/**
	 * bulk update
	 * */
	@Test
	public void bulkUpdate() {
		
		queryFactory.update(member).set(member.username, "비회원").where(member.age.lt(28)).execute();
		/*
		 * 영속성 컨텍스트와 DB 간의 상이함 발생 -> 영속성 컨텍스트가 우선적으로 반영되도록 전략이 되어있음
		 * 영속성 컨텍스트와 DB의 데이터 일치를 위해 영속성 컨텍스트는 초기화
		 * 초기화할 경우 영속성 컨텍스트에 값이 없으므로 DB에서 값을 가져와 영속성 컨텍스트에 세팅
		 * */
		em.flush();
		em.clear();
		
		List<Member> result = queryFactory.select(member).from(member).fetch();
		
		for (Member m:result) {
			System.out.println(m);
		}
		
	}
	
	@Test
	public void bulkAdd() {
		//long count = queryFactory.update(member).set(member.age, member.age.add(1)).execute(); - 덧셈
		
		long count = queryFactory.update(member).set(member.age, member.age.multiply(1)).execute(); // 곱셈
	}
	
	@Test
	public void bulkDelete() {
		long count = queryFactory.delete(member).where(member.age.gt(18)).execute();
	}
	
}

