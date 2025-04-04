package com.example.CeleraAi.users.model;



import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.NaturalId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


@EntityListeners(AuditingEntityListener.class)
@Data
@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
public abstract class User implements UserDetails {

    @Id
    @GeneratedValue(generator = "UUID",strategy = GenerationType.UUID)
    @GenericGenerator(name = "UUID", type = org.hibernate.id.UUIDGenerator.class)
    protected UUID id;

    @NaturalId
    @Column(unique = true, updatable = false)
    protected String email;
    @Column(name = "username")
    protected String username;

    @Column(name = "password")
    protected String password;

    @Column(name = "name")
    protected String name;

    @Column(name = "last_name")
    protected String lastName;

    @Column(name = "phone_number")
    protected String phoneNumber;

    @DateTimeFormat(pattern = "dd-mm-YYYY")
    @Column(name = "birth_date")
    protected LocalDate birthDate;



    @Column(name = "foto_url")
    protected String fotoUrl;

    @Builder.Default
    protected boolean accountNonExpired = true;
    @Builder.Default
    protected boolean accountNonLocked = true;
    @Builder.Default
    protected boolean credentialsNonExpired = true;
    @Builder.Default
    protected boolean enabled = true;

    @ElementCollection(fetch = FetchType.EAGER)
    protected Set<UserRoles> roles;

    @CreatedDate
    protected LocalDateTime createdAt;

    @Builder.Default
    protected LocalDateTime lastPasswordChangeAt = LocalDateTime.now();




    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {return this.email;}

    @Override
    public boolean isAccountNonExpired() {
        return this.credentialsNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}
