package org.example.what_seoul.domain.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String nickName;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime deletedAt;

    public User(String userId, String password, String email, String nickName, LocalDateTime createdAt) {
        this.userId = userId;
        this.password = password;
        this.email = email;
        this.nickName = nickName;
        this.createdAt = createdAt;
    }

    public User(String userId, String password, String email, String nickName) {
        this.userId = userId;
        this.password = password;
        this.email = email;
        this.nickName = nickName;
        this.createdAt = LocalDateTime.now();
    }

    public void changeUserInfo(String password, String email, String nickName) {
        if (password != null) {
            this.password = password;
        }
        if (email != null) {
            this.email = email;
        }
        if (nickName != null) {
            this.nickName = nickName;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.deletedAt = LocalDateTime.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("user"));
    }

    @Override
    public String getUsername() {
        return userId;
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
