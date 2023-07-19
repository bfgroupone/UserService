package groupone.userservice.security;
import groupone.userservice.entity.User;
import groupone.userservice.exception.NoTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
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
    public String createToken(AuthUserDetail userDetails){
        Claims claims = Jwts.claims().setSubject(userDetails.getUsername());
        claims.put("permissions", userDetails.getAuthorities());
        claims.put("userId", userDetails.getUserId());
        claims.put("email", userDetails.getEmail());
        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS256, key)
                .compact();
    }

    // resolves the token -> use the information in the token to create a userDetail object
    public Optional<AuthUserDetail> resolveToken(HttpServletRequest request)throws NoTokenException {
        String prefixedToken = request.getHeader("Authorization"); // extract token value by key "Authorization"
        if(prefixedToken == null) {
            throw new NoTokenException("No token founded, please login first.");
        }
        System.out.printf("prefixedToken %s\n",prefixedToken);
        String token = prefixedToken.substring(7); // remove the prefix "Bearer "
        System.out.printf("token %s\n",token);

        Claims claims = Jwts.parser().setSigningKey(key.getBytes(Charset.forName("UTF-8"))).parseClaimsJws(token.replace("{", "").replace("}","")).getBody();// decode
        System.out.printf("claims %s\n",claims.toString());

//        String username = claims.getSubject();
        String email = (String)claims.get("email");

        System.out.printf("token email %s\n",email);

        List<LinkedHashMap<String, String>> permissions = (List<LinkedHashMap<String, String>>) claims.get("permissions");

        // convert the permission list to a list of GrantedAuthority
        List<GrantedAuthority> authorities = permissions.stream()
                .map(p -> new SimpleGrantedAuthority(p.get("authority")))
                .collect(Collectors.toList());

        //return a userDetail object with the permissions the user has
        return Optional.of(AuthUserDetail.builder()
                .email(email)
                .authorities(authorities)
                .build());

    }
}
