package groupone.userservice.controller;


import com.google.gson.Gson;
import groupone.userservice.dao.UserDao;
import groupone.userservice.dto.request.LoginRequest;
import groupone.userservice.dto.request.RegisterRequest;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import java.text.SimpleDateFormat;
import java.util.*;

import static javax.swing.UIManager.put;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest{

    @MockBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;


    @MockBean
    private JwtProvider jwtProvider;



    @MockBean
    private RabbitTemplate rabbitTemplate;


    @Test
    void test_Login() throws Exception{
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@test.com");
        loginRequest.setPassword("123");

        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(loginRequest)))
                .andExpect(status().isOk());

    }

    @Test
    void test_getAllUsersWhenEmpty() throws Exception {

        List<User> expected = new ArrayList<>();
        Mockito.when(userService.getAllUsers()).thenReturn(expected);
        mockMvc.perform(MockMvcRequestBuilders.get("/users")

                        )
                .andExpect(status().isOk()).andReturn();
    }

    @Test
    public void test_getAllUser() throws Exception{
        List<User> expectedUsers = new ArrayList<>();
        String sDate1="7/12/2014";
        Date date1=new SimpleDateFormat("MM/dd/yyyy").parse(sDate1);
        String sDate2="4/21/2014";
        Date date2=new SimpleDateFormat("MM/dd/yyyy").parse(sDate2);
        expectedUsers.add(new User(1,"john@example.com","John","Doe","123", date1, 1, "https://drive.google.com/file/d/1Ul78obBTS0zgaVOufCHpUKwMxBvDON-i/view", "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiZXhwIjoxNjg5ODkyMjc0fQ.bMc7kDmKL92H3DJleE7G7u9v0Y98KLDk4qPjPEbZdoo"));
        expectedUsers.add(new User(2,"jane@example.com","Jane","Smith","123", date2,2,"url-new","eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIzIiwiZXhwIjoxNjkwMDU3MzcwfQ.mWBG0g25cfv9K-rn-XuScThmUx4KEU04323-6kaZDZI"));
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
                        "\"type\":2,\"profileImageURL\":\"url-new\",\"validationToken\":\"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIzIiwiZXhwIjoxNjkwMDU3MzcwfQ.mWBG0g25cfv9K-rn-XuScThmUx4KEU04323-6kaZDZI\"}]}"));

    }

    @Test
    public void test_Register() throws Exception{
        RegisterRequest request = new RegisterRequest("firstname", "lastname", "email@test.com", "password", "");
        User newUser - new User(request.getFirstName(), request.getLastName(), request.getEmail(), request.getPassword(), request.getProfileImageURL());
    }

}
