package groupone.userservice.service;
import groupone.userservice.dao.UserDao;
import groupone.userservice.dto.response.AllHistoryResponse;
import groupone.userservice.entity.History;
import groupone.userservice.entity.User;


import groupone.userservice.exception.UserException;
import groupone.userservice.security.AuthUserDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

//import javax.transaction.Transactional;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
    public void setRemoteHistoryService(RemoteHistoryService remoteHistoryService) {this.remoteHistoryService = remoteHistoryService;}



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
        if (!userOptional.isPresent()){
            throw new UsernameNotFoundException("Username does not exist");
        }

        User user = userOptional.get(); // database user
        //getAuthoritiesFromUser
        return AuthUserDetail.builder() // spring security's userDetail
                .email(user.getEmail())
                .password(new BCryptPasswordEncoder().encode(user.getPassword()))
//                .authorities(getAuthoritiesFromUser(user))
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .enabled(true)
                .build();
    }

    @Transactional
    public List<History> getHistoryByUid(Integer uid) {
        return remoteHistoryService.getAllHistory().getHistorylist()
                .stream().filter(history -> history.getUserId().equals(uid)).collect(Collectors.toList());
    }
    @Transactional
    public void addUser(String firstName, String lastName, String email, String password, String profileImageUrl){
        int id = userDao.getAllUsers().size()+1;
        User user = new User(id, email, firstName, lastName, password,new Date(),2,profileImageUrl);
        userDao.addUser(user);
    }

}
