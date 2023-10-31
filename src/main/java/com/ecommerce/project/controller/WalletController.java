package com.ecommerce.project.controller;

import com.ecommerce.project.entity.TransactionDetails;
import com.ecommerce.project.entity.User;
import com.ecommerce.project.entity.Wallet;
import com.ecommerce.project.service.OrderService;
import com.ecommerce.project.service.UserService;
import com.ecommerce.project.service.WalletService;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;

@Controller
public class WalletController {

    @Autowired
    UserService userService;

    @Autowired
    WalletService walletService;

    @Autowired
    OrderService orderService;

    @GetMapping("/wallet")
    public String getWallet(Model model, Principal principal) {

        //Already logged-in user block
        if (!userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).isEnabled()) {
            return "redirect:/logout";
        }

        User user = userService.findUserByEmail(principal.getName());
        Wallet wallet = user.getWallet();

        model.addAttribute("wallet", wallet);

        //  Add cartCount
        int cartCount = userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).getCart().getCartItems().stream().map(x->x.getQuantity()).reduce(0,(a,b)->a+b);
        model.addAttribute("cartCount", cartCount);

        return "wallet";
    }

    @PostMapping("/addAmountToWallet")
    public String addAmountToWallet(@ModelAttribute("amount") double amount, Model model) {

        //Already logged-in user block
        if (!userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).isEnabled()) {
            return "redirect:/logout";
        }

        TransactionDetails transactionDetails = orderService.createTransaction(amount);
        model.addAttribute("amount", amount*100);
        model.addAttribute("orderId", transactionDetails.getOrderId());

        return "xxx2";
    }

    @PostMapping("/addedToWallet")
    public String addedToWallet(@ModelAttribute("razorpay_payment_id") String id,
                                Model model,
                                Principal principal) throws RazorpayException {

        //Already logged-in user block
        if (!userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).isEnabled()) {
            return "redirect:/logout";
        }

        Wallet wallet = userService.findUserByEmail(principal.getName()).getWallet();

        //Fetch the razorpay payment by its id
        RazorpayClient razorpayClient = new RazorpayClient("rzp_test_amAJ6g1mhBlQKL", "xW9gfY6xByn88aKq8GixUNZ0");
        Payment payment = razorpayClient.payments.fetch(id);
        int amountInt = payment.get("amount");
        double amount = (double) amountInt /100;

        wallet.setAmount(wallet.getAmount() + amount);
        walletService.save(wallet);
        model.addAttribute("wallet", wallet);
        model.addAttribute("addedToWallet", "The amount was successfully added to the wallet");

        //  Add cartCount
        int cartCount = userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).getCart().getCartItems().stream().map(x->x.getQuantity()).reduce(0,(a,b)->a+b);
        model.addAttribute("cartCount", cartCount);

        return "wallet";
    }


}
