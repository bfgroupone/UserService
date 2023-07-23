package groupone.userservice.security;

import groupone.userservice.exception.NoTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@PropertySource("classpath:application.properties")
public class JwtProvider {
    @Value("${security.jwt.token.key}")
    private String key;

    private String token;


    // create jwt from a UserDetail
    public String createToken(AuthUserDetail userDetails) {
        Claims claims = Jwts.claims().setSubject(userDetails.getUsername());
        List<String> authorities = new ArrayList<>();
        for (GrantedAuthority c : userDetails.getAuthorities()) {
            authorities.add(c.getAuthority());
        }

        claims.put("permissions", authorities);
        claims.put("userId", userDetails.getUserId());
        claims.put("email", userDetails.getEmail());
        claims.put("active", userDetails.getActive());
        claims.put("role", userDetails.getUserType());
        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS256, key)
                .compact();
    }

    // resolves the token -> use the information in the token to create a userDetail object
    public Optional<AuthUserDetail> resolveToken(HttpServletRequest request) throws NoTokenException {
        String prefixedToken = request.getHeader("Authorization"); // extract token value by key "Authorization"
        System.out.printf("prefixedToken %s\n", prefixedToken);

        if (prefixedToken != null && prefixedToken.startsWith("Bearer")) {
            token = prefixedToken.substring(7); // remove the prefix "Bearer "
        } else {
            throw new NoTokenException("Invalid token, please login or check your token type");
        }

        String token = prefixedToken.substring(7); // remove the prefix "Bearer "
        System.out.printf("token %s\n", token);

        Claims claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody(); // decode

        List<GrantedAuthority> authorities = ((List<String>) claims.get("permissions")).stream()
                .map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        System.out.println(authorities);

        int userId = (int) claims.get("userId");
        String email = (String) claims.get("email");
        boolean active = (boolean) claims.get("active");

        System.out.printf("userId %s\n", userId);
        System.out.printf("email %s\n", email);
        System.out.printf("active %s\n", active);

        //return a userDetail object with the permissions the user has
        return Optional.of(AuthUserDetail.builder()
                .email(email)
                .userId(userId)
                .active(active)
                .authorities(authorities)
                .build());

    }

    public String extractToken(HttpServletRequest request) throws NoTokenException {

        String header = request.getHeader("Authorization");
        if (header == null) {
            throw new NoTokenException("No token founded, please login first.");
        }
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7); // Extract the token without the "Bearer " prefix
        }
        return null; // Token not found in the request header
    }
}
