package groupone.userservice.service;

import groupone.userservice.dao.UserDao;
import groupone.userservice.dto.request.RegisterRequest;
import groupone.userservice.dto.request.UserPatchRequest;
import groupone.userservice.dto.user.UserGeneralDTO;
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

//    @Transactional
//    public void deleteUser(User user) {
//        userDao.deleteUser(user);
//    }

    public User getUserById(Integer userId) {
        User user = userDao.getUserById(userId);
        if (user == null) {
            throw new UsernameNotFoundException("Cannot find user with uid: " + userId);
        }

        return user;
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
                .active(user.isActive())
                .authorities(getAuthoritiesFromUser(user))
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .enabled(true)
                .build();
    }

    public List<GrantedAuthority> getAuthoritiesFromUser(User user) {
        List<GrantedAuthority> userAuthorities = new ArrayList<>();

        if (user.getType() == UserType.SUPER_ADMIN.ordinal()) {
            List<String> auths = new ArrayList<String>();
            userAuthorities.add(new SimpleGrantedAuthority("read"));
            userAuthorities.add(new SimpleGrantedAuthority("admin_read"));
            userAuthorities.add(new SimpleGrantedAuthority("write"));
            userAuthorities.add(new SimpleGrantedAuthority("delete"));
            userAuthorities.add(new SimpleGrantedAuthority("ban_unban"));
            userAuthorities.add(new SimpleGrantedAuthority("recover"));
            userAuthorities.add(new SimpleGrantedAuthority("promote"));
        } else if (user.getType() == UserType.ADMIN.ordinal()) {
            userAuthorities.add(new SimpleGrantedAuthority("read"));
            userAuthorities.add(new SimpleGrantedAuthority("admin_read"));
            userAuthorities.add(new SimpleGrantedAuthority("write"));
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
        }

        return userAuthorities;
    }

    @Transactional
    public boolean existsByEmail(String email) {
        return userDao.loadUserByEmail(email).isPresent();
    }

    @Transactional
    public int addUser(RegisterRequest request) throws InvalidCredentialsException {
        if (existsByEmail(request.getEmail())) {
            throw new InvalidCredentialsException("Email already exists.");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(request.getPassword())
                .dateJoined(new Date(Timestamp.valueOf(LocalDateTime.now()).getTime()))
                .type(UserType.NORMAL_USER_NOT_VALID.ordinal())
                .active(true)
                .build();

        if (request.getProfileImageURL() == null || request.getProfileImageURL().equals("")) {
            user.setProfileImageURL("https://bfgroupone.s3.amazonaws.com/1690076947341_default_avatar.png");
        } else {
            user.setProfileImageURL(request.getProfileImageURL());
        }

        int userId = userDao.addUser(user);
//        String token = createValidationToken(userId);
//        user.setValidationToken(token);

        return userId;
    }

    @Transactional
    public User updateUserProfile(UserPatchRequest request, int uid) {
        User user = userDao.getUserById(uid);
        if (user == null) {
            throw new UsernameNotFoundException("Cannot find user with uid: " + uid);
        }
        if (!request.getFirstName().equals("")) {
            user.setFirstName(request.getFirstName());
        }

        if (!request.getLastName().equals("")) {
            user.setLastName(request.getLastName());
        }

        if (!request.getEmail().equals("")) {
            user.setEmail(request.getEmail());
            user.setType(UserType.NORMAL_USER_NOT_VALID.ordinal());
        }

        if (!request.getPassword().equals("")) {
            user.setPassword(request.getPassword());
        }

        if (!request.getProfileImageURL().isEmpty()) {
            user.setProfileImageURL(request.getProfileImageURL());
        }

        return user;
    }

    @Transactional
    public User promoteUser(int uid) throws InvalidTypeAuthorization {
        User user = userDao.getUserById(uid);
        if (user == null) {
            throw new UsernameNotFoundException("Cannot find user with uid: " + uid);
        }
        int origType = user.getType();

        if (origType != UserType.NORMAL_USER.ordinal()) {
            throw new InvalidTypeAuthorization("Can only promote normal users");
        }

        user.setType(UserType.ADMIN.ordinal());
        return user;
    }


    @Transactional
    public User updateUserActive(int uid) throws InvalidTypeAuthorization {
        User user = userDao.getUserById(uid);
        if (user == null) {
            throw new UsernameNotFoundException("Cannot find user with uid: " + uid);
        }
        int origType = user.getType();

        if (origType == UserType.SUPER_ADMIN.ordinal() || origType == UserType.ADMIN.ordinal()) {
            throw new InvalidTypeAuthorization("Can not ban user type: " + UserType.getLabelFromOrdinal(origType));
        }
        user.setActive(!user.isActive());
        return user;
    }


    @Transactional
    public String createValidationToken(User user, String key) {
        int userId = user.getId();

        String token;

        // Calculate the expiration time (3 hours from now)
        long expirationTimeMillis = System.currentTimeMillis() + 3 * 60 * 60 * 1000; // 3 hours in milliseconds
        Date expirationDate = new Date(expirationTimeMillis); // Build the JWT token
        token = Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS256, key)
                .compact();
        user.setValidationToken(token);
        return token;
    }

    @Transactional
    public boolean validateEmailToken(String token, boolean checkOnly, String key) {
        try {
            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(key).parseClaimsJws(token);
            if (checkOnly) {
                return true;
            }
            int userId = Integer.parseInt(claimsJws.getBody().getSubject());
            User user = userDao.getUserById(userId);

            user.setType(UserType.NORMAL_USER.ordinal());
            return true;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    @Transactional
    public UserGeneralDTO getUserGeneralInfo(int userId) {
        User user = this.userDao.getUserById(userId);
        if (user == null) {
            throw new UsernameNotFoundException("Cannot find user with uid: " + userId);
        }
        return UserGeneralDTO.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .profileImageURL(user.getProfileImageURL()).build();
    }
}
