package com.ecommerce.project.service;

import com.ecommerce.project.entity.User;
import com.ecommerce.project.otp.entity.Otp;
import com.ecommerce.project.otp.repository.OtpRepository;
import com.ecommerce.project.otp.util.EmailUtil;
import com.ecommerce.project.otp.util.OtpUtil;
import com.ecommerce.project.repository.UserRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    OtpUtil otpUtil;

    @Autowired
    EmailUtil emailUtil;

    @Autowired
    OtpRepository otpRepository;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public void deleteById(Integer id) {
        userRepository.deleteById(id);
    }

    public User findById(Integer id) {
        User user = userRepository.findById(id).get();
        return user;
    }

    public void save(User user) {
        userRepository.save(user);
    }

    public User findUserByEmail(String name) {
        return userRepository.findUserByEmail(name).get();
    }

    public void register(User user) {

        String otp = otpUtil.generateOtp();

        try {
            emailUtil.sendOtpEmail(user.getEmail(), otp);
        } catch (MessagingException e) {
            throw new RuntimeException("Unable to send OTP. Please try again.");
        }

        Otp otp1 = new Otp();
        otp1.setOtp(otp);
        otp1.setOtpGeneratedTime(LocalDateTime.now());
        otp1.setUser(user);
        otpRepository.save(otp1);

    }

    public int verifyAccount(String email, String otp) {

        User user = userRepository.findUserByEmail(email).get();

        Otp otp1 = otpRepository.findByUser_Id(user.getId()).get();

        if (otp1.getOtp().equals(otp) &&
                Duration.between(otp1.getOtpGeneratedTime(), LocalDateTime.now()).getSeconds() <= (2 * 60) ) {
            user.setVerified(true);
            userRepository.save(user);
            otpRepository.delete(otp1);
            return 1;
        } else if (otp1.getOtp().equals(otp) &&
                Duration.between(otp1.getOtpGeneratedTime(), LocalDateTime.now()).getSeconds() > (2 * 60)) {
            return 2;
        }

        return 0;
    }
}
