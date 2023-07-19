package groupone.userservice.controller;

import groupone.userservice.dto.request.LoginRequest;
import groupone.userservice.dto.request.RegisterRequest;
import groupone.userservice.dto.request.UserRegistrationRequest;
import groupone.userservice.dto.response.DataResponse;
import groupone.userservice.entity.User;
import groupone.userservice.security.AuthUserDetail;
import groupone.userservice.security.JwtProvider;
import groupone.userservice.service.UserService;
import groupone.userservice.util.SerializeUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping()
public class UserController {

    private UserService userService;
    private AuthenticationManager authenticationManager;

    private JwtProvider jwtProvider;
    private RabbitTemplate rabbitTemplate;

    @Autowired
    public UserController(UserService userService, AuthenticationManager authenticationManager, JwtProvider jwtProvider, RabbitTemplate rabbitTemplate) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtProvider = jwtProvider;
        this.rabbitTemplate = rabbitTemplate;
    }

    @GetMapping("/users")
    public ResponseEntity<DataResponse> getAllUsers() {
        List<User> data = userService.getAllUsers();
        for (User u : data) System.out.println(u.getFirstName());
        DataResponse res = DataResponse.builder()
                .success(true)
                .message("Success")
                .data(data)
                .build();
        return ResponseEntity.ok(res);
    }


    @PostMapping("/login")
    public ResponseEntity<DataResponse> login(@RequestBody LoginRequest request) {

        Authentication authentication;

        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Incorrect credentials, please try again.");
        }

        AuthUserDetail authUserDetail = (AuthUserDetail) authentication.getPrincipal();

        String token = jwtProvider.createToken(authUserDetail);

        return new ResponseEntity<>(
                DataResponse.builder()
                        .message("Welcome " + authUserDetail.getUsername())
                        .data(token)
                        .build(), HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<DataResponse> register(@RequestBody RegisterRequest request) throws DataIntegrityViolationException {
//        if (bindingResult.hasErrors()) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        userService.addUser(request.getFirstName(), request.getLastName(), request.getEmail(), request.getPassword(), "https://drive.google.com/file/d/1Ul78obBTS0zgaVOufCHpUKwMxBvDON-i/view");

        //        TODO: only if user is successfully added
        UserRegistrationRequest registrationRequest = UserRegistrationRequest.builder()
                .recipient(request.getEmail())
                .subject("test")
                .msgBody("test")
                .build();

        String jsonMessage = SerializeUtil.serialize(registrationRequest);

        rabbitTemplate.convertAndSend("x.user-registration", "send-email", jsonMessage);

        return new ResponseEntity<>(
                DataResponse.builder()
                        .message("Registered, please log in with your new account")
                        .build(), HttpStatus.OK);
    }

    @GetMapping("/user")
    public ResponseEntity<DataResponse> getUserById(@RequestParam("userId") Integer userId) {
        User exist = userService.getUserById(userId);
        if(exist == null){
            DataResponse response = DataResponse.builder()
                    .success(false)
                    .message("User not found")
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.ok(DataResponse.builder()
                .success(true)
                .message("Get user by id Success")
                .data(exist)
                .build());
    }
    @DeleteMapping("/user")
    public ResponseEntity<DataResponse> deleteUesr(@RequestParam("userId") Integer userId){
        User existingUser = userService.getUserById(userId);
        if (existingUser == null){
            DataResponse response = DataResponse.builder()
                    .success(false)
                    .message("User not found")
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        userService.deleteUser(existingUser);
        DataResponse response = DataResponse.builder()
                .success(true)
                .message("User deleted successfully")
                .build();
        return ResponseEntity.ok(response);
    }


}