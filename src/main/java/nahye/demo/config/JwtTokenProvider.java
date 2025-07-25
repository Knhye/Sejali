package nahye.demo.config;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class JwtTokenProvider {

    private final Key key;
    private final RedisTemplate<String, String> redisTemplate;

    private static final long ACCESS_TOKEN_VALIDITY = 1000L * 60 * 60;         // 60분
    private static final long REFRESH_TOKEN_VALIDITY = 1000L * 60 * 60 * 24 * 7; // 7일

    // JWT 시크릿 키를 application.properties에서 주입
    public JwtTokenProvider(@Value("${spring.jwt.secret}") String secretKey, RedisTemplate<String, String> redisTemplate) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.redisTemplate = redisTemplate;
    }



    // AT 생성
    public String createAccessToken(String userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + ACCESS_TOKEN_VALIDITY);

        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // RT 생성
    public String createRefreshToken(String userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + REFRESH_TOKEN_VALIDITY);

        String refreshToken= Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // deleteRefreshToken 메서드가 userId 기반으로 토큰을 삭제하기 위해서는 아래 로직이 필요함
        // refreshToken을 Redis에 저장
        redisTemplate.opsForValue().set("refreshToken:" + userId, refreshToken, REFRESH_TOKEN_VALIDITY, TimeUnit.MILLISECONDS);

        return refreshToken;
    }


    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }


    // 토큰에서 만료 시간 추출
    public Date getExpirationDateFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
    }

    //토큰 무효화
    public void invalidateToken(String token) {
        Date expiration = getExpirationDateFromToken(token);

        if (expiration == null) {
            throw new IllegalArgumentException("Access Token의 만료 시간을 확인할 수 없습니다.");
        }

        long remainingTime = expiration.getTime() - System.currentTimeMillis();

        if (remainingTime <= 0) {
            throw new IllegalArgumentException("유효하지 않거나 이미 만료된 Access Token입니다.");
        } else{
            redisTemplate.opsForValue().set("blacklist:" + token, "invalidated", remainingTime, TimeUnit.MILLISECONDS);
        }
    }

    // 블랙리스트에 있는지 확인
    public boolean isTokenBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + token));
    }

    public boolean deleteRefreshToken(String refreshToken) {
        try {
            String userId = getUsername(refreshToken);

            // Redis에서 해당 userId에 연결된 Refresh Token 키 삭제
            // Redis에 저장할 때 사용했던 키 패턴("refreshToken:{userId}")을 동일하게 사용합니다.
            Boolean deleted = redisTemplate.delete("refreshToken:" + userId);

            // Redis delete 메서드는 삭제 성공 시 true, 실패 시 (키가 없으면) false를 반환합니다.
            return Boolean.TRUE.equals(deleted); // null 체크를 위해 Boolean.TRUE.equals() 사용
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("유효하지 않은 Refresh Token: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Refresh Token 삭제 중 오류 발생: " + e.getMessage());
            return false;
        }
    }
}