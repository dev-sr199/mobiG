package com.ecommerce.project.controller;

import com.ecommerce.project.aws.service.StorageService;
import com.ecommerce.project.entity.Order;
import com.ecommerce.project.entity.OrderItem;
import com.ecommerce.project.entity.OrderStatus;
import com.ecommerce.project.entity.User;
import com.ecommerce.project.service.OrderService;
import com.ecommerce.project.service.OrderStatusService;
import com.ecommerce.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

@Controller
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderStatusService orderStatusService;

    @Autowired
    private StorageService storageService;

    @GetMapping("/orders")
    public String listOrders(Model model) {

        //Already logged-in user block
        if (!userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).isEnabled()) {
            return "redirect:/logout";
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByEmail(authentication.getName());

        List<Order> userOrders = orderService.getOrdersByUser(user);
        Collections.reverse(userOrders);
        model.addAttribute("userOrders", userOrders);

        //  Add cartCount
        int cartCount = userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).getCart().getCartItems().stream().map(x->x.getQuantity()).reduce(0,(a,b)->a+b);
        model.addAttribute("cartCount", cartCount);

        return "orders";
    }

    @GetMapping("/orders/{orderId}")
    public String viewOrderDetails(@PathVariable Long orderId,
                                   Model model,
                                   @ModelAttribute("moneyCredited") String moneyCredited,
                                   @ModelAttribute("bug") String bug) {

        //Already logged-in user block
        if (!userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).isEnabled()) {
            return "redirect:/logout";
        }

        if (!moneyCredited.isEmpty())
            model.addAttribute("moneyCreditedCheck", "Money Credited Check");

        if (!bug.isEmpty())
            model.addAttribute("invoiceDownload", "Invoice has been downloaded");

        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            return "redirect:/orders";
        }

        model.addAttribute("order", order);

        //  Add cartCount
        int cartCount = userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).getCart().getCartItems().stream().map(x->x.getQuantity()).reduce(0,(a,b)->a+b);
        model.addAttribute("cartCount", cartCount);

        //13-10-2023 coupon check
        List<OrderItem> orderItems = order.getOrderItems();
        double actualTotal=0.0;
        for (OrderItem orderItem : orderItems) {
            actualTotal += orderItem.getProduct().getPrice() * orderItem.getQuantity();
        }
        if (order.getTotalPrice()!=actualTotal)
            model.addAttribute("couponApplied", "Coupon Applied!");

        model.addAttribute("urlList", storageService.getUrlListForSingleOrder(order));

        return "orderDetails";
    }

    @GetMapping("/cancelOrder/{id}")
    public String cancelOrder(@PathVariable("id") Long orderId, Principal principal, RedirectAttributes redirectAttributes) {

        //Already logged-in user block
        if (!userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).isEnabled()) {
            return "redirect:/logout";
        }

        Order order = orderService.getOrderById(orderId);
        OrderStatus orderStatus = orderStatusService.findById(6);
        order.setOrderStatus(orderStatus);
        orderService.saveOrder(order);

        if (order.getPaymentMethod().getId()==3) {
            User user = userService.findUserByEmail(principal.getName());
            double backToWallet = order.getTotalPrice();
            user.getWallet().setAmount(user.getWallet().getAmount() + backToWallet);
            userService.save(user);
            redirectAttributes.addFlashAttribute("moneyCredited", "Invoice amount of ₹" + backToWallet + " has been credited back to your wallet");
        }

        return "redirect:/orders/" + orderId;
    }

}
