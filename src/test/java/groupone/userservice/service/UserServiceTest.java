package groupone.userservice.service;

import groupone.userservice.dao.UserDao;
import groupone.userservice.dto.request.RegisterRequest;
import groupone.userservice.dto.request.UserPatchRequest;
import groupone.userservice.dto.user.UserGeneralDTO;
import groupone.userservice.entity.User;
import groupone.userservice.entity.UserType;
import groupone.userservice.exception.InvalidTypeAuthorization;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.http.auth.InvalidCredentialsException;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.TestPropertySource;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;


@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Value("${email.validation.token.key}")
    private String validationEmailKey;


    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserService userService;

    String sDate1="7/12/2014";
    Date date1=new SimpleDateFormat("MM/dd/yyyy").parse(sDate1);
    String sDate2="4/21/2014";
    Date date2=new SimpleDateFormat("MM/dd/yyyy").parse(sDate2);
    private User user1 = new User(1,"john@example.com","John","Doe","123", date1, true, 1,"https://drive.google.com/file/d/1Ul78obBTS0zgaVOufCHpUKwMxBvDON-i/view", "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiZXhwIjoxNjg5ODkyMjc0fQ.bMc7kDmKL92H3DJleE7G7u9v0Y98KLDk4qPjPEbZdoo");
    private User user2 = new User(2,"jane@example.com","Jane","Smith","123", date2, true,2,"url-new","eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIzIiwiZXhwIjoxNjkwMDU3MzcwfQ.mWBG0g25cfv9K-rn-XuScThmUx4KEU04323-6kaZDZI");

    public UserServiceTest() throws ParseException {
    }

    @Test
    public void test_getAllUsers(){
        List<User> expected = new ArrayList<>();
        expected.add(user1);
        expected.add(user2);

        Mockito.when(userDao.getAllUsers()).thenReturn(expected);

        assertEquals(expected, userService.getAllUsers());
    }

    @Test
    public void test_getUserById() {
        Integer userId = 1;
        User user = user1;
        Mockito.when(userDao.getUserById(userId)).thenReturn(user);

        User actualUser = userService.getUserById(userId);

        assertEquals(user, actualUser);
    }

    @Test
    public void test_getUserById_empty() {
        Integer userId = 1;
        Mockito.when(userDao.getUserById(userId)).thenReturn(null);


        assertThrows(UsernameNotFoundException.class, () -> userService.getUserById(userId));
    }

    @Test
    public void test_loadUserByUsername_UserNotFound() {
        String email = "notfound@example.com";
        Mockito.when(userDao.loadUserByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername(email));
    }

    @Test
    public void test_loadUserByUsername() {
        String email = "johndoe@example.com";
        User user = user1;
        user.setEmail(email);
        Mockito.when(userDao.loadUserByEmail(email)).thenReturn(Optional.of(user));

        UserDetails userDetails = userService.loadUserByUsername(email);

        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());
        assertEquals(true, new BCryptPasswordEncoder().matches(user.getPassword(), userDetails.getPassword()));

        assertTrue(userDetails.isEnabled());
    }
    @Test
    public void test_getAuthoritiesFromUser_NormalUser() {
        User user = new User();
        user.setType(UserType.NORMAL_USER.ordinal());

        List<GrantedAuthority> authorities = userService.getAuthoritiesFromUser(user);

        assertNotNull(authorities);
        assertTrue(authorities.contains(new SimpleGrantedAuthority("read")));
        assertTrue(authorities.contains(new SimpleGrantedAuthority("write")));
        assertTrue(authorities.contains(new SimpleGrantedAuthority("delete")));
        assertTrue(authorities.contains(new SimpleGrantedAuthority("update")));
    }
    @Test
    public void test_getAuthoritiesFromUser_NormalUserNotValid() {
        User user = new User();
        user.setType(UserType.NORMAL_USER_NOT_VALID.ordinal());

        List<GrantedAuthority> authorities = userService.getAuthoritiesFromUser(user);

        assertNotNull(authorities);
        assertTrue(authorities.contains(new SimpleGrantedAuthority("read")));
        assertFalse(authorities.contains(new SimpleGrantedAuthority("write")));
        assertFalse(authorities.contains(new SimpleGrantedAuthority("delete")));
        assertFalse(authorities.contains(new SimpleGrantedAuthority("update")));
    }

    @Test
    public void test_getAuthoritiesFromUser_Admin() {
        User user = new User();
        user.setType(UserType.ADMIN.ordinal());

        List<GrantedAuthority> authorities = userService.getAuthoritiesFromUser(user);

        assertNotNull(authorities);
        assertTrue(authorities.contains(new SimpleGrantedAuthority("read")));
        assertTrue(authorities.contains(new SimpleGrantedAuthority("admin_read")));
        assertTrue(authorities.contains(new SimpleGrantedAuthority("delete")));
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ban_unban")));
        assertTrue(authorities.contains(new SimpleGrantedAuthority("recover")));
        assertFalse(authorities.contains(new SimpleGrantedAuthority("update")));
    }

    @Test
    public void test_getAuthoritiesFromUser_SuperAdmin() {
        User user = new User();
        user.setType(UserType.SUPER_ADMIN.ordinal());

        List<GrantedAuthority> authorities = userService.getAuthoritiesFromUser(user);

        assertNotNull(authorities);
        assertTrue(authorities.contains(new SimpleGrantedAuthority("read")));
        assertTrue(authorities.contains(new SimpleGrantedAuthority("admin_read")));
        assertTrue(authorities.contains(new SimpleGrantedAuthority("delete")));
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ban_unban")));
        assertTrue(authorities.contains(new SimpleGrantedAuthority("recover")));
        assertTrue(authorities.contains(new SimpleGrantedAuthority("promote")));
        assertFalse(authorities.contains(new SimpleGrantedAuthority("update")));
    }

    @Test
    public void test_existsByEmail_EmailExists() {
        String email = "john@example.com";
        Mockito.when(userDao.loadUserByEmail(email)).thenReturn(Optional.of(new User()));

        boolean result = userService.existsByEmail(email);

        assertTrue(result);
    }
    @Test
    public void test_existsByEmail_EmailDoesNotExist() {
        String email = "nonexistent@example.com";
        Mockito.when(userDao.loadUserByEmail(email)).thenReturn(Optional.empty());

        boolean result = userService.existsByEmail(email);

        assertFalse(result);
    }
    @Test
    public void test_addUser_Success() throws InvalidCredentialsException {
        RegisterRequest request = new RegisterRequest("John", "Doe", "john@example.com", "123", "url");


        Mockito.when(userDao.loadUserByEmail(request.getEmail())).thenReturn(Optional.empty());
        Mockito.when(userDao.addUser(any(User.class))).thenReturn(1); // Return the user ID for testing purposes

        assertEquals(1, userService.addUser(request));

    }
    @Test
    public void test_addUser_Success_EmptyImgUrl() throws InvalidCredentialsException {
        RegisterRequest request = new RegisterRequest("John", "Doe", "john@example.com", "123", null);

        Mockito.when(userDao.loadUserByEmail(request.getEmail())).thenReturn(Optional.empty());
        Mockito.when(userDao.addUser(any(User.class))).thenReturn(1); // Return the user ID for testing purposes

        assertEquals(1, userService.addUser(request));

    }
    @Test
    public void test_addUser_exist(){
        RegisterRequest request = new RegisterRequest("John", "Doe", "john@example.com", "123", "url");


        Mockito.when(userDao.loadUserByEmail(request.getEmail())).thenReturn(Optional.of(new User()));

        assertThrows(InvalidCredentialsException.class, () -> userService.addUser(request));

    }

    @Test
    public void test_updateUserProfile_Success() {
        int userId = 1;
        UserPatchRequest request = new UserPatchRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setPassword("newpassword");
        request.setProfileImageURL("https://example.com/profile.jpg");


        Mockito.when(userDao.getUserById(userId)).thenReturn(user1);

        User updatedUser = userService.updateUserProfile(request, userId);

        assertEquals(request.getFirstName(), updatedUser.getFirstName());
        assertEquals(request.getLastName(), updatedUser.getLastName());
        assertEquals(request.getEmail(), updatedUser.getEmail());
        assertEquals(request.getPassword(), updatedUser.getPassword());
        assertEquals(request.getProfileImageURL(), updatedUser.getProfileImageURL());
    }

    @Test
    public void testUpdateUserType_NormalUserPromote_Success() throws InvalidTypeAuthorization {
        int userId = 1;
        int newType = UserType.ADMIN.ordinal();
        List<String> authorities = new ArrayList<>();
        authorities.add("promote");

        User existingUser = user1;
        existingUser.setType(UserType.NORMAL_USER.ordinal());

        Mockito.when(userDao.getUserById(userId)).thenReturn(existingUser);

        User updatedUser = userService.promoteUser(userId);

        assertEquals(newType, updatedUser.getType());
    }
    @Test
    public void testUpdateUserType_SuperAdminPromote_Fail() throws InvalidTypeAuthorization {
        int userId = 1;
        List<String> authorities = new ArrayList<>();
        authorities.add("promote");

        User existingUser = user1;
        existingUser.setType(UserType.SUPER_ADMIN.ordinal());

        Mockito.when(userDao.getUserById(userId)).thenReturn(existingUser);

        assertThrows(InvalidTypeAuthorization.class, () -> userService.promoteUser(userId));
    }



    @Test
    public void test_updateUserActive_Success() throws InvalidTypeAuthorization {
        // Create a user with UserType.NORMAL_USER
        User user = user1;
        user.setType(UserType.NORMAL_USER.ordinal());
        boolean userstate = true;
        user.setActive(userstate);
        // Set up the userDao mock
        Mockito.when(userDao.getUserById(1)).thenReturn(user);

        // Call the updateUserActive method
        User updatedUser = userService.updateUserActive(1);

        // Assert that the user's active status is toggled (from true to false or vice versa)
        assertNotEquals(userstate, updatedUser.isActive());
    }


    @Test
    public void test_updateUserActive_CanNotBanAdmin() throws InvalidTypeAuthorization {
        // Create a user with UserType.NORMAL_USER
        User user = user1;
        user.setType(UserType.ADMIN.ordinal());
        boolean userstate = true;
        user.setActive(userstate);

        // Set up the userDao mock
        Mockito.when(userDao.getUserById(1)).thenReturn(user);

        // Assert that the user's active status is toggled (from true to false or vice versa)
        assertThrows(InvalidTypeAuthorization.class, () -> userService.updateUserActive(1));
    }
    @Test
    public void test_createValidationToken_Success() {
        int userId = 1;

        // Create a user with the given userId
        User user = user1;
        user.setId(userId);

        String token = userService.createValidationToken(user, "grouponeEmail");
        assertNotNull(token);
    }

    @Test
    public void test_validateEmailToken_Success() {
        // Generate a valid JWT token
        int userId = 5;
        User user = user1;
        user.setType(UserType.NORMAL_USER_NOT_VALID.ordinal());
        String token = Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setExpiration(new Date(System.currentTimeMillis() + 100000000)) // Set an expiration time in the future
                .signWith(SignatureAlgorithm.HS256, "grouponeEmail")
                .compact();
        Mockito.when(userDao.getUserById(userId)).thenReturn(user);

        boolean isValid = userService.validateEmailToken(token, false, "grouponeEmail");
        assertEquals(user.getType(), UserType.NORMAL_USER.ordinal());

        assertTrue(isValid);
    }
    @Test
    public void test_validateEmailToken_Success_checkTrue() {
        // Generate a valid JWT token
        int userId = 5;
        String token = Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setExpiration(new Date(System.currentTimeMillis() + 100000000)) // Set an expiration time in the future
                .signWith(SignatureAlgorithm.HS256, "grouponeEmail")
                .compact();

        boolean isValid = userService.validateEmailToken(token, true, "grouponeEmail");
        assertTrue(isValid);
    }
    @Test
    public void test_validateEmailToken_fail() {
        // Generate a valid JWT token
        int userId = 5;
        String token = Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setExpiration(new Date(System.currentTimeMillis() + 10000)) // Set an expiration time in the past
                .signWith(SignatureAlgorithm.HS256, "grouponeEmail")
                .compact();
        boolean isValid = userService.validateEmailToken(token, true, "grouponeEma");
        assertFalse(isValid);
    }


    @Test
    public void test_validateEmailToken_exception() {
        // Generate a valid JWT token
        int userId = 5;
        User user = user1;
        user.setType(UserType.NORMAL_USER_NOT_VALID.ordinal());
        String token = Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setExpiration(new Date(System.currentTimeMillis() + 100000000)) // Set an expiration time in the future
                .signWith(SignatureAlgorithm.HS256, "grouponeEmail")
                .compact();

        boolean isValid = userService.validateEmailToken(token, false, null);

        assertFalse(isValid);
    }

    @Test
    public void test_GetUserGeneralInfo(){
        User user = user1;


        Mockito.when(userDao.getUserById(anyInt())).thenReturn(user);
        UserGeneralDTO newDto = userService.getUserGeneralInfo(1);
        assertEquals(newDto.getFirstName(), user.getFirstName());
        assertEquals(newDto.getLastName(), user.getLastName());
    }

    @Test
    public void test_GetUserGeneralInfos(){
        List<Integer> ids = new ArrayList<>();
        ids.add(1);
        ids.add(2);
        List<User> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);
        Mockito.when(userDao.getUserGroupByIdList(any(List.class))).thenReturn(users);
        List<UserGeneralDTO> newDtos = userService.getUserGeneralInfos(ids);
        assertNotNull(newDtos);
    }

}
