package com.ecommerce.project.service;

import com.ecommerce.project.entity.CustomUserDetails;
import com.ecommerce.project.entity.User;
import com.ecommerce.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<User> userOptional = userRepository.findUserByEmail(username);

        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
        } else {
            user = null;
        }

        if (user == null) {
            throw new UsernameNotFoundException("Invalid username or password.");
        }

        if (!user.isVerified()) {
            throw new DisabledException("User account is not verified");
        }

        return userOptional.map(CustomUserDetails::new).get();

    }

}
