package com.ecommerce.project.controller;

import com.ecommerce.project.entity.CartItem;
import com.ecommerce.project.entity.Product;
import com.ecommerce.project.entity.User;
import com.ecommerce.project.repository.AddressRepository;
import com.ecommerce.project.repository.CartItemRepository;
import com.ecommerce.project.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.Principal;
import java.util.Optional;

@Controller
public class CartController2 {

    @Autowired
    ProductService productService;

    @Autowired
    UserService userService;

    @Autowired
    CartService cartService;

    @Autowired
    CartItemRepository cartItemRepository;

    @GetMapping("/addToCartFromCart/{id}")
    public String addToCartFromCart(@PathVariable long id, Principal principal) {

        //Already logged-in user block
        if (!userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).isEnabled()) {
            return "redirect:/logout";
        }

        User user = userService.findUserByEmail(principal.getName());

        Product product = productService.getProductById(id).orElse(null);

        Optional<CartItem> cartItemOptional = cartItemRepository.findCartItemByProductAndCart(product, user.getCart());

        //Check inventory if the product is in stock
        if (product.getQuantity()==0)
            return "redirect:/cart";
        if (cartItemOptional.isPresent())
            if (product.getQuantity()<=cartItemOptional.get().getQuantity())
                return "redirect:/cart";

        cartService.addProductToCart(user, product);

        return "redirect:/cart";

    }

    @GetMapping("/removeFromCartFromCart/{id}")
    public String removeFromCartFromCart(@PathVariable long id, Principal principal) {

        //Already logged-in user block
        if (!userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).isEnabled()) {
            return "redirect:/logout";
        }

        User user = userService.findUserByEmail(principal.getName());
        Product product = productService.getProductById(id).orElse(null);
        Optional<CartItem> cartItemOptional = cartItemRepository.findCartItemByProductAndCart(product, user.getCart());

        if (cartItemOptional.isPresent()) {
            CartItem cartItem = cartItemOptional.get();
            if (cartItem.getQuantity()>1) {
                cartItem.setQuantity(cartItem.getQuantity()-1);
                cartItemRepository.save(cartItem);
            } else if (cartItem.getQuantity()==1) {
                cartItemRepository.delete(cartItem);
            }
        }
        return "redirect:/cart";
    }
}
