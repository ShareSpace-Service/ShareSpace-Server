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

	public List<Category> getRelatedCategories() {
		return switch (this) {
			case SMALL -> List.of(SMALL, MEDIUM, LARGE);
			case MEDIUM -> List.of(MEDIUM, LARGE);
			case LARGE -> List.of(LARGE);
		};
	}
}
