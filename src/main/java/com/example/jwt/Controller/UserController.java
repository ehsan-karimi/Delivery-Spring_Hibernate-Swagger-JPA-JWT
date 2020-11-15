package com.example.jwt.Controller;

import com.example.jwt.Model.UserDto;
import com.example.jwt.Service.JwtUserDetailsService;
import com.example.jwt.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin()
public class UserController {

    @Autowired
    private UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/user/list", method = RequestMethod.GET)
    public ResponseEntity<?> getUsers() {
        return ResponseEntity.ok(userService.getUserList());
    }

    @RequestMapping(value = "/user/register", method = RequestMethod.POST)
    public ResponseEntity<?> saveUser(@RequestBody UserDto user) {
        return ResponseEntity.ok(userService.save(user));
    }
}

