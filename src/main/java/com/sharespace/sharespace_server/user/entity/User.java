package com.sharespace.sharespace_server.user.entity;

import com.sharespace.sharespace_server.global.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user", schema = "sharespace")
@AllArgsConstructor
@Builder
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nick_name", nullable = false, length = 50)
    private String nickName;

    @Column(name = "email", nullable = false, length = 50, unique = true)
    private String email;

    @Column(name = "image", columnDefinition = "TEXT")
    private String image;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 10)
    private Role role;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "lock_time")
    private LocalDateTime lockTime;

    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts = 0;

    @Column(name = "location", nullable = false, length = 50)
    private String location;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "email_validated", nullable = false)
    private Boolean emailValidated = false;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(this.role.name()));
    }

    @Override
    public String getUsername() {
        return this.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
