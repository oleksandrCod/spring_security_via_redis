package karpiuk.test.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String BEARER_TOKEN = "Bearer ";
    private static final String HEADER_NAME = "Authorization";

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest req,
            HttpServletResponse resp,
            FilterChain filterChain) throws ServletException, IOException {
        log.info("Processing authentication filter for request: {}", req.getRequestURI());

        String token = getTokenFromHeader(req);
        if (token != null && jwtTokenProvider.isValidToken(token)) {
            log.info("Valid token found in request header. Setting authentication in security context.");

            setAuthentication(token);
        } else {
            log.info("No valid token found in request header.");
        }
        filterChain.doFilter(req, resp);
    }

    private void setAuthentication(String token) {
        log.info("Setting authentication for token: {}", token);

        String username = jwtTokenProvider.getUserNameFromJwtToken(token);

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.info("Authentication set for user: {}", username);
    }

    private String getTokenFromHeader(HttpServletRequest req) {
        log.info("Getting token from request header");

        String bearerToken = req.getHeader(HEADER_NAME);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_TOKEN)) {
            log.info("Bearer token found in request header");
            return bearerToken.substring(BEARER_TOKEN.length());
        }
        log.info("No bearer token found in request header");
        return null;
    }
}
