package com.sharespace.sharespace_server.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Role {
    ROLE_HOST("HOST"),
    ROLE_GUEST("GUEST");

    private String value;

    public static Role fromValue(String value) {
        for (Role role : Role.values()) {
            if (role.getValue().equals(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + value);
    }
}
