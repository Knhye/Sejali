package nahye.demo.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import nahye.demo.config.JwtTokenProvider;
import nahye.demo.dto.room.RoomResponse;
import nahye.demo.dto.user.*;
import nahye.demo.entity.RefreshToken;
import nahye.demo.entity.User;
import nahye.demo.enums.AuthLevel;
import nahye.demo.repository.RefreshTokenRepository;
import nahye.demo.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;

    public UserResponse signup(SignRequest request) {
        if (userRepository.findByUserId(request.getUserId()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 사용자명입니다.");
        }

        User user = User.builder()
                .studentNum(request.getStudentNum())
                .username(request.getUsername())
                .userId(request.getUserId())
                .password(passwordEncoder.encode(request.getPassword()))
//                .authLevel(AuthLevel.USER)
                .authLevel(request.getAuthLevel())
                .build();

        User saved = userRepository.save(user);

        return new UserResponse(
                saved.getId(),
                saved.getStudentNum(),
                saved.getUsername()
        );
    }

    @Transactional
    public TokenResponse login(LoginRequest request){
        if (userRepository.findByUserId(request.getUserId()).isEmpty()) {
            throw new IllegalArgumentException("사용자가 없습니다.");
        }

        if (isUserLoggedIn(request.getUserId())) {
            throw new IllegalStateException("이미 로그인된 사용자입니다.");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUserId(), request.getPassword())
        );


        String accessToken = jwtTokenProvider.createAccessToken(authentication.getName());
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication.getName());

        refreshTokenRepository.deleteByUserId(request.getUserId());
        refreshTokenRepository.save(RefreshToken.builder()
                .token(refreshToken)
                .userId(request.getUserId())
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build());

        return new TokenResponse(accessToken, refreshToken);
    }

    public TokenResponse getRefresh(RefreshRequest request){
        RefreshToken token =refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(()-> new RuntimeException("유효하지 않은 리프레시 토큰입니다."));

        if(token.getExpiryDate().isBefore(LocalDateTime.now())){
            refreshTokenRepository.delete(token);
            throw new RuntimeException("리프레시 토큰이 만료되었습니다.");
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(token.getUserId());
        //AT, RT 반환
        return new TokenResponse(newAccessToken, token.getToken());
    }

    public void logout(String accessToken, String refreshToken){

        try {
            jwtTokenProvider.invalidateToken(accessToken);
        } catch (Exception e) {
            throw new IllegalArgumentException("Access Token 무효화 중 오류 발생: " + e.getMessage());
        }

        // 3. Refresh Token을 저장소에서 삭제
        // 사용자 ID 또는 기타 고유 식별자를 키로 사용하여 Refresh Token을 관리한다고 가정합니다.
        // 실제 구현에서는 userId 등을 이용하여 해당 사용자의 Refresh Token을 정확히 찾아 삭제해야 합니다.
        boolean deleted = jwtTokenProvider.deleteRefreshToken(refreshToken);
        if (!deleted) {
            // Refresh Token이 존재하지 않거나 이미 삭제된 경우
            throw new IllegalArgumentException("유효하지 않거나 이미 만료/삭제된 Refresh Token입니다.");
        }
    }

    private boolean isUserLoggedIn(String userId) {
        // Security Context 확인 => 사용자가 이미 로그인 되어있는지 확인
         Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
         return authentication != null && authentication.getName().equals(userId) && authentication.isAuthenticated();
    }

    @Transactional
    public UserProfileResponse updateUser(String accessToken, SignRequest request) {
        // 1. 액세스 토큰을 통해 로그인된 유저 확인
        String userId = jwtTokenProvider.getUsername(accessToken); // 토큰에서 userId 추출
        User existingUser = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("로그인된 사용자를 찾을 수 없습니다."));

        // 2. 필수 입력값 검증
        if (String.valueOf(request.getStudentNum()).length() != 5 ||
                isNullOrEmpty(request.getUsername()) ||
                isNullOrEmpty(request.getPassword())) {
            throw new IllegalArgumentException("필수 요청 값이 누락되었습니다. (학번, 사용자 이름, 아이디, 비밀번호)");
        }

        // 3. 학번 중복 체크 (현재 유저가 아닌 다른 유저가 동일한 학번을 사용하는 경우)
        userRepository.findByStudentNum(request.getStudentNum())
                .filter(user -> !user.getId().equals(existingUser.getId()))
                .ifPresent(user -> {
                    throw new IllegalArgumentException("입력하신 학번은 이미 다른 사용자가 사용 중입니다.");
                });

        // 아이디 중복 체크 (현재 유저가 아닌 다른 유저가 동일한 userId를 사용하는 경우)
        //userRepository.findByUserId(request.getUserId())
          //      .filter(user -> !user.getId().equals(existingUser.getId()))
            //    .ifPresent(user -> {
              //      throw new IllegalArgumentException("입력하신 아이디는 이미 다른 사용자가 사용 중입니다.");
                //});

        // 4. 유저 정보 수정
        existingUser.setStudentNum(request.getStudentNum());
        existingUser.setUsername(request.getUsername());
        existingUser.setPassword(passwordEncoder.encode(request.getPassword()));

        // 5. 저장 및 응답 반환
        User updatedUser = userRepository.save(existingUser);
        return new UserProfileResponse(
                updatedUser.getStudentNum(),
                updatedUser.getUsername(),
                updatedUser.getUserId()
        );
    }

    // 유틸 메서드
    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public List<AdminUserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(room -> new AdminUserResponse(
                        room.getStudentNum(),
                        room.getUsername(),
                        room.getUserId()
                ))
                .collect(Collectors.toList());
    }
}
