package groupone.userservice.service;

import groupone.userservice.dao.UserDao;
import groupone.userservice.entity.User;
import groupone.userservice.entity.UserType;
import groupone.userservice.security.AuthUserDetail;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
    @Value("${email.validation.token.key}")
    private String validationEmailKey;

    private UserDao userDao;

    @Autowired
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }




    @Transactional
    public List<User> getAllUsers() {
        List<User> users = userDao.getAllUsers();
        return users;
    }

    @Transactional
    public void createUser(User... users) {
        for (User u : users) {
            userDao.addUser(u);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> userOptional = userDao.loadUserByEmail(email);
        if (!userOptional.isPresent()) {
            throw new UsernameNotFoundException("Username does not exist");
        }

        User user = userOptional.get(); // database user
        //getAuthoritiesFromUser
        return AuthUserDetail.builder() // spring security's userDetail
                .email(user.getEmail())
                .password(new BCryptPasswordEncoder().encode(user.getPassword()))
                .userId(user.getId())
                .type(UserType.getLabelFromOrdinal(user.getType()))
                .authorities(getAuthoritiesFromUser(user))
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .enabled(true)
                .build();
    }

    private List<GrantedAuthority> getAuthoritiesFromUser(User user) {
        List<GrantedAuthority> userAuthorities = new ArrayList<>();

        if (user.getType() == UserType.SUPER_ADMIN.ordinal()) {
            List<String> auths = new ArrayList<String>();
            userAuthorities.add(new SimpleGrantedAuthority("read"));
            userAuthorities.add(new SimpleGrantedAuthority("admin_read"));
            userAuthorities.add(new SimpleGrantedAuthority("delete"));
            userAuthorities.add(new SimpleGrantedAuthority("ban_unban"));
            userAuthorities.add(new SimpleGrantedAuthority("recover"));
            userAuthorities.add(new SimpleGrantedAuthority("promote"));
        } else if (user.getType() == UserType.ADMIN.ordinal()) {
            userAuthorities.add(new SimpleGrantedAuthority("read"));
            userAuthorities.add(new SimpleGrantedAuthority("admin_read"));
            userAuthorities.add(new SimpleGrantedAuthority("delete"));
            userAuthorities.add(new SimpleGrantedAuthority("ban_unban"));
            userAuthorities.add(new SimpleGrantedAuthority("recover"));
        } else if (user.getType() == UserType.NORMAL_USER.ordinal()) {
            userAuthorities.add(new SimpleGrantedAuthority("read"));
            userAuthorities.add(new SimpleGrantedAuthority("write"));
            userAuthorities.add(new SimpleGrantedAuthority("delete"));
            userAuthorities.add(new SimpleGrantedAuthority("update"));
        } else if (user.getType() == UserType.NORMAL_USER_NOT_VALID.ordinal()) {
            userAuthorities.add(new SimpleGrantedAuthority("read"));
        } else if (user.getType() == UserType.VISITOR_BANNED.ordinal()) {
        }

        return userAuthorities;
    }


    @Transactional
    public void addUser(String firstName, String lastName, String email, String password, String profileImageUrl) {
        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .password(password)
                .dateJoined(new Date(Timestamp.valueOf(LocalDateTime.now()).getTime()))
                .type(UserType.NORMAL_USER_NOT_VALID.ordinal())
                .build();
        
        if (profileImageUrl.length() != 0) {
            user.setProfileImageURL(profileImageUrl);
        }
        userDao.addUser(user);
    }
    @Transactional
    public User getUserById(Integer userId) {
        return userDao.getUserById(userId);
    }

    @Transactional
    public void deleteUser(User user) {
        userDao.deleteUser(user);
    }

    public String createValidationToken(int userId) {
        // Calculate the expiration time (3 hours from now)
        long expirationTimeMillis = System.currentTimeMillis() + 3 * 60 * 60 * 1000; // 3 hours in milliseconds
        Date expirationDate = new Date(expirationTimeMillis); // Build the JWT token
        String token = Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS256, validationEmailKey)
                .compact();
        return token;
    }

    public void isJwtTokenValid(String token) {
        try {
            // Parse the token using the secret key
            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(validationEmailKey).parseClaimsJws(token);


            // Check if the token has expired
//            Date expirationDate = claimsJws.getBody().getExpiration();
//            Date currentDate = new Date();
//            return !currentDate.after(expirationDate);

        } catch (Exception e) {
            // Token is invalid or has expired return false; }
        }
    }
}
