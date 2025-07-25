package nahye.demo.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import nahye.demo.dto.user.*;
import nahye.demo.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignRequest request){
        try {
            if (request == null) {
                logger.error("회원가입 실패: request가 null입니다. 요청: {}", request);
                return ResponseEntity.status(400).body("회원가입 실패: 입력 오류");
            }
            UserResponse response = userService.signup(request);
            if (response == null) {
                logger.error("회원가입 실패: UserService.signup() 결과가 null입니다. 요청: {}", request);
                return new ResponseEntity<>("회원가입 실패: 서버 내부 오류", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("회원가입 중 예외 발생", e);
            return new ResponseEntity<>("회원가입 실패: " ,HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request){
        try{
            if (request == null) {
                logger.error("로그인 실패: request가 null입니다. 요청: {}", request);
                return ResponseEntity.status(400).body("로그인 실패: 입력 오류");
            }
            TokenResponse response = userService.login(request);
            if (response == null) {
                logger.error("로그인 실패: UserService.signup() 결과가 null입니다. 요청: {}", request);
                return new ResponseEntity<>("로그인 실패: 서버 내부 오류",HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("로그인 중 예외 발생", e);
            return new ResponseEntity<>("로그인 실패: ",HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest request){
        try{
            if(request == null){
                logger.error("엑세스 토큰 발급 실패: request가 null입니다. 요청: {}", request);
                return ResponseEntity.status(400).body("엑세스 토큰 발급 실패: 입력 오류");
            }

            TokenResponse tokenResponse = userService.getRefresh(request);

            if (tokenResponse == null) {
                logger.error("로그인 실패: UserService.signup() 결과가 null입니다. 요청: {}", request);
                return new ResponseEntity<>("로그인 실패: 서버 내부 오류", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            return ResponseEntity.ok(tokenResponse);
        } catch (Exception e){
            logger.error("오류 : ",e);
            return new ResponseEntity<>("엑세스 토큰 발급 실패", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest accessRequest, @RequestBody RefreshRequest refreshRequest){
        String accessToken = accessRequest.getHeader("Authorization");
        String refreshToken = refreshRequest.getRefreshToken();

        if(accessToken == null || !accessToken.startsWith("Bearer ")){
            return new ResponseEntity<>("Access Token이 누락되었거나 유효하지 않습니다.", HttpStatus.UNAUTHORIZED);
        }

        accessToken = accessToken.substring(7);

        if (refreshToken == null || refreshToken.isEmpty()) {
            return new ResponseEntity<>("Refresh Token이 누락되었습니다.", HttpStatus.BAD_REQUEST);
        }

        try {
            userService.logout(accessToken, refreshToken);
            return new ResponseEntity<>("로그아웃 성공", HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("서버 오류가 발생했습니다: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateUser(HttpServletRequest accessRequest, @RequestBody SignRequest request){
        try{
            String accessToken = accessRequest.getHeader("Authorization");
            if(accessToken == null || !accessToken.startsWith("Bearer ")){
                return new ResponseEntity<>("Access Token이 누락되었거나 유효하지 않습니다.", HttpStatus.UNAUTHORIZED);
            }

            accessToken = accessToken.substring(7);
            if(request == null){
                logger.error("요청: {}", request);
                return ResponseEntity.status(400).body("프로필 수정 실패: 입력 오류");
            }

            UserProfileResponse response = userService.updateUser(accessToken, request);

            if (response == null) {
                logger.error("결과가 null입니다. 요청: {}", request);
                return new ResponseEntity<>("서버 내부 오류", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e){
            logger.error("오류 : ",e);
            return new ResponseEntity<>("서버 내부 오류", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}
