package com.example.querydsl.dto;

import com.querydsl.core.types.dsl.*;

import javax.annotation.Generated;

import com.querydsl.core.types.ConstructorExpression;

/**
 * com.example.querydsl.dto.QMemberTeamDto is a Querydsl Projection type for MemberTeamDto
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QMemberTeamDto extends ConstructorExpression<MemberTeamDto> {

    private static final long serialVersionUID = -528401103L;

    public QMemberTeamDto(com.querydsl.core.types.Expression<Long> memberId, com.querydsl.core.types.Expression<String> username, com.querydsl.core.types.Expression<Integer> age, com.querydsl.core.types.Expression<Long> teamId, com.querydsl.core.types.Expression<String> teamName) {
        super(MemberTeamDto.class, new Class<?>[]{long.class, String.class, int.class, long.class, String.class}, memberId, username, age, teamId, teamName);
    }

}

