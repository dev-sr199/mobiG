package com.ecommerce.project.controller;

import com.ecommerce.project.entity.*;
//import com.ecommerce.project.global.GlobalData;
import com.ecommerce.project.otp.dto.OtpDto;
import com.ecommerce.project.otp.entity.Otp;
import com.ecommerce.project.otp.repository.OtpRepository;
import com.ecommerce.project.repository.RoleRepository;
import com.ecommerce.project.repository.UserRepository;
import com.ecommerce.project.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class LoginController {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private OtpRepository otpRepository;

    @GetMapping("/access-denied")
    public String access() {
        return "access-denied";
    }

    @GetMapping("/login")
    public String login() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication==null || authentication instanceof AnonymousAuthenticationToken) {
            return "login";
        }

        return "redirect:/";

    }

    @GetMapping("/register")
    public String registerGet(Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication==null || authentication instanceof AnonymousAuthenticationToken) {
            model.addAttribute("user", new User());
            return "register";
        }

        return "redirect:/";

    }

    @PostMapping("/register")
    public String registerPost(
            @Valid @ModelAttribute("user") User user,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()){
            model.addAttribute("user", user);
            return "register";
        }

        // check the database if user already exists
        Optional<User> existing = userRepository.findUserByEmail(user.getEmail());

        if (existing.isPresent() && !existing.get().isVerified()) {
            Otp otpToDelete = otpRepository.findByUser_Id(existing.get().getId()).get();
            otpRepository.delete(otpToDelete);
            userRepository.deleteById(existing.get().getId());
        }

        if (existing.isPresent() && existing.get().isVerified()){
            model.addAttribute("user", user);
            model.addAttribute("registrationError", "Email already exists.");
            return "register";
        }

        String password = user.getPassword();
        user.setPassword(bCryptPasswordEncoder.encode(password));
        List<Role> roles = new ArrayList<>();
        roles.add(roleRepository.findById(2).get());
        user.setRoles(roles);
        user.setEnabled(true);

        Cart cart = new Cart();
        cart.setUser(user);
        user.setCart(cart);

        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        user.setWishlist(wishlist);

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        user.setWallet(wallet);
        userRepository.save(user);

        userService.register(user);

        OtpDto otpDto = new OtpDto();
        otpDto.setEmail(user.getEmail());
        model.addAttribute("otpDto", otpDto);

        return "registration-confirmation";

    }

    @PostMapping("/verify-account")
    public String postOtp(@ModelAttribute OtpDto otpDto, Model model) {

        int flag = userService.verifyAccount(otpDto.getEmail(), otpDto.getOtp());
        if (flag==1)
            return "user-verified";

        if (flag==2) {
            model.addAttribute("again", "Time's up. Resend OTP");
            User user = userService.findUserByEmail(otpDto.getEmail());
            model.addAttribute("user", user);
            return "registration-confirmation";
        }

        model.addAttribute("again", "Wrong OTP. Please try again.");
        User user = userService.findUserByEmail(otpDto.getEmail());
        model.addAttribute("user", user);
        return "registration-confirmation";
    }

    @PostMapping("/resend-otp")
    public String resendOtp(@ModelAttribute OtpDto otpDto, Model model) {

        User user = userService.findUserByEmail(otpDto.getEmail());

        //Get the user's existing OTP
        Otp otpToDelete = otpRepository.findByUser_Id(user.getId()).get();

        //Check if the OTP's time has not expired
        if (Duration.between(otpToDelete.getOtpGeneratedTime(), LocalDateTime.now()).getSeconds() <= (2 * 60) ) {
            long timeLeft = (2 * 60) + 1 - Duration.between(otpToDelete.getOtpGeneratedTime(), LocalDateTime.now()).getSeconds();
            long minutesLeft = timeLeft/60;
            long secondsLeft = timeLeft%60;
            model.addAttribute("again", "Resend OTP in " + minutesLeft + " minutes " + secondsLeft + " seconds");
            model.addAttribute("user", user);
            return "registration-confirmation";
        }

        //Delete the already existing otp entry
        otpRepository.delete(otpToDelete);

        //Send the new otp and save the new otp to database
        userService.register(user);

        model.addAttribute("again", "New OTP has been sent");
        model.addAttribute("user", user);
        return "registration-confirmation";
    }

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {

        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);

        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

}
