package com.example.querydsl.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MemberDto {
	private String username;
	private int age;
	
	public MemberDto(String username,int age) {
		this.age =age;
		this.username = username;
	}
}