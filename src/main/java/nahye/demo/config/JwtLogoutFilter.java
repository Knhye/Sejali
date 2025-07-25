package nahye.demo.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtLogoutFilter extends OncePerRequestFilter {

    private final RequestMatcher logoutRequestMatcher;
    private final JwtTokenProvider tokenProvider;
    private final LogoutSuccessHandler logoutSuccessHandler;
    private final LogoutHandler logoutHandler; // SecurityContextLogoutHandler 등 추가 가능

    public JwtLogoutFilter(String logoutUrl, JwtTokenProvider tokenProvider, LogoutSuccessHandler logoutSuccessHandler) {
        this.logoutRequestMatcher = request ->
                request.getMethod().equalsIgnoreCase("POST") && request.getRequestURI().equals(logoutUrl);
        this.tokenProvider = tokenProvider;
        this.logoutSuccessHandler = logoutSuccessHandler;
        this.logoutHandler = (request, response, authentication) -> {
            // JWT 토큰 무효화 로직
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);
                tokenProvider.invalidateToken(token);
            }
            SecurityContextHolder.clearContext(); // SecurityContext 비우기
        };
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (logoutRequestMatcher.matches(request)) {
            // 1. JWT 토큰 무효화 (블랙리스트 등) 및 SecurityContext 초기화
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            this.logoutHandler.logout(request, response, authentication);

            // 2. 로그아웃 성공 처리 (리다이렉션 등)
            this.logoutSuccessHandler.onLogoutSuccess(request, response, authentication);
            return; // 로그아웃 처리 후 필터 체인 중단
        }

        filterChain.doFilter(request, response);
    }
}
