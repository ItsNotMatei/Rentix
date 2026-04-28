package com.project.rentixoficial;


import com.project.rentixoficial.model.MyUser;
import com.project.rentixoficial.model.MyUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class RegistrationController {
    @Autowired
    private MyUserRepository myUserRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @PostMapping("/register/user")
    public MyUser createUser(@RequestBody MyUser user){
      user.setPassword(passwordEncoder.encode(user.getPassword()));
      return myUserRepository.save(user);
  }
}
