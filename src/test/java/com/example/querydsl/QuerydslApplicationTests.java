package com.example.querydsl;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

import com.example.querydsl.entity.Hello;
import com.example.querydsl.entity.QHello;
import com.querydsl.jpa.impl.JPAQueryFactory;

@SpringBootTest
@Transactional
@Commit
class QuerydslApplicationTests {
	
	@PersistenceContext
	EntityManager em;

	@Test
	void contextLoads() {
		Hello hello = new Hello();
		em.persist(hello);
		
		JPAQueryFactory query = new JPAQueryFactory(em);
		QHello qHello = new QHello("h");
		
		/* Query 관련 entity는 QClass를 parameter로 사용*/
		Hello result = query.selectFrom(qHello).fetchOne();
		assertThat(result).isEqualTo(hello);
		
		assertThat(result.getId()).isEqualTo(hello.getId());
	}

}
