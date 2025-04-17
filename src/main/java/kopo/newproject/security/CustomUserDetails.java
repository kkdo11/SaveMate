package kopo.newproject.security;



import kopo.newproject.repository.entity.jpa.UserInfoEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final UserInfoEntity user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 권한 설정이 없다면 빈 리스트 반환
        return Collections.emptyList();
    }

    public String getName() {
        return user.getName(); // UserInfoEntity에 getName() 메서드가 있어야 함
    }

    @Override
    public String getPassword() {
        return user.getPassword(); // DB에 저장된 암호화된 비밀번호
    }

    @Override
    public String getUsername() {
        return user.getUserId(); // 로그인에 사용할 사용자 ID
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 계정 만료 여부
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 계정 잠김 여부
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 비밀번호 만료 여부
    }

    @Override
    public boolean isEnabled() {
        return true; // 계정 활성화 여부
    }
}

