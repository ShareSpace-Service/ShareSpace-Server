package com.sharespace.sharespace_server.global.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Category {
	CATEGORY_SMALL("Small"),
	CATEGORY_MEDIUM("Medium"),
	CATEGORY_LARGE("Large");
	private String value;

	public static Category fromValue(String value) {
		for (Category category : Category.values()) {
			if (category.getValue().equals(value)) {
				return category;
			}
		}
		throw new IllegalArgumentException("Unknown Category: " + value);
	}
}
