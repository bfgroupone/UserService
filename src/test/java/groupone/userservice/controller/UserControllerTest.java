package groupone.userservice.controller;


import com.google.gson.Gson;
import groupone.userservice.dao.UserDao;
import groupone.userservice.dto.request.CreateValidationEmailRequest;
import groupone.userservice.dto.request.LoginRequest;
import groupone.userservice.dto.request.RegisterRequest;
import groupone.userservice.dto.request.UserPatchRequest;
import groupone.userservice.dto.response.DataResponse;
import groupone.userservice.entity.User;
import groupone.userservice.security.JwtFilter;
import groupone.userservice.security.JwtProvider;
import groupone.userservice.service.UserService;

import io.vavr.collection.Multimap;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static javax.swing.UIManager.put;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest{

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

    public UserControllerTest() throws ParseException {
    }


    @Test
    void test_login() throws Exception{
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@test.com");
        loginRequest.setPassword("123");

        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(loginRequest)))
                .andExpect(status().isOk())
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
        String validToken ="eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMCIsImV4cCI6MTY4OTgzOTM5MH0.tw706SlQBnriZgLmTCkAh2t_c4WNooUhXjYOEL2_vNw";

        Mockito.when(userService.addUser(any(RegisterRequest.class))).thenReturn(validToken);

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
        Mockito.when(userService.updateUserProfile(any(UserPatchRequest.class), anyInt())).thenReturn(modifiedUser);

        mockMvc.perform(MockMvcRequestBuilders.patch("/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(userPatchRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.firstName").value(modifiedUser.getFirstName()))
                .andExpect(jsonPath("$.data.lastName").value(modifiedUser.getLastName()))
                .andExpect(jsonPath("$.data.email").value(modifiedUser.getEmail()))
                .andDo(MockMvcResultHandlers.print());
    }
    @Test
    public void test_modifiedUserActive() throws Exception {
        int userId = 5;
        boolean active = false;

        User modifiedUser = user1;
        modifiedUser.setActive(active);
        Mockito.when(userService.updateUserActive(anyInt(), any(List.class))).thenReturn(modifiedUser);

        mockMvc.perform(MockMvcRequestBuilders.patch("/users/" + userId + "/active"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.data.userType").value(userType))
                .andDo(MockMvcResultHandlers.print());
    }
    @Test
    public void test_modifiedUserType() throws Exception {
        int userId = 5;
        int userType = 1;

        User modifiedUser = user1;
        modifiedUser.setType(userType);
        Mockito.when(userService.updateUserType(anyInt(), anyInt(), any(List.class))).thenReturn(modifiedUser);

        mockMvc.perform(MockMvcRequestBuilders.patch("/users/" + userId + "/" + userType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.data.userType").value(userType))
                .andDo(MockMvcResultHandlers.print());
    }
    @Test
    public void test_getUserById() throws Exception {
        int userId = 1;
        User user = user1;
        Mockito.when(userService.getUserById(userId)).thenReturn(user);

        mockMvc.perform(MockMvcRequestBuilders.get("/user")
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.firstName").value(user.getFirstName()))
                .andExpect(jsonPath("$.data.lastName").value(user.getLastName()))
                .andExpect(jsonPath("$.data.email").value(user.getEmail()))
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

        mockMvc.perform(MockMvcRequestBuilders.post("/logout").header("authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.message").value("Successfully logout!"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void test_createEmailToken() throws Exception {
        CreateValidationEmailRequest request = new CreateValidationEmailRequest();
        request.setUserId(user1.getId());
        String validationToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiZXhwIjoxNjkwMTY3NTAzfQ.HXHqQ2_SFffAh1iz8gRY-54SZSnCPYbcUG8hWGM6ZA0";
        Mockito.when(userService.createValidationToken(anyInt())).thenReturn(validationToken);

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
        Mockito.when(userService.validateEmailToken(any(String.class), any(boolean.class))).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.get("/validate")
                        .param("token", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Token valid: true"))
                .andDo(MockMvcResultHandlers.print());
    }
}
