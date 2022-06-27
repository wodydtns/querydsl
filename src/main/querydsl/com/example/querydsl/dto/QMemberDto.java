package com.example.querydsl.dto;

import com.querydsl.core.types.dsl.*;

import javax.annotation.Generated;

import com.querydsl.core.types.ConstructorExpression;

/**
 * com.example.querydsl.dto.QMemberDto is a Querydsl Projection type for MemberDto
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QMemberDto extends ConstructorExpression<MemberDto> {

    private static final long serialVersionUID = -857882546L;

    public QMemberDto(com.querydsl.core.types.Expression<String> username, com.querydsl.core.types.Expression<Integer> age) {
        super(MemberDto.class, new Class<?>[]{String.class, int.class}, username, age);
    }

}

