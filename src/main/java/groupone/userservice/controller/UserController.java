package groupone.userservice.controller;

import groupone.userservice.dto.request.LoginRequest;
import groupone.userservice.dto.request.RegisterRequest;
import groupone.userservice.dto.response.AllHistoryResponse;
import groupone.userservice.dto.response.DataResponse;
import groupone.userservice.entity.History;
import groupone.userservice.entity.User;
import groupone.userservice.security.AuthUserDetail;
import groupone.userservice.security.JwtProvider;
import groupone.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping()
public class UserController {

    private UserService userService;
    private AuthenticationManager authenticationManager;

    private JwtProvider jwtProvider;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
    @Autowired
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {this.authenticationManager = authenticationManager;}

    @Autowired
    public void setJwtProvider(JwtProvider jwtProvider) {this.jwtProvider=jwtProvider;}
    @GetMapping("/users")
    public ResponseEntity<DataResponse> getAllUsers(){
        List<User> data =  userService.getAllUsers();
        for(User u: data) System.out.println(u.getFirstName());
        DataResponse res = DataResponse.builder()
                .success(true)
                .message("Success")
                .data(data)
                .build();
        return ResponseEntity.ok(res);
    }

    @GetMapping("/history")
    public ResponseEntity<AllHistoryResponse> getHistory(){
        List<History> data =  userService.getHistory();
//        for(History h: data) System.out.println(h.getId());
        AllHistoryResponse res = AllHistoryResponse.builder()
                .historylist(data)
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
    public ResponseEntity<DataResponse> register(@RequestBody RegisterRequest request ) throws DataIntegrityViolationException {
//        if (bindingResult.hasErrors()) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        userService.addUser(request.getFirstName(), request.getLastName(), request.getEmail(), request.getPassword(), "https://drive.google.com/file/d/1Ul78obBTS0zgaVOufCHpUKwMxBvDON-i/view");

        return new ResponseEntity<>(
                DataResponse.builder()
                        .message("Registered, please log in with your new account")
                        .build(), HttpStatus.OK);
    }



}