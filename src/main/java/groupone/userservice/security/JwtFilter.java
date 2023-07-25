package groupone.userservice.security;

import groupone.userservice.exception.NoTokenException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
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

        Optional<AuthUserDetail> authUserDetailOptional = null; // extract jwt from request, generate a userdetails object
        try {
            authUserDetailOptional = jwtProvider.resolveToken(request);
        } catch (NoTokenException e) {
            throw new RuntimeException(e);
        }
        if (authUserDetailOptional.isPresent()){
            AuthUserDetail authUserDetail = authUserDetailOptional.get();
            LoginUserAuthentication authentication = new LoginUserAuthentication(
                    authUserDetail.getUserId(), (List<GrantedAuthority>) authUserDetail.getAuthorities(), true, authUserDetail.getEmail());
            SecurityContextHolder.getContext().setAuthentication(authentication); // put authentication object in the secruitycontext
        }
        filterChain.doFilter(request, response);
    }
}