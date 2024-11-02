package com.sharespace.sharespace_server.global.enums;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Category {
	SMALL(0),
	MEDIUM(1),
	LARGE(2);

	final Integer value;
}
