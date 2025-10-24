
package com.example.nom035.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.access.annotation.Secured;

import com.example.nom035.entity.User;
import com.example.nom035.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping
    @Secured("ROLE_ADMIN")
    public List<User> getAllUsers() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("Principal: " + authentication.getPrincipal());
            System.out.println("Authorities: ");
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                System.out.println(" - " + authority.getAuthority());
            }
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    @Secured("ROLE_ADMIN")
    public Optional<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PostMapping
    @Secured("ROLE_ADMIN")
    public User createUser(@RequestBody User user) {
        return userService.saveUser(user);
    }

    @PutMapping("/{id}")
    @Secured("ROLE_ADMIN")
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        return userService.saveUser(user);
    }

    @DeleteMapping("/{id}")
    @Secured("ROLE_ADMIN")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
