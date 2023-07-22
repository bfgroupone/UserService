package groupone.userservice.controller;

import groupone.userservice.dto.request.*;
import groupone.userservice.dto.response.DataResponse;
import groupone.userservice.entity.User;
import groupone.userservice.exception.InvalidTypeAuthorization;
import groupone.userservice.security.AuthUserDetail;
import groupone.userservice.security.JwtProvider;
import groupone.userservice.service.UserService;
import groupone.userservice.util.SerializeUtil;
import org.apache.http.auth.InvalidCredentialsException;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping()
@CrossOrigin(origins = "*")
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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    DataResponse.builder()
                            .success(false)
                            .message("Incorrect credentials, please try again.")
                            .build());
        }

        AuthUserDetail authUserDetail = (AuthUserDetail) authentication.getPrincipal();

        String token = jwtProvider.createToken(authUserDetail);
        System.out.println("Bearer " + token);

        return new ResponseEntity<>(
                DataResponse.builder()
                        .success(true)
                        .message("Welcome " + authUserDetail.getUsername())
                        .data(token)
                        .build(), HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<DataResponse> register(@RequestBody RegisterRequest request) throws DataIntegrityViolationException {
//        if (bindingResult.hasErrors()) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        try {
            String token = userService.addUser(request.getFirstName(), request.getLastName(), request.getEmail(), request.getPassword(), "https://drive.google.com/file/d/1Ul78obBTS0zgaVOufCHpUKwMxBvDON-i/view");

            //        TODO: only if user is successfully added
            UserRegistrationRequest registrationRequest = UserRegistrationRequest.builder()
                    .recipient(request.getEmail())
                    .subject("Please click the link to validate your account")
                    .msgBody("http://localhost:8082/user-service/validate?token=" + token)
                    .build();

            String jsonMessage = SerializeUtil.serialize(registrationRequest);

            rabbitTemplate.convertAndSend("x.user-registration", "send-email", jsonMessage);

            return new ResponseEntity<>(
                    DataResponse.builder()
                            .success(true)
                            .message("Registered, please log in with your new account")
                            .build(), HttpStatus.OK);
        } catch (InvalidCredentialsException e){
            DataResponse response = DataResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

    }

    @PatchMapping("/users/{id}")
    public ResponseEntity<DataResponse> modifiedUserProfile(@RequestBody UserPatchRequest request, @PathVariable int id) {
        Optional<User> possibleDumplicationEmail =  userService.getAllUsers().stream().filter(user -> user.getEmail().equals(request.getEmail())).findAny();
        if(possibleDumplicationEmail.isPresent()) {
            return new ResponseEntity<>(
                    DataResponse.builder()
                            .success(false)
                            .message("This email has already registered, please go to login.")
                            .build(), HttpStatus.FORBIDDEN);
        }

        User data = userService.updateUserProfile(request, id);

        DataResponse res = DataResponse.builder()
                .success(true)
                .data(data)
                .build();
        return ResponseEntity.ok(res);

    }

    @PatchMapping("/users/{id}/{type}")
    public ResponseEntity<DataResponse> modifiedUserType(@PathVariable int id, @PathVariable int type) {
        try {
            User data = userService.updateUserType(id, type);

            DataResponse res = DataResponse.builder()
                    .data(data)
                    .build();
            return ResponseEntity.ok(res);
        } catch (InvalidTypeAuthorization e) {
            DataResponse res = DataResponse.builder()
                    .data("Cannot assign SUPER ADMIN type.")
                    .build();
            return ResponseEntity.badRequest().body(res);
        }
    }

    @GetMapping("/user")
    public ResponseEntity<DataResponse> getUserById(@RequestParam("userId") Integer userId) {
        User exist = userService.getUserById(userId);
        if (exist == null) {
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
    public ResponseEntity<DataResponse> deleteUser(@RequestParam("userId") Integer userId) {
        User existingUser = userService.getUserById(userId);
        if (existingUser == null) {
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


    @PostMapping("/validate")
    public ResponseEntity<DataResponse> createValidationEmailToken(@RequestBody CreateValidationEmailRequest request) {
        String token = createValidationEmailToken(request.getUserId());

        return ResponseEntity.ok(DataResponse.builder()
                .success(true)
                .message("New validation email sent")
                .data(token)
                .build());
    }

    private String createValidationEmailToken(int userId) {
        return userService.createValidationToken(userId);
    }

    @GetMapping("/validate")
    public ResponseEntity<DataResponse> validateEmailToken(@RequestParam("token") String token) {
        boolean isValid = userService.validateEmailToken(token, false);

        return ResponseEntity.ok(DataResponse.builder()
                .success(true)
                .message("Token valid: " + isValid)
                .build());
    }
}