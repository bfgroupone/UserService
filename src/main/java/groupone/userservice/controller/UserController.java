package groupone.userservice.controller;

import groupone.userservice.dto.request.*;
import groupone.userservice.dto.response.DataResponse;
import groupone.userservice.entity.User;
import groupone.userservice.exception.InvalidTypeAuthorization;
import groupone.userservice.security.AuthUserDetail;
import groupone.userservice.security.JwtProvider;
import groupone.userservice.security.LoginUserAuthentication;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
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
        DataResponse res = DataResponse.builder()
                .success(true)
                .message("Success")
                .data(data)
                .build();
        return ResponseEntity.ok(res);
    }


    @PostMapping("/login")
    public ResponseEntity<DataResponse> login(@RequestBody LoginRequest request) throws BadCredentialsException {
        Authentication authentication;

        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (AuthenticationException e) {
            return new ResponseEntity<>(
                    DataResponse.builder()
                            .success(false)
                            .message("Incorrect credentials, please try again.")
                            .build(), HttpStatus.OK);
        }

        AuthUserDetail authUserDetail = (AuthUserDetail) authentication.getPrincipal();
        String token = jwtProvider.createToken(authUserDetail);

        if (!authUserDetail.getActive()) {
            return new ResponseEntity<>(
                    DataResponse.builder()
                            .success(false)
                            .message("User is banned, cannot login")
                            .build(), HttpStatus.OK);
        }

        return new ResponseEntity<>(
                DataResponse.builder()
                        .success(true)
                        .message("Welcome " + authUserDetail.getUsername())
                        .data(token)
                        .build(), HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<DataResponse> register(@RequestBody RegisterRequest request) throws DataIntegrityViolationException, InvalidCredentialsException {
        try {
            String token = userService.addUser(request);

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
        } catch (InvalidCredentialsException e) {
            return new ResponseEntity<>(
                    DataResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build(), HttpStatus.OK);
        }
    }

    @PatchMapping("/users/{id}")
    public ResponseEntity<DataResponse> modifiedUserProfile(@RequestBody UserPatchRequest request, @PathVariable int id) {
        Optional<User> possibleDuplicationEmail = userService.getAllUsers().stream().filter(user -> user.getEmail().equals(request.getEmail())).findAny();
        if (possibleDuplicationEmail.isPresent()) {
            return new ResponseEntity<>(
                    DataResponse.builder()
                            .success(false)
                            .message("This email has already registered, please go to login.")
                            .build(), HttpStatus.OK);
        }

        User data = userService.updateUserProfile(request, id);
        if (!request.getEmail().equals("")) {
            String token = createValidationEmailToken(id);
            UserRegistrationRequest registrationRequest = UserRegistrationRequest.builder()
                    .recipient(request.getEmail())
                    .subject("Please click the link to validate your account")
                    .msgBody("http://localhost:8082/user-service/validate?token=" + token)
                    .build();

            String jsonMessage = SerializeUtil.serialize(registrationRequest);

            rabbitTemplate.convertAndSend("x.user-registration", "send-email", jsonMessage);

            System.out.println("send....");
        }

        DataResponse res = DataResponse.builder()
                .success(true)
                .data(data)
                .message("Successfully update user profile")
                .build();
        return ResponseEntity.ok(res);
    }

    @PatchMapping("/users/{id}/promote")
    public ResponseEntity<DataResponse> promoteUser(@PathVariable int id) {
        User data;
        try {
            data = userService.promoteUser(id);
        } catch (InvalidTypeAuthorization e) {
            DataResponse res = DataResponse.builder()
                    .message(e.getMessage())
                    .success(false)
                    .build();
            return ResponseEntity.badRequest().body(res);
        }
        DataResponse res = DataResponse.builder()
                .success(true)
                .message("Successfully promote user with ID: " + id)
                .data(data)
                .build();
        return ResponseEntity.ok(res);
    }

    @PatchMapping("/users/{id}/active")
    public ResponseEntity<DataResponse> updateUserActive(@PathVariable int id) {
        User data;
        try {
            data = userService.updateUserActive(id);
        } catch (InvalidTypeAuthorization e) {
            DataResponse res = DataResponse.builder()
                    .message(e.getMessage())
                    .success(false)
                    .build();
            return ResponseEntity.badRequest().body(res);
        }
        DataResponse res = DataResponse.builder()
                .success(true)
                .message("Successfully ban/unban user with ID: " + id)
                .data(data)
                .build();
        return ResponseEntity.ok(res);
    }

    @GetMapping("/user")
    public ResponseEntity<DataResponse> getUserById(@RequestParam("userId") Integer userId) throws InvalidCredentialsException {
        LoginUserAuthentication auth = (LoginUserAuthentication) SecurityContextHolder.getContext().getAuthentication();
        List<GrantedAuthority> authorities = (List<GrantedAuthority>) auth.getAuthorities();

        if (auth.getUserID() == userId || authorities.contains("admin_read")) {
            User user = userService.getUserById(userId);
            if (user == null) {
                DataResponse response = DataResponse.builder()
                        .success(false)
                        .message("User not found")
                        .build();
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            return ResponseEntity.ok(DataResponse.builder()
                    .success(true)
                    .message("Get user by id Success")
                    .data(user)
                    .build());
        } else {
            throw new InvalidCredentialsException("No permission to check other users' profile");
        }
    }

    @DeleteMapping("/user")
    public ResponseEntity<DataResponse> deleteUser(@RequestParam("userId") Integer userId) throws InvalidCredentialsException {
        LoginUserAuthentication auth = (LoginUserAuthentication) SecurityContextHolder.getContext().getAuthentication();
        List<GrantedAuthority> authorities = (List<GrantedAuthority>) auth.getAuthorities();

        if (!auth.getAuthorities().contains("admin_read")) {
            throw new InvalidCredentialsException("No permission to check other users' profile");
        }

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


    @PostMapping("/logout")
    public ResponseEntity<DataResponse> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        SecurityContextHolder.clearContext();
        session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        for (Cookie cookie : request.getCookies()) {
            cookie.setMaxAge(0);
        }
        return ResponseEntity.ok(DataResponse.builder()
                .success(true)
                .message("Successfully logout!")
                .build());
    }
}