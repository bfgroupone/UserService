package groupone.userservice.controller;

import groupone.userservice.dto.request.UserCreationRequest;
import groupone.userservice.dto.response.AllHistoryResponse;
import groupone.userservice.dto.response.DataResponse;
import groupone.userservice.entity.History;
import groupone.userservice.entity.User;
import groupone.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping()
public class UserController {

    private UserService userService;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

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

    @GetMapping("/history/{id}")
    public ResponseEntity<AllHistoryResponse> getHistoryByUid(@PathVariable int id){
        List<History> data =  userService.getHistoryByUid(id);
        for(History h: data) System.out.println(h.getId());
        AllHistoryResponse res = AllHistoryResponse.builder()
                .historylist(data)
                .build();
        return ResponseEntity.ok(res);
    }


}