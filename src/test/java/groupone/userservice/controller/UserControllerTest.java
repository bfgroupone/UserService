package groupone.userservice.controller;


import com.google.gson.Gson;
import groupone.userservice.dto.request.*;
import groupone.userservice.dto.response.DataResponse;
import groupone.userservice.dto.user.UserGeneralDTO;
import groupone.userservice.entity.User;
import groupone.userservice.exception.InvalidTypeAuthorization;
import groupone.userservice.security.AuthUserDetail;
import groupone.userservice.security.JwtProvider;
import groupone.userservice.security.LoginUserAuthentication;
import groupone.userservice.service.UserService;

import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.InvalidCredentialsException;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import org.springframework.mock.web.MockCookie;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import javax.persistence.criteria.CriteriaBuilder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest{

    @Value("${email.validation.token.key}")
    private String validationEmailKey;


    @MockBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;


    @MockBean
    private JwtProvider jwtProvider;

    private String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJlbWFpbDFAZW1haWwuY29tIiwicGVybWlzc2lvbnMiOlsicmVhZCIsImFkbWluX3JlYWQiLCJkZWxldGUiLCJiYW5fdW5iYW4iLCJyZWNvdmVyIiwicHJvbW90ZSJdLCJ1c2VySWQiOjEsImVtYWlsIjoiZW1haWwxQGVtYWlsLmNvbSIsImFjdGl2ZSI6dHJ1ZSwicm9sZSI6IlNVUEVSX0FETUlOIn0.K-IuHyVud5X-rgs0rch1V5dMgP7WcmHAoz6n6peYv10";

    String sDate1="7/12/2014";
    Date date1=new SimpleDateFormat("MM/dd/yyyy").parse(sDate1);
    String sDate2="4/21/2014";
    Date date2=new SimpleDateFormat("MM/dd/yyyy").parse(sDate2);
    private User user1 = new User(1,"john@example.com","John","Doe","123", date1, true, 1,"https://drive.google.com/file/d/1Ul78obBTS0zgaVOufCHpUKwMxBvDON-i/view", "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiZXhwIjoxNjg5ODkyMjc0fQ.bMc7kDmKL92H3DJleE7G7u9v0Y98KLDk4qPjPEbZdoo");
    private User user2 = new User(2,"jane@example.com","Jane","Smith","123", date2, true,2,"url-new","eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIzIiwiZXhwIjoxNjkwMDU3MzcwfQ.mWBG0g25cfv9K-rn-XuScThmUx4KEU04323-6kaZDZI");

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private AuthUserDetail authUserDetail;
//
//    @MockBean
//    private UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken;
//
//    @MockBean
//    private SecurityContextHolder securityContextHolder;

    public UserControllerTest() throws ParseException {
    }

    @Test
    void test_login() throws Exception{
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@test.com");
        loginRequest.setPassword("123");
        Authentication authentication = mock(Authentication.class);
        authentication.setAuthenticated(true);
        Mockito.when(authenticationManager.authenticate(any())).thenReturn(authentication);
        Mockito.when(authentication.isAuthenticated()).thenReturn(true);
//        Mockito.when(userService.loadUserByUsername(any(String.class))).thenReturn(dummy);
        Mockito.when(jwtProvider.createToken(any(AuthUserDetail.class))).thenReturn("123");
        Mockito.when(authentication.getPrincipal()).thenReturn(authUserDetail);
        Mockito.when(authUserDetail.getActive()).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andDo(MockMvcResultHandlers.print());

    }
    @Test
    void test_login_BannedUser() throws Exception{
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@test.com");
        loginRequest.setPassword("123");
        Authentication authentication = mock(Authentication.class);
        authentication.setAuthenticated(true);
        Mockito.when(authenticationManager.authenticate(any())).thenReturn(authentication);
        Mockito.when(authentication.isAuthenticated()).thenReturn(true);
//        Mockito.when(userService.loadUserByUsername(any(String.class))).thenReturn(dummy);
        Mockito.when(jwtProvider.createToken(any(AuthUserDetail.class))).thenReturn("123");
        Mockito.when(authentication.getPrincipal()).thenReturn(authUserDetail);
        Mockito.when(authUserDetail.getActive()).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User is banned, cannot login"))
                .andDo(MockMvcResultHandlers.print());

    }
    @Test
    void test_login_AuthenticationException() throws Exception{
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@test.com");
        loginRequest.setPassword("123");

        Mockito.when(authenticationManager.authenticate(any())).thenThrow(BadCredentialsException.class);

        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Incorrect credentials, please try again."))
                .andDo(MockMvcResultHandlers.print());

    }
    @Test
    void test_getAllUsersWhenEmpty() throws Exception {

        List<User> expected = new ArrayList<>();
        Mockito.when(userService.getAllUsers()).thenReturn(expected);
        mockMvc.perform(MockMvcRequestBuilders.get("/users")

                        )
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void test_getAllUser() throws Exception{
        List<User> expectedUsers = new ArrayList<>();

        expectedUsers.add(user1);
        expectedUsers.add(user2);
        System.out.println(expectedUsers.size());
        Mockito.when(userService.getAllUsers()).thenReturn(expectedUsers);
//        Mockito.doReturn(expectedUsers).when(userService.getAllUsers());

        DataResponse response = DataResponse.builder()
                .success(true)
                .data(expectedUsers)
                .message("Success")
                .build();
        mockMvc.perform(MockMvcRequestBuilders.get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"success\":true,\"message\":\"Success\",\"data\":[{\"id\":1,\"email\":\"john@example.com\",\"firstName\":\"John\"," +
                        "\"lastName\":\"Doe\",\"password\":\"123\",\"dateJoined\":\"2014-07-12T04:00:00.000+00:00\",\"type\":1," +
                        "\"profileImageURL\":\"https://drive.google.com/file/d/1Ul78obBTS0zgaVOufCHpUKwMxBvDON-i/view\"," +
                        "\"validationToken\":\"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiZXhwIjoxNjg5ODkyMjc0fQ.bMc7kDmKL92H3DJleE7G7u9v0Y98KLDk4qPjPEbZdoo\"}," +
                        "{\"id\":2,\"email\":\"jane@example.com\",\"firstName\":\"Jane\",\"lastName\":\"Smith\",\"password\":\"123\",\"dateJoined\":\"2014-04-21T04:00:00.000+00:00\"," +
                        "\"type\":2,\"profileImageURL\":\"url-new\",\"validationToken\":\"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIzIiwiZXhwIjoxNjkwMDU3MzcwfQ.mWBG0g25cfv9K-rn-XuScThmUx4KEU04323-6kaZDZI\"}]}"))
                .andDo(MockMvcResultHandlers.print());

    }

    @Test
    public void test_register() throws Exception{
        RegisterRequest request = new RegisterRequest("firstname", "lastname", "email@test.com", "password", "");
        String validToken ="testToken";
        int uid = 1;
        Mockito.when(userService.addUser(any(RegisterRequest.class))).thenReturn(uid);
        Mockito.when(userService.createValidationToken(any(User.class), any(String.class))).thenReturn(validToken);
        Mockito.when(userService.getUserById(1)).thenReturn(user1);
        mockMvc.perform(MockMvcRequestBuilders.post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new Gson().toJson(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Registered, please log in with your new account"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void test_register_throwException() throws Exception{
        RegisterRequest request = new RegisterRequest("firstname", "lastname", "email@test.com", "password", "");
        String validToken ="testToken";
        int uid = 1;
        Mockito.when(userService.addUser(any(RegisterRequest.class))).thenThrow(InvalidCredentialsException.class);
        Mockito.when(userService.createValidationToken(any(User.class), any(String.class))).thenReturn(validToken);
        Mockito.when(userService.getUserById(1)).thenReturn(user1);
        mockMvc.perform(MockMvcRequestBuilders.post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andDo(MockMvcResultHandlers.print());
    }
    @Test
    public void test_register_withoutToken() throws Exception{
        RegisterRequest request = new RegisterRequest("firstname", "lastname", "email@test.com", "password", "");
        String validToken ="testToken";
        User user = user1;
        user.setValidationToken("");
        int uid = 1;
        Mockito.when(userService.addUser(any(RegisterRequest.class))).thenReturn(uid);
        Mockito.when(userService.createValidationToken(any(User.class), any(String.class))).thenReturn(validToken);
        Mockito.when(userService.getUserById(1)).thenReturn(user);
        mockMvc.perform(MockMvcRequestBuilders.post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Registered, please log in with your new account"))
                .andDo(MockMvcResultHandlers.print());
    }
    @Test
    public void test_modifiedUserProfile() throws Exception {
        int userId = 4;
        UserPatchRequest userPatchRequest = new UserPatchRequest("update@email.com", "UpdatedFirstName", "UpdatedLastName", "123", "url");

        User modifiedUser = user2;
        modifiedUser.setEmail(userPatchRequest.getEmail());
        modifiedUser.setFirstName(userPatchRequest.getFirstName());
        modifiedUser.setLastName(userPatchRequest.getLastName());
        modifiedUser.setPassword(userPatchRequest.getPassword());
        modifiedUser.setProfileImageURL(userPatchRequest.getProfileImageURL());
        Mockito.when(userService.getUserById(userId)).thenReturn(user2);
        Mockito.when(userService.getAllUsers()).thenReturn(new ArrayList<>());
        Mockito.when(userService.updateUserProfile(any(UserPatchRequest.class), anyInt())).thenReturn(modifiedUser);

        mockMvc.perform(MockMvcRequestBuilders.patch("/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(userPatchRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.firstName").value(modifiedUser.getFirstName()))
                .andExpect(jsonPath("$.data.lastName").value(modifiedUser.getLastName()))
                .andExpect(jsonPath("$.data.email").value(modifiedUser.getEmail()))
                .andDo(MockMvcResultHandlers.print());
    }
    @Test
    public void test_modifiedUserProfile_SameEmailExistForOtherUser() throws Exception {
        int userId = 4;
        UserPatchRequest userPatchRequest = new UserPatchRequest("update@email.com", "UpdatedFirstName", "UpdatedLastName", "123", "url");
        List<User> users = new ArrayList<>();
        users.add(user2);
        User modifiedUser = new User();
        modifiedUser.setEmail(userPatchRequest.getEmail());
        modifiedUser.setFirstName(userPatchRequest.getFirstName());
        modifiedUser.setLastName(userPatchRequest.getLastName());
        modifiedUser.setPassword(userPatchRequest.getPassword());
        modifiedUser.setProfileImageURL(userPatchRequest.getProfileImageURL());
        users.add(modifiedUser);
        Mockito.when(userService.getUserById(userId)).thenReturn(user1);
        Mockito.when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(MockMvcRequestBuilders.patch("/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(userPatchRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andDo(MockMvcResultHandlers.print());
    }
    @Test
    public void test_modifiedUserActive() throws Exception {
        int userId = 5;
        boolean active = user1.isActive();
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        UsernamePasswordAuthenticationToken auth = mock(UsernamePasswordAuthenticationToken.class);
        User modifiedUser = user1;
        modifiedUser.setActive(!active);
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("read"));
        Mockito.when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(auth.getAuthorities()).thenReturn(authorities);
        Mockito.when(userService.updateUserActive(anyInt())).thenReturn(modifiedUser);


        mockMvc.perform(MockMvcRequestBuilders.patch("/users/" + userId + "/active"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.active").value(!active))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void test_UpdateUserActive_InvalidType() throws Exception {
        int userId = 5;
        boolean active = false;
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        UsernamePasswordAuthenticationToken auth = mock(UsernamePasswordAuthenticationToken.class);
        User modifiedUser = user1;
        modifiedUser.setActive(active);
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("read"));
        Mockito.when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(auth.getAuthorities()).thenReturn(authorities);
        Mockito.when(userService.updateUserActive(anyInt())).thenThrow(InvalidTypeAuthorization.class);


        mockMvc.perform(MockMvcRequestBuilders.patch("/users/" + userId + "/active"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andDo(MockMvcResultHandlers.print());
    }
    @Test
    public void test_PromoteUser() throws Exception {
        int userId = 5;
        int userType = 1;

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        UsernamePasswordAuthenticationToken auth = mock(UsernamePasswordAuthenticationToken.class);

        User modifiedUser = user1;
        modifiedUser.setType(userType);
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("read"));
        Mockito.when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(auth.getAuthorities()).thenReturn(authorities);
        Mockito.when(userService.promoteUser(anyInt())).thenReturn(modifiedUser);

        mockMvc.perform(MockMvcRequestBuilders.patch("/users/" + userId + "/" + "promote"))

                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.type").value(userType))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void test_PromoteUser_InvalidType() throws Exception {
        int userId = 5;
        int userType = 1;

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        UsernamePasswordAuthenticationToken auth = mock(UsernamePasswordAuthenticationToken.class);
        User modifiedUser = user1;
        modifiedUser.setType(userType);
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("read"));
        Mockito.when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(auth.getAuthorities()).thenReturn(authorities);
        Mockito.when(userService.promoteUser(anyInt())).thenThrow(InvalidTypeAuthorization.class);

        mockMvc.perform(MockMvcRequestBuilders.patch("/users/" + userId + "/promote"))

                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andDo(MockMvcResultHandlers.print());
    }
    @Test
    public void test_getUserById() throws Exception {
        int userId = 1;
        User user = user1;

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("admin_read"));

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        LoginUserAuthentication auth = new LoginUserAuthentication(userId, authorities, true, user.getEmail());
        Mockito.when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
//        Mockito.when(auth.getAuthorities()).thenReturn();


        Mockito.when(userService.getUserById(userId)).thenReturn(user);

        mockMvc.perform(MockMvcRequestBuilders.get("/user")
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.firstName").value(user.getFirstName()))
                .andExpect(jsonPath("$.data.lastName").value(user.getLastName()))
                .andExpect(jsonPath("$.data.email").value(user.getEmail()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void test_getUserByIdGeneral() throws Exception {
        int userId = 1;
        UserGeneralDTO dto = new UserGeneralDTO();
        dto.setUserId(1);
        dto.setProfileImageURL(user1.getProfileImageURL());
        dto.setFirstName(user1.getFirstName());
        dto.setLastName(user1.getLastName());
        Mockito.when(userService.getUserGeneralInfo(userId)).thenReturn(dto);

        mockMvc.perform(MockMvcRequestBuilders.get("/user/" + userId + "/general"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.firstName").value(dto.getFirstName()))
                .andExpect(jsonPath("$.data.lastName").value(dto.getLastName()))
                .andExpect(jsonPath("$.data.profileImageURL").value(dto.getProfileImageURL()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void test_getUsersByIdsGeneral() throws Exception {
        GeneralInfoRequest request = new GeneralInfoRequest();
        List<Integer> Ids = new ArrayList<>();
        Ids.add(1);
        Ids.add(2);
        request.setUserIdList(Ids);
        List<UserGeneralDTO> dtos = new ArrayList<>();
        UserGeneralDTO dto = new UserGeneralDTO();
        dto.setUserId(1);
        dto.setProfileImageURL(user1.getProfileImageURL());
        dto.setFirstName(user1.getFirstName());
        dto.setLastName(user1.getLastName());
        dtos.add(dto);
//        request.setUserIdList(Ids);
        UserGeneralDTO dto2 = new UserGeneralDTO(2, user2.getFirstName(), user2.getLastName(), user2.getProfileImageURL());
        dtos.add(dto2);
        Mockito.when(userService.getUserGeneralInfos(Ids)).thenReturn(dtos);

        mockMvc.perform(MockMvcRequestBuilders.get("/users/general")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void test_getUserById_InvalidCredential() throws Exception {
        int userId = 1;
        User user = user1;

        List<GrantedAuthority> authorities = new ArrayList<>();

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        LoginUserAuthentication auth = new LoginUserAuthentication(2, authorities, true, user.getEmail());
        Mockito.when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(MockMvcRequestBuilders.get("/user")
                        .param("userId", String.valueOf(userId)))
//                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof InvalidCredentialsException))
                .andExpect(result -> assertEquals("No permission to check other users' profile", result.getResolvedException().getMessage()))
                .andDo(MockMvcResultHandlers.print());
    }
    @Test
    public void test_getUserById_NullUser() throws Exception {
        int userId = 1;
        User user = user1;

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("admin_read"));

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        LoginUserAuthentication auth = new LoginUserAuthentication(userId, authorities, true, user.getEmail());
        Mockito.when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
//        Mockito.when(auth.getAuthorities()).thenReturn();

        Mockito.when(userService.getUserById(userId)).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.get("/user")
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andDo(MockMvcResultHandlers.print());
    }
//    @Test
//    public void test_deleteUser() throws Exception {
//        int userId = 5;
//        User user = user2;
//        Mockito.doNothing().when(userService.deleteUser(user2));
//        mockMvc.perform(MockMvcRequestBuilders.delete("/user")
//                        .param("userId", String.valueOf(userId)))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.message").value("User deleted successfully"))
//                .andDo(MockMvcResultHandlers.print());
//
//    }



    @Test
    public void test_logout() throws Exception {
//        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("random", "stuff");
//        request.setSession(session);
        MockCookie cookie = new MockCookie("random", "stuff");
//        request.setCookies(cookie);

        mockMvc.perform(MockMvcRequestBuilders.post("/logout").with(request -> {
            request.setSession(session);
            request.setCookies(cookie);
            return request;
                }))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Successfully logout!"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void test_createEmailToken() throws Exception {
        CreateValidationEmailRequest request = new CreateValidationEmailRequest();
        request.setUserId(user1.getId());
        User user = user1;

        String validationToken = "tokendummy";
        user.setValidationToken(validationToken);
        Mockito.when(userService.getUserById(anyInt())).thenReturn(user);
        Mockito.when(userService.validateEmailToken(validationToken, true, validationEmailKey)).thenReturn(true);
        Mockito.when(userService.createValidationToken(any(User.class),  any(String.class))).thenReturn(validationToken);

        mockMvc.perform(MockMvcRequestBuilders.post("/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("New validation email sent"))
                .andExpect(jsonPath("$.data").value(validationToken))
                .andDo(MockMvcResultHandlers.print());
    }
    @Test
    public void test_createEmailToken_NoToken() throws Exception {
        CreateValidationEmailRequest request = new CreateValidationEmailRequest();
        request.setUserId(user1.getId());
        User user = user1;

        String validationToken = "tokendummy";
        user.setValidationToken("");
        Mockito.when(userService.getUserById(anyInt())).thenReturn(user);
        Mockito.when(userService.createValidationToken(any(User.class),  any(String.class))).thenReturn(validationToken);

        mockMvc.perform(MockMvcRequestBuilders.post("/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("New validation email sent"))
                .andExpect(jsonPath("$.data").value(validationToken))
                .andDo(MockMvcResultHandlers.print());
    }
    @Test
    public void test_validateEmailToken() throws Exception {
        String token = "mockToken";
        Mockito.when(userService.validateEmailToken(any(String.class), any(boolean.class),  any(String.class))).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.get("/validate")
                        .param("token", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Token valid: true"))
                .andDo(MockMvcResultHandlers.print());
    }
}
