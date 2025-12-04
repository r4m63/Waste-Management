//package ru.itmo.wastemanagement.config.security.mapper;
//
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//import ru.itmo.wastemanagement.entity.User;
//
//import java.util.Collection;
//import java.util.List;
//
//public class SecurityUser implements UserDetails {
//
//    private final User user;
//
//    public SecurityUser(User user) {
//        this.user = user;
//    }
//
//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        // ROLE_ADMIN, ROLE_RESIDENT и т.п.
//        String roleName = user.getRole().name().toUpperCase(); // admin -> ADMIN
//        return List.of(new SimpleGrantedAuthority("ROLE_" + roleName));
//    }
//
//    @Override
//    public String getPassword() {
//        return user.getPasswordHash(); // тут должен быть hash, а не в лоб
//    }
//
//    @Override
//    public String getUsername() {
//        // по чему логинишься — phone/email/username
//        return user.getPhone();
//    }
//
//    @Override
//    public boolean isAccountNonExpired() {
//        return true;
//    }
//
//    @Override
//    public boolean isAccountNonLocked() {
//        return true;
//    }
//
//    @Override
//    public boolean isCredentialsNonExpired() {
//        return true;
//    }
//
//    @Override
//    public boolean isEnabled() {
//        return user.isActive();
//    }
//
//    public User getUser() {
//        return user;
//    }
//}
