package com.backendspa.security;

import com.backendspa.entity.Cliente;
import com.backendspa.entity.Empleado;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class SpaUserDetails implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final GrantedAuthority authority;
    private final String userType;

    public SpaUserDetails(Cliente cliente, String userType) {
        this.id = cliente.getId();
        this.email = cliente.getEmail();
        this.password = cliente.getPassword();
        this.authority = new SimpleGrantedAuthority("ROLE_CLIENTE");
        this.userType = userType;
    }

    public SpaUserDetails(Empleado empleado, String userType) {
        this.id = empleado.getId();
        this.email = empleado.getEmail();
        this.password = empleado.getPassword();
        this.authority = new SimpleGrantedAuthority("ROLE_" + empleado.getRol().name());
        this.userType = userType;
    }

    public Long getId() {
        return id;
    }

    public String getUserType() {
        return userType;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(authority);
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
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
