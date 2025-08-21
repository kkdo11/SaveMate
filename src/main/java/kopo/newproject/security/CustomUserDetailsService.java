package kopo.newproject.security;

import kopo.newproject.repository.entity.jpa.UserInfoEntity;
import kopo.newproject.repository.jpa.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserInfoRepository userInfoRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        Optional<UserInfoEntity> userOptional = userInfoRepository.findByUserId(userId);
        UserInfoEntity user = userOptional.orElseThrow(() -> new UsernameNotFoundException("User not found with userId: " + userId));
        return new CustomUserDetails(user);
    }
}

