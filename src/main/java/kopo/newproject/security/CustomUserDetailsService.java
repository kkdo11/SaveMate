package kopo.newproject.security;

import kopo.newproject.repository.entity.jpa.UserInfoEntity;
import kopo.newproject.repository.jpa.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Spring Security의 사용자 인증을 위해 사용자 정보를 로드하는 서비스 구현체.
 * <p>
 * {@link UserDetailsService} 인터페이스를 구현하여, Spring Security가 로그인 과정에서
 * 사용자 아이디(여기서는 {@code userId})를 기반으로 사용자 정보를 데이터베이스에서 가져올 수 있도록 합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserInfoRepository userInfoRepository; // 사용자 정보 조회를 위한 레포지토리

    /**
     * {@inheritDoc}
     * <p>
     * Spring Security가 로그인 요청을 처리할 때 호출하는 핵심 메소드.
     * 주어진 사용자 아이디(username)를 사용하여 데이터베이스에서 사용자 정보를 조회하고,
     * {@link UserDetails} 객체로 변환하여 반환합니다.
     *
     * @param userId 로그인 시도하는 사용자의 아이디 (Spring Security에서는 'username'으로 간주)
     * @return 조회된 사용자 정보를 담은 {@link UserDetails} 객체 (여기서는 {@link CustomUserDetails})
     * @throws UsernameNotFoundException 해당 아이디를 가진 사용자를 찾을 수 없을 경우 발생
     */
    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        log.info("▶▶▶ [Security] loadUserByUsername | userId: {}", userId);

        // 1. userInfoRepository를 사용하여 데이터베이스에서 사용자 정보 조회
        Optional<UserInfoEntity> userOptional = userInfoRepository.findByUserId(userId);

        // 2. 사용자 존재 여부 확인 및 예외 처리
        UserInfoEntity user = userOptional.orElseThrow(() -> {
            log.warn("사용자를 찾을 수 없음: userId: {}", userId);
            return new UsernameNotFoundException("User not found with userId: " + userId);
        });

        // 3. 조회된 UserInfoEntity를 Spring Security의 UserDetails 타입인 CustomUserDetails로 변환하여 반환
        log.info("◀◀◀ [Security] loadUserByUsername | 사용자 정보 로드 성공 | userId: {}", userId);
        return new CustomUserDetails(user);
    }
}