package com.ecommerce.project.controller;

import com.ecommerce.project.aws.service.StorageService;
import com.ecommerce.project.entity.*;
import com.ecommerce.project.repository.CartItemRepository;
import com.ecommerce.project.repository.WishlistItemRepository;
import com.ecommerce.project.service.ProductService;
import com.ecommerce.project.service.UserService;
import com.ecommerce.project.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
public class WishlistController {

    @Autowired
    UserService userService;

    @Autowired
    ProductService productService;

    @Autowired
    WishlistService wishlistService;

    @Autowired
    WishlistItemRepository wishlistItemRepository;

    @Autowired
    CartItemRepository cartItemRepository;

    @Autowired
    StorageService storageService;

    @GetMapping("/wishlist")
    public String getWishlist(Model model) {

        //Already logged-in user block
        if (!userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).isEnabled()) {
            return "redirect:/logout";
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findUserByEmail(username);

        Wishlist wishlist = user.getWishlist();
        List<WishlistItem> wishlistItemList = wishlist.getWishlistItems();

        model.addAttribute("cart", wishlistItemList);
        model.addAttribute("wishlistCount", wishlistItemList.size());

        //  Add cartCount
        int cartCount = userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).getCart().getCartItems().stream().map(x->x.getQuantity()).reduce(0,(a,b)->a+b);
        model.addAttribute("cartCount", cartCount);

        model.addAttribute("urlList", storageService.getUrlListForSingleWishlist(wishlist));

        return "wishlist";
    }

    @GetMapping("/addToWishlist/{id}")
    public String addToWishlist(@PathVariable long id, Principal principal, Model model) {

        //Already logged-in user block
        if (!userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).isEnabled()) {
            return "redirect:/logout";
        }

        User user = userService.findUserByEmail(principal.getName());
        Product product = productService.getProductById(id).orElse(null);

        boolean flag = wishlistService.addProductToWishlist(user, product);

        if (!flag) {
            model.addAttribute("alreadyInWishlist", "Product is already in wishlist");
        } else {
            model.addAttribute("addedToWishlist", "Product added to wishlist");
        }

        Optional<CartItem> cartItemOptional =
                cartItemRepository.findCartItemByProductAndCart(product,user.getCart());

        if (cartItemOptional.isPresent()) {
            CartItem cartItem = cartItemOptional.get();
            model.addAttribute("quantityInCart", cartItem.getQuantity());
        } else {
            model.addAttribute("quantityInCart", 0);
        }


        boolean check = user.getWishlist().getWishlistStatus(productService.getProductById(id).get());
        if (check) {
            model.addAttribute("ondu", "Product is there in wishlist");
        } else {
            model.addAttribute("illa", "Product is not there in wishlist");
        }

        model.addAttribute("product", product);

        //  Add cartCount
        int cartCount = userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).getCart().getCartItems().stream().map(x->x.getQuantity()).reduce(0,(a,b)->a+b);
        model.addAttribute("cartCount", cartCount);

        model.addAttribute("urlList", storageService.getUrlListForSingleProduct(product));

        return "viewProduct";
    }

    @GetMapping("/removeFromWishlist/{id}")
    public String removeFromWishlist(@PathVariable long id, Principal principal, Model model) {

        //Already logged-in user block
        if (!userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).isEnabled()) {
            return "redirect:/logout";
        }

        User user = userService.findUserByEmail(principal.getName());
        Product product = productService.getProductById(id).orElse(null);

        Optional<WishlistItem> wishlistItemOptional = wishlistItemRepository.findWishlistItemByProductAndWishlist(product, user.getWishlist());

        if (wishlistItemOptional.isPresent()) {
            WishlistItem wishlistItem = wishlistItemOptional.get();
            wishlistItemRepository.delete(wishlistItem);
            model.addAttribute("deletedFromWishlist", "Product was deleted from wishlist");
        } else {
            model.addAttribute("notInWishlist", "Product is not in wishlist");
        }

        Optional<CartItem> cartItemOptional =
                cartItemRepository.findCartItemByProductAndCart(product,user.getCart());

        if (cartItemOptional.isPresent()) {
            CartItem cartItem = cartItemOptional.get();
            model.addAttribute("quantityInCart", cartItem.getQuantity());
        } else {
            model.addAttribute("quantityInCart", 0);
        }

        boolean flag = user.getWishlist().getWishlistStatus(productService.getProductById(id).get());
        if (flag) {
            model.addAttribute("ondu", "Product is there in wishlist");
        } else {
            model.addAttribute("illa", "Product is not there in wishlist");
        }

        model.addAttribute("product", product);

        //  Add cartCount
        int cartCount = userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).getCart().getCartItems().stream().map(x->x.getQuantity()).reduce(0,(a,b)->a+b);
        model.addAttribute("cartCount", cartCount);

        model.addAttribute("urlList", storageService.getUrlListForSingleProduct(product));

        return "viewProduct";
    }

    @GetMapping("/removeFromWishlistAtWishlist/{id}")
    public String removeFromWishlistAtWishlist(@PathVariable long id, Principal principal) {

        //Already logged-in user block
        if (!userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).isEnabled()) {
            return "redirect:/logout";
        }

        User user = userService.findUserByEmail(principal.getName());
        Product product = productService.getProductById(id).orElse(null);

        Optional<WishlistItem> wishlistItemOptional = wishlistItemRepository.findWishlistItemByProductAndWishlist(product, user.getWishlist());

        if (wishlistItemOptional.isPresent()) {
            WishlistItem wishlistItem = wishlistItemOptional.get();
            wishlistItemRepository.delete(wishlistItem);
        }

        return "redirect:/wishlist";
    }

}
