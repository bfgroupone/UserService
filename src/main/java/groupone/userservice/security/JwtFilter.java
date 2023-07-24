package groupone.userservice.security;

import groupone.userservice.exception.NoTokenException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

//The jwt filter that we want to add to the chain of filters of Spring Security
@Component
public class JwtFilter extends OncePerRequestFilter {


    private JwtProvider jwtProvider;

    @Autowired
    public void setJwtProvider(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getRequestURI().equals("/user-service/register")
                || request.getRequestURI().equals("/user-service/login")
                || (request.getRequestURI().equals("/user-service/validate"))) {
            filterChain.doFilter(request, response);
            return;
        }

        Optional<AuthUserDetail> authUserDetailOptional;
        try {
            String token = jwtProvider.extractToken(request);
            authUserDetailOptional = jwtProvider.resolveToken(request); // extract jwt from request, generate a userdetails object
            if (authUserDetailOptional.isPresent()) {

                AuthUserDetail authUserDetail = authUserDetailOptional.get();
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        authUserDetail.getUsername(),
                        token,
                        authUserDetail.getAuthorities()
                ); // generate authentication object

                SecurityContextHolder.getContext().setAuthentication(authentication); // put authentication object in the secruitycontext
            }
            filterChain.doFilter(request, response);
        } catch (NoTokenException e) {
            e.printStackTrace();
        }
    }
}