package kopo.newproject.security;

import kopo.newproject.repository.entity.jpa.UserInfoEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

public class CustomUserDetails implements UserDetails {

    private final UserInfoEntity user; // UserInfoEntity 필드 추가

    public CustomUserDetails(UserInfoEntity user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return new ArrayList<>(); // 권한은 일단 비워둠
    }

    @Override
    public String getPassword() {
        return user.getPassword(); // DB에 저장된 암호화된 비밀번호
    }

    @Override
    public String getUsername() {
        return user.getUserId(); // 로그인에 사용할 사용자 ID
    }

    // UserInfoEntity의 getName() 메서드를 사용하도록 수정
    public String getName() {
        return user.getName();
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


