package com.treino.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "tbl_usuario")
@SQLDelete(sql = "UPDATE tbl_usuario SET estado = 'INACTIVO' WHERE user_id = ?")
@SQLRestriction("estado = 'ACTIVO'")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(length = 30)
    private String telefono;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Rol rol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Estado estado = Estado.ACTIVO;

    public enum Rol { ADMINISTRADOR, PROFESOR, CLIENTE }
    public enum Estado { ACTIVO, INACTIVO }

    public Usuario(Long userId) {
        this.userId = userId;
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return password;
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return email;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }
}
