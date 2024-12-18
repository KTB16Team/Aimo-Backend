package aimo.backend.domains.member.model;

import lombok.Getter;

@Getter
public enum MemberRole {

	GUEST("GUEST"),
	USER("USER"),
	ADMIN("ADMIN");

	private final String value;

	MemberRole(String value) {
		this.value = value;
	}
}
