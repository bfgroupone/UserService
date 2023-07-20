package groupone.userservice.service;

import groupone.userservice.dao.UserDao;
import groupone.userservice.dto.request.UserPatchRequest;
import groupone.userservice.entity.User;
import groupone.userservice.entity.UserType;
import groupone.userservice.exception.InvalidTypeAuthorization;
import groupone.userservice.security.AuthUserDetail;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.http.auth.InvalidCredentialsException;
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

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
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
    public boolean existsByEmail(String email){
        return userDao.loadUserByEmail(email).isPresent();
    }

    @Transactional
    public String addUser(String firstName, String lastName, String email, String password, String profileImageUrl) throws InvalidCredentialsException {
        if (existsByEmail(email)) {
            throw new InvalidCredentialsException("Email already exists.");
        }

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

        int userId = userDao.addUser(user);
        String token = createValidationToken(userId);
        user.setValidationToken(token);

        return token;
    }

    @Transactional
    public User updateUserProfile(UserPatchRequest request, int uid) {
        User user = userDao.getUserById(uid);
        if (!request.getProfileImageURL().isEmpty()) {
            user.setProfileImageURL(request.getProfileImageURL());
        }
        if (!request.getEmail().equals("")) {
            // sending verification code,
            // user type is invalid
            user.setEmail(request.getEmail());
            user.setType(3);
//            System.out.println("email empty");
            // code to send email for validation
        }
        if (!request.getPassword().equals("")) {
            user.setPassword(request.getPassword());
        }
        return user;
    }

    @Transactional
    public User updateUserType(int uid, int type) throws InvalidTypeAuthorization {
        User user = userDao.getUserById(uid);
        userDao.setType(user, type);
        return user;
    }

    public User getUserById(Integer userId) {
        return userDao.getUserById(userId);
    }

    @Transactional
    public void deleteUser(User user) {
        userDao.deleteUser(user);
    }

    @Transactional
    public String createValidationToken(int userId) {
        User user = userDao.getUserById(userId);

        String token = user.getValidationToken();

        // If current token is valid, reuse it
        if (validateEmailToken(token, true)) {
            return token;
        }

        // Calculate the expiration time (3 hours from now)
        long expirationTimeMillis = System.currentTimeMillis() + 3 * 60 * 60 * 1000; // 3 hours in milliseconds
        Date expirationDate = new Date(expirationTimeMillis); // Build the JWT token
        token = Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS256, validationEmailKey)
                .compact();
        user.setValidationToken(token);
        return token;
    }

    @Transactional
    public boolean validateEmailToken(String token, boolean checkOnly) {
        try {
            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(validationEmailKey).parseClaimsJws(token);

            Date expirationDate = claimsJws.getBody().getExpiration();
            if (isJwtTokenValid(expirationDate)) {
                if (checkOnly) {
                    return true;
                }
                int userId = Integer.parseInt(claimsJws.getBody().getSubject());
                User user = userDao.getUserById(userId);
                userDao.setType(user, UserType.NORMAL_USER.ordinal());
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isJwtTokenValid(Date expirationDate) {
        Date currentDate = new Date();

        return !currentDate.after(expirationDate);
    }
}
