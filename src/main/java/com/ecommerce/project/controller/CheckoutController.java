package com.ecommerce.project.controller;

import com.ecommerce.project.entity.*;
import com.ecommerce.project.repository.CartItemRepository;
import com.ecommerce.project.service.*;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Controller
public class CheckoutController {

    @Autowired
    private ProductService productService;

    @Autowired
    private AddressService addressService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderStatusService orderStatusService;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private PaymentMethodService paymentMethodService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private CouponService couponService;


    @PostMapping("/checkout/razorpay")
    public String processOrder(@ModelAttribute("razorpay_payment_id") String id,
                                Model model,
                               Principal principal) throws RazorpayException {

        //Already logged-in user block
        if (!userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).isEnabled()) {
            return "redirect:/logout";
        }

        //Fetch the razorpay payment by its id
        RazorpayClient razorpayClient = new RazorpayClient("key", "secret");
        Payment payment = razorpayClient.payments.fetch(id);
        JSONObject notes = payment.get("notes");

        User user = userService.findUserByEmail(principal.getName());

        // Retrieve the selected address from the Razorpay payments response
        Address selectedAddress = addressService.getAddressById(notes.getLong("address"));

        // Retrieve the selected payment method from the Razorpay payments response
        PaymentMethod paymentMethod = paymentMethodService.getPaymentMethodById(notes.getInt("paymentMethod"));

        Cart userCartEntity = user.getCart();
        List<CartItem> cartItemList = userCartEntity.getCartItems();

        //Inventory management
        for (CartItem cartItem : cartItemList) {
            Product product = cartItem.getProduct();
            product.setQuantity(product.getQuantity() - cartItem.getQuantity());
            productService.addProduct(product);
        }

        int razorAmount = payment.get("amount");
        double totalPrice = (double) razorAmount /100.0;

        // Create and save the order
        Order order = new Order();
        order.setUser(user);
        order.setAddress(selectedAddress);
        order.setPaymentMethod(paymentMethod);

        Date date = new Date();
        TimeZone istTimeZone = TimeZone.getTimeZone("Asia/Kolkata");
        date.setTime(date.getTime() + istTimeZone.getRawOffset());
        order.setOrderDate(date);

        order.setTotalPrice(totalPrice);

        OrderStatus orderStatus = orderStatusService.findById(1);
        order.setOrderStatus(orderStatus);

        // Add products to the order
        for (CartItem cartItem : cartItemList) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setOrder(order);
            order.getOrderItems().add(orderItem);
        }

        // Save the order
        orderService.saveOrder(order);

        // Clear the user's cart in the database
        for (CartItem cartItem : cartItemList) {
            cartItemRepository.delete(cartItem);
        }

        model.addAttribute("successMessage", "Your order has been placed successfully!");

        //  Add cartCount
        model.addAttribute("cartCount", 0);

        return "checkoutConfirmation";
    }

    @PostMapping("/checkout/process2")
    public String processOrder2(@ModelAttribute("address") Long selectedAddressId,
                                @ModelAttribute("paymentMethod") int paymentMethodId,
                                @ModelAttribute("total") double total,
                                Model model,
                                RedirectAttributes redirectAttributes) {

        //Already logged-in user block
        if (!userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).isEnabled()) {
            return "redirect:/logout";
        }

        //Check the inventory for required stock
        User user = userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        Cart userCartEntity = user.getCart();
        List<CartItem> cartItemList = userCartEntity.getCartItems();
        for (CartItem cartItem : cartItemList) {
            if (cartItem.getQuantity()>cartItem.getProduct().getQuantity()) {
                return "redirect:/cart";
            }
        }

        //Check the payment method id for Cash On Delivery
        if (paymentMethodId == 1) {
            redirectAttributes.addFlashAttribute("address", selectedAddressId);
            redirectAttributes.addFlashAttribute("paymentMethod", paymentMethodId);
            redirectAttributes.addFlashAttribute("total", total);
            return "redirect:/checkout/COD";
        }

        //Check the payment method for Wallet
        if (paymentMethodId == 3) {
            redirectAttributes.addFlashAttribute("address", selectedAddressId);
            redirectAttributes.addFlashAttribute("paymentMethod", paymentMethodId);
            redirectAttributes.addFlashAttribute("total", total);
            return "redirect:/checkout/wallet";
        }

        //Create a Razorpay transaction(order) and get the response
        TransactionDetails transactionDetails = orderService.createTransaction(total);

        model.addAttribute("amount", total*100);
        model.addAttribute("orderId", transactionDetails.getOrderId());
        model.addAttribute("address", selectedAddressId);
        model.addAttribute("paymentMethod", paymentMethodId);

        return "xxx";
    }

    @GetMapping("/checkout/COD")
    public String COD(@ModelAttribute("address") Long selectedAddressId,
                      @ModelAttribute("paymentMethod") int paymentMethodId,
                      @ModelAttribute("total") double total,
                      Model model,
                      Principal principal) {

        //Already logged-in user block
        if (!userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).isEnabled()) {
            return "redirect:/logout";
        }

        User user = userService.findUserByEmail(principal.getName());
        Address selectedAddress = addressService.getAddressById(selectedAddressId);
        PaymentMethod paymentMethod = paymentMethodService.getPaymentMethodById(paymentMethodId);

        Cart userCartEntity = user.getCart();
        List<CartItem> cartItemList = userCartEntity.getCartItems();

        //Inventory management
        for (CartItem cartItem : cartItemList) {
            Product product = cartItem.getProduct();
            product.setQuantity(product.getQuantity() - cartItem.getQuantity());
            productService.addProduct(product);
        }

        // Create and save the order
        Order order = new Order();
        order.setUser(user);
        order.setAddress(selectedAddress);
        order.setPaymentMethod(paymentMethod);

        Date date = new Date();
        TimeZone istTimeZone = TimeZone.getTimeZone("Asia/Kolkata");
        date.setTime(date.getTime() + istTimeZone.getRawOffset());
        order.setOrderDate(date);

        order.setTotalPrice(total);

        OrderStatus orderStatus = orderStatusService.findById(1);
        order.setOrderStatus(orderStatus);

        // Add products to the order
        for (CartItem cartItem : cartItemList) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setOrder(order);
            order.getOrderItems().add(orderItem);
        }

        // Save the order
        orderService.saveOrder(order);

        // Clear the user's cart in the database
        for (CartItem cartItem : cartItemList) {
            cartItemRepository.delete(cartItem);
        }

        model.addAttribute("successMessage", "Your order has been placed successfully!");

        //  Add cartCount
        model.addAttribute("cartCount", 0);

        return "checkoutConfirmation";
    }

    @GetMapping("/checkout/wallet")
    public String checkoutWallet(@ModelAttribute("address") Long selectedAddressId,
                                 @ModelAttribute("paymentMethod") int paymentMethodId,
                                 @ModelAttribute("total") double total,
                                 Model model,
                                 Principal principal) {

        //Already logged-in user block
        if (!userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).isEnabled()) {
            return "redirect:/logout";
        }

        User user = userService.findUserByEmail(principal.getName());
        Wallet wallet = user.getWallet();
        if (wallet.getAmount() < total) {

            model.addAttribute("insufficient", "Insufficient funds in wallet. Try another payment method.");

            List<Address> userAddresses = addressService.getAddressesForUser(user);

            Cart userCartEntity = user.getCart();
            List<CartItem> cartItemList = userCartEntity.getCartItems();
            model.addAttribute("cart", cartItemList);

            model.addAttribute("total", total);

            model.addAttribute("userAddresses", userAddresses);

            model.addAttribute("paymentMethods", paymentMethodService.getAllPaymentMethods());

            //  Add cartCount
            int cartCount = userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).getCart().getCartItems().stream().map(x->x.getQuantity()).reduce(0,(a,b)->a+b);
            model.addAttribute("cartCount", cartCount);

            return "checkoutNew";
        }

        wallet.setAmount(wallet.getAmount() - total);
        walletService.save(wallet);

        Address selectedAddress = addressService.getAddressById(selectedAddressId);
        PaymentMethod paymentMethod = paymentMethodService.getPaymentMethodById(paymentMethodId);

        Cart userCartEntity = user.getCart();
        List<CartItem> cartItemList = userCartEntity.getCartItems();

        //Inventory management
        for (CartItem cartItem : cartItemList) {
            Product product = cartItem.getProduct();
            product.setQuantity(product.getQuantity() - cartItem.getQuantity());
            productService.addProduct(product);
        }

        // Create and save the order
        Order order = new Order();
        order.setUser(user);
        order.setAddress(selectedAddress);
        order.setPaymentMethod(paymentMethod);

        Date date = new Date();
        TimeZone istTimeZone = TimeZone.getTimeZone("Asia/Kolkata");
        date.setTime(date.getTime() + istTimeZone.getRawOffset());
        order.setOrderDate(date);

        order.setTotalPrice(total);

        OrderStatus orderStatus = orderStatusService.findById(1);
        order.setOrderStatus(orderStatus);

        // Add products to the order
        for (CartItem cartItem : cartItemList) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setOrder(order);
            order.getOrderItems().add(orderItem);
        }

        // Save the order
        orderService.saveOrder(order);

        // Clear the user's cart in the database
        for (CartItem cartItem : cartItemList) {
            cartItemRepository.delete(cartItem);
        }

        model.addAttribute("successMessage", "Your order has been placed successfully!");

        //  Add cartCount
        model.addAttribute("cartCount", 0);

        return "checkoutConfirmation";
    }

    @PostMapping("/checkout/applyCoupon")
    public String applyCoupon(@ModelAttribute("couponCode") String couponCode, Model model, Principal principal) {

        Optional<Coupon> couponOptional = couponService.getCouponByCouponCode(couponCode);

        User user = userService.findUserByEmail(principal.getName());
        List<Address> userAddresses = addressService.getAddressesForUser(user);

        Cart userCartEntity = user.getCart();
        List<CartItem> cartItemList = userCartEntity.getCartItems();

        double total = 0.0;
        if (user != null) {
            Cart userCart = user.getCart();
            if (userCart != null) {
                List<CartItem> cartItems = userCart.getCartItems();
                for (CartItem cartItem : cartItems) {
                    total += cartItem.getProduct().getPrice() * cartItem.getQuantity();
                }
            }
        }

        if (couponOptional.isEmpty()) {
            model.addAttribute("invalidCoupon", "Invalid coupon code");

            model.addAttribute("cart", cartItemList);

            model.addAttribute("total", total);

            model.addAttribute("userAddresses", userAddresses);

            model.addAttribute("paymentMethods", paymentMethodService.getAllPaymentMethods());

            //  Add cartCount
            int cartCount = userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).getCart().getCartItems().stream().map(x->x.getQuantity()).reduce(0,(a,b)->a+b);
            model.addAttribute("cartCount", cartCount);

            return "checkoutNew";
        }

        if (total < couponOptional.get().getMinimumPurchase()) {
            model.addAttribute("invalidCoupon", "This coupon is only valid for purchases of " + couponOptional.get().getMinimumPurchase() + " and above");

            model.addAttribute("cart", cartItemList);

            model.addAttribute("total", total);

            model.addAttribute("userAddresses", userAddresses);

            model.addAttribute("paymentMethods", paymentMethodService.getAllPaymentMethods());

            //  Add cartCount
            int cartCount = userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).getCart().getCartItems().stream().map(x->x.getQuantity()).reduce(0,(a,b)->a+b);
            model.addAttribute("cartCount", cartCount);

            return "checkoutNew";
        }

        if (couponOptional.get().getDiscountType().equals("ABSOLUTE")) {
            total = total - couponOptional.get().getDiscountValue();
            model.addAttribute("discountApplied", "You get a discount of ₹" + couponOptional.get().getDiscountValue());
        }

        if (couponOptional.get().getDiscountType().equals("PERCENTAGE")) {
            total = total - (couponOptional.get().getDiscountValue()/100*total);
            model.addAttribute("discountApplied", "You get a discount of " + couponOptional.get().getDiscountValue() + "%");
        }

        model.addAttribute("couponApplied", "Coupon applied");

        model.addAttribute("cart", cartItemList);

        model.addAttribute("total", total);

        model.addAttribute("userAddresses", userAddresses);

        model.addAttribute("paymentMethods", paymentMethodService.getAllPaymentMethods());

        //  Add cartCount
        int cartCount = userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).getCart().getCartItems().stream().map(x->x.getQuantity()).reduce(0,(a,b)->a+b);
        model.addAttribute("cartCount", cartCount);

        return "checkoutNew";

    }
}

























