package groupone.userservice.service;

import groupone.userservice.dao.UserDao;
import groupone.userservice.dto.request.UserPatchRequest;
import groupone.userservice.dto.response.DataResponse;
import groupone.userservice.entity.History;
import groupone.userservice.entity.User;
import groupone.userservice.entity.UserType;
import groupone.userservice.exception.InvalidTypeAuthorization;
import groupone.userservice.security.AuthUserDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {
    private UserDao userDao;
    private RemoteHistoryService remoteHistoryService;

    @Autowired
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    @Autowired
    public void setRemoteHistoryService(RemoteHistoryService remoteHistoryService) {
        this.remoteHistoryService = remoteHistoryService;
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
    public List<History> getHistory() {
        DataResponse response = remoteHistoryService.getAllHistory().getBody();
        List<History> histories = (List<History>) response.getData();
        return histories;
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
    public User updateUserProfile(UserPatchRequest request, int uid) {
        User user = userDao.getUserById(uid);
        if(!request.getProfileImageURL().isEmpty()) {
            user.setProfileImageURL(request.getProfileImageURL());
        }
        if(!request.getEmail().isEmpty()) {
            // sending verification code,
            // user type is invalid
//            user.setEmail(request.getEmail());
//            user.setType(3);
            System.out.println("email empty");
            // code to send email for validation
        }
        return user;
    }

    @Transactional
    public User updateUserType(int uid, int type) throws InvalidTypeAuthorization {
        User user = userDao.getUserById(uid);
        userDao.setType(user, type);
        return user;
    }

}
