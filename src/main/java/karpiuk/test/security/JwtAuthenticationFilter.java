package karpiuk.test.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import karpiuk.test.service.BlackListingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String BEARER_TOKEN = "Bearer ";
    private static final String AUTHORIZATION = "Authorization";
    private static final int INDEX = 7;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final BlackListingService blackListingService;

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse resp,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = getTokenFromHeader(req);
        String jwtBlackList = blackListingService.getJwtBlackList(token);

        if (token != null && token.equals(jwtBlackList)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json");
            String errorMessage = "{\"error\": \"Your access token is invalid. Please log in.\"}";
            resp.getWriter().write(errorMessage);
            return;
        }
        if (token != null && jwtBlackList == null && jwtUtil.isValidToken(token)) {
            String username = jwtUtil.getUserName(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null,
                            userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

        }
        filterChain.doFilter(req, resp);
    }

    private String getTokenFromHeader(HttpServletRequest req) {
        String bearerToken = req.getHeader(AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_TOKEN)) {
            return bearerToken.substring(INDEX);
        }
        return null;
    }
}
