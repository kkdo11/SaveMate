package kopo.newproject.security;



import kopo.newproject.repository.jpa.UserInfoRepository;
import kopo.newproject.repository.entity.jpa.UserInfoEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserInfoRepository userInfoRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 사용자 ID로 사용자 정보를 조회
        UserInfoEntity user = userInfoRepository.findByUserId(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        // 조회된 사용자 정보를 기반으로 UserDetails 반환
        return new CustomUserDetails(user);
    }
}
