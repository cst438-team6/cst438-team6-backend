package com.cst438.controller;

import com.cst438.domain.User;
import com.cst438.domain.UserRepository;
import com.cst438.dto.UserDTO;
import com.cst438.service.GradebookServiceProxy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.List;


@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    GradebookServiceProxy gradebookService;

    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * list all users
     */
    @GetMapping("/users")
    public List<UserDTO> findAllUsers() {

        List<User> users = userRepository.findAllByOrderByIdAsc();
        List<UserDTO> userDTO_list = new ArrayList<>();
        for (User u : users) {
            userDTO_list.add(new UserDTO(u.getId(), u.getName(), u.getEmail(), u.getType()));
        }
        return userDTO_list;
    }

    /**
     * Admin creates a user
     */
    @PostMapping("/users")
    public UserDTO createUser(@RequestBody UserDTO UserDTO) {
        User user = new User();
        user.setName(UserDTO.name());
        user.setEmail(UserDTO.email());

        // create password and encrypt it
        String password = UserDTO.name() + "2024";
        String enc_password = encoder.encode(password);
        user.setPassword(enc_password);

        user.setType(UserDTO.type());
        if (!UserDTO.type().equals("STUDENT") &&
                !UserDTO.type().equals("INSTRUCTOR") &&
                !UserDTO.type().equals("ADMIN")) {
            // invalid type
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid user type");
        }
        userRepository.save(user);
        UserDTO userDTO = new UserDTO(user.getId(), user.getName(), user.getEmail(), user.getType());
        gradebookService.addUser(userDTO);
        return userDTO;
    }

    /**
     * Admin update a user's name, email or type
     */
    @PutMapping("/users")
    public UserDTO updateUser(@RequestBody UserDTO UserDTO) {
        User user = userRepository.findById(UserDTO.id()).orElse(null);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "user id not found");
        }
        user.setName(UserDTO.name());
        user.setEmail(UserDTO.email());
        user.setType(UserDTO.type());
        if (!UserDTO.type().equals("STUDENT") &&
                !UserDTO.type().equals("INSTRUCTOR") &&
                !UserDTO.type().equals("ADMIN")) {
            // invalid type
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid user type");
        }
        userRepository.save(user);
        UserDTO userDTO = new UserDTO(user.getId(), user.getName(), user.getEmail(), user.getType());
        gradebookService.updateUser(userDTO);
        return userDTO;
    }

    /**
     * Admin delete a user
     */
    @DeleteMapping("/users/{id}")
    public void updateUser(@PathVariable("id") int id) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            userRepository.delete(user);
            gradebookService.deleteUser(id);
        }

    }

}
