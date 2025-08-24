package kopo.newproject.security;

import kopo.newproject.repository.entity.jpa.UserInfoEntity;
import lombok.Getter; // Getter 어노테이션 추가
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Spring Security의 사용자 인증 및 권한 부여를 위한 {@link UserDetails} 인터페이스 구현체.
 * <p>
 * 데이터베이스에서 조회한 {@link UserInfoEntity} 객체를 Spring Security가 요구하는
 * {@code UserDetails} 형태로 변환하여 제공합니다.
 */
public class CustomUserDetails implements UserDetails {

    /**
     * 데이터베이스에서 조회된 실제 사용자 정보를 담고 있는 엔티티.
     */
    private final UserInfoEntity user;

    /**
     * {@code CustomUserDetails}의 생성자.
     *
     * @param user 사용자 정보를 담은 {@link UserInfoEntity} 객체
     */
    public CustomUserDetails(UserInfoEntity user) {
        this.user = user;
    }

    /**
     * 사용자에게 부여된 권한(Role) 목록을 반환합니다.
     * <p>
     * 현재는 별도의 권한(Role) 관리가 구현되어 있지 않으므로 빈 컬렉션을 반환합니다.
     * (예: "ROLE_USER", "ROLE_ADMIN" 등)
     *
     * @return 사용자에게 부여된 권한 목록
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return new ArrayList<>(); // 현재는 권한이 없으므로 빈 리스트 반환
    }

    /**
     * 사용자의 비밀번호를 반환합니다.
     * <p>
     * 데이터베이스에 저장된 암호화된 비밀번호를 반환합니다.
     *
     * @return 사용자의 비밀번호
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * 사용자의 아이디를 반환합니다.
     * <p>
     * Spring Security에서 사용자를 식별하는 고유한 이름(username)으로 사용됩니다.
     *
     * @return 사용자의 아이디
     */
    @Override
    public String getUsername() {
        return user.getUserId();
    }

    /**
     * 사용자 엔티티의 이름을 반환합니다.
     * <p>
     * {@code UserDetails} 인터페이스의 기본 메소드는 아니지만, 편의를 위해 추가되었습니다.
     *
     * @return 사용자의 이름
     */
    public String getName() {
        return user.getName();
    }

    /**
     * 계정의 만료 여부를 반환합니다.
     * <p>
     * 현재는 계정 만료 로직이 구현되어 있지 않으므로 항상 {@code true}를 반환합니다.
     *
     * @return 계정이 만료되지 않았으면 {@code true}
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 계정의 잠금 여부를 반환합니다.
     * <p>
     * 현재는 계정 잠금 로직이 구현되어 있지 않으므로 항상 {@code true}를 반환합니다.
     *
     * @return 계정이 잠겨있지 않으면 {@code true}
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 비밀번호의 만료 여부를 반환합니다.
     * <p>
     * 현재는 비밀번호 만료 로직이 구현되어 있지 않으므로 항상 {@code true}를 반환합니다.
     *
     * @return 비밀번호가 만료되지 않았으면 {@code true}
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 계정의 활성화 여부를 반환합니다.
     * <p>
     * 현재는 계정 활성화/비활성화 로직이 구현되어 있지 않으므로 항상 {@code true}를 반환합니다.
     *
     * @return 계정이 활성화되어 있으면 {@code true}
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}