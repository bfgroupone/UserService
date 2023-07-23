package groupone.userservice.controller;


import groupone.userservice.dao.UserDao;
import groupone.userservice.entity.User;
import groupone.userservice.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
public class UserControllerTest{

    @MockBean
    private UserService userService;
    @Mock
    private UserDao userDao;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void test_getAllUsers_successWhenEmpty() {
        List<User> expected = new ArrayList<>();
        Mockito.when(userDao.getAllUsers()).thenReturn(expected);
        assertEquals(expected, userDao.getAllUsers());
    }

//    @Test
//    @DisplayName("Test getAllUser()")
//    public void test_getAllUser() throws Exception{
//        List<User> expectedUsers = new ArrayList<>();
//        String sDate1="7/12/2014";
//        Date date1=new SimpleDateFormat("MM/dd/yyyy").parse(sDate1);
//        String sDate2="4/21/2014";
//        Date date2=new SimpleDateFormat("MM/dd/yyyy").parse(sDate2);
//        expectedUsers.add(new User(1,"john@example.com","John","Doe","123", date1, 1, "https://drive.google.com/file/d/1Ul78obBTS0zgaVOufCHpUKwMxBvDON-i/view", "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiZXhwIjoxNjg5ODkyMjc0fQ.bMc7kDmKL92H3DJleE7G7u9v0Y98KLDk4qPjPEbZdoo"));
//        expectedUsers.add(new User(2,"jane@example.com","Jane","Smith","123", date2,2,"url-new","eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIzIiwiZXhwIjoxNjkwMDU3MzcwfQ.mWBG0g25cfv9K-rn-XuScThmUx4KEU04323-6kaZDZI"));
//        System.out.println(expectedUsers.size());
////        Mockito.when(userService.getAllUsers()).thenReturn(expectedUsers);
//        Mockito.doReturn(expectedUsers).when(userService.getAllUsers());
//
//
//        mockMvc.perform(get("/users"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(content().json("[{\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john@example.com\"}, " +
//                        "{\"firstName\":\"Jane\",\"lastName\":\"Smith\",\"email\":\"jane@example.com\"}]"));
//    }
}
