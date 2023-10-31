package com.ecommerce.project.controller;

import com.ecommerce.project.aws.service.StorageService;
import com.ecommerce.project.entity.*;
import com.ecommerce.project.repository.AddressRepository;
import com.ecommerce.project.repository.CartItemRepository;
import com.ecommerce.project.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class CartController {

    @Autowired
    ProductService productService;

    @Autowired
    UserService userService;

    @Autowired
    CartService cartService;

    @Autowired
    AddressService addressService;

    @Autowired
    CartItemRepository cartItemRepository;

    @Autowired
    AddressRepository addressRepository;

    @Autowired
    PaymentMethodService paymentMethodService;

    @Autowired
    StorageService storageService;

    @GetMapping("/addToCart/{id}")
    public String addToCart(@PathVariable long id, Principal principal, Model model) {

        //Already logged-in user block
        if (!userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).isEnabled()) {
            return "redirect:/logout";
        }

        User user = userService.findUserByEmail(principal.getName());

        Product product = productService.getProductById(id).orElse(null);

        Optional<CartItem> cartItemOptional = cartItemRepository.findCartItemByProductAndCart(product, user.getCart());

        //Check inventory if the product is in stock
        if (product.getQuantity()==0)
            return "redirect:/shop/viewproduct/" +id;
        if (cartItemOptional.isPresent())
            if (product.getQuantity()<=cartItemOptional.get().getQuantity())
                return "redirect:/shop/viewproduct/" +id;

        model.addAttribute("product", product);
        model.addAttribute("addedToCart", "Added to Cart!");

        boolean flag = user.getWishlist().getWishlistStatus(productService.getProductById(id).get());
        if (flag) {
            model.addAttribute("ondu", "Product is there in wishlist");
        } else {
            model.addAttribute("illa", "Product is not there in wishlist");
        }

        // Add the product to the user's cart
        cartService.addProductToCart(user, product);

        if (cartItemOptional.isPresent()) {
            CartItem cartItem = cartItemOptional.get();
            model.addAttribute("quantityInCart", cartItem.getQuantity());
        } else {
            model.addAttribute("quantityInCart", 1);
        }

        //  Add cartCount
        int cartCount = userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).getCart().getCartItems().stream().map(x->x.getQuantity()).reduce(0,(a,b)->a+b);
        model.addAttribute("cartCount", cartCount);

        //Check inventory for stock availability and add the required model attributes accordingly
        Optional<CartItem> cartItemOptional2 = cartItemRepository.findCartItemByProductAndCart(product, user.getCart());
        if (product.getQuantity()==0)
            model.addAttribute("outOfStock", "OUT OF STOCK");
        else if (cartItemOptional2.isPresent())
            if (product.getQuantity()==cartItemOptional2.get().getQuantity())
                model.addAttribute("equalStock", "QUANTITY IN CART ALREADY EQUALS STOCK IN INVENTORY");
            else if (product.getQuantity()<cartItemOptional2.get().getQuantity())
                model.addAttribute("outOfStock", "OUT OF STOCK");
            else
                model.addAttribute("inStock", "IN STOCK");
        else
            model.addAttribute("inStock", "IN STOCK");

        model.addAttribute("urlList", storageService.getUrlListForSingleProduct(product));

        return "viewProduct";

    }

    @GetMapping("/removeFromCart/{id}")
    public String removeFromCart(@PathVariable long id, Principal principal, Model model) {

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
                model.addAttribute("removedFromCart", "Removed from Cart!");
                model.addAttribute("quantityInCart", cartItem.getQuantity());
            } else if (cartItem.getQuantity()==1) {
                cartItemRepository.delete(cartItem);
                model.addAttribute("quantityInCart", 0);
                model.addAttribute("removedFromCart", "Removed from Cart!");
            }
        } else {
            model.addAttribute("quantityInCart", 0);
        }


        model.addAttribute("product", product);

        boolean flag = user.getWishlist().getWishlistStatus(productService.getProductById(id).get());
        if (flag) {
            model.addAttribute("ondu", "Product is there in wishlist");
        } else {
            model.addAttribute("illa", "Product is not there in wishlist");
        }

        //  Add cartCount
        int cartCount = userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).getCart().getCartItems().stream().map(x->x.getQuantity()).reduce(0,(a,b)->a+b);
        model.addAttribute("cartCount", cartCount);

        //Check inventory for stock availability and add the required model attributes accordingly
        Optional<CartItem> cartItemOptional2 = cartItemRepository.findCartItemByProductAndCart(product, user.getCart());
        if (product.getQuantity()==0)
            model.addAttribute("outOfStock", "OUT OF STOCK");
        else if (cartItemOptional2.isPresent())
            if (product.getQuantity()==cartItemOptional2.get().getQuantity())
                model.addAttribute("equalStock", "QUANTITY IN CART ALREADY EQUALS STOCK IN INVENTORY");
            else if (product.getQuantity()<cartItemOptional2.get().getQuantity())
                model.addAttribute("outOfStock", "OUT OF STOCK");
            else
                model.addAttribute("inStock", "IN STOCK");
        else
            model.addAttribute("inStock", "IN STOCK");

        model.addAttribute("urlList", storageService.getUrlListForSingleProduct(product));

        return "viewProduct";

    }


    @GetMapping("/cart")
    public String getCart(Model model) {

        //Already logged-in user block
        if (!userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).isEnabled()) {
            return "redirect:/logout";
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findUserByEmail(username);

        int cartCount = user.getCart().getCartItems().stream().map(x->x.getQuantity()).reduce(0,(a,b)->a+b);
        model.addAttribute("cartCount", cartCount);

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
        model.addAttribute("total", total);

        Cart userCartEntity = user.getCart();
        List<CartItem> cartItemList = userCartEntity.getCartItems();

        model.addAttribute("cart", cartItemList);

        model.addAttribute("urlList", storageService.getUrlListForSingleCart(userCartEntity));

        return "cart";
    }

    @GetMapping("/cart/removeItem/{index}")
    public String cartItemRemove(@PathVariable int index, Principal principal) {

        //Already logged-in user block
        if (!userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).isEnabled()) {
            return "redirect:/logout";
        }

        User user = userService.findUserByEmail(principal.getName());
        Cart userCart = user.getCart();
        List<CartItem> cartItemList = userCart.getCartItems();
        CartItem cartItem = cartItemList.get(index);
        cartItemRepository.delete(cartItem);
        return "redirect:/cart";
    }

    @GetMapping("/checkout")
    public String checkout(Model model, Principal principal) {

        //Already logged-in user block
        if (!userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).isEnabled()) {
            return "redirect:/logout";
        }

        User user = userService.findUserByEmail(principal.getName());
        List<Address> userAddresses = addressService.getAddressesForUser(user);

        Cart userCartEntity = user.getCart();
        List<CartItem> cartItemList = userCartEntity.getCartItems();
        if (cartItemList.isEmpty())
            return "redirect:/cart";
        for (CartItem cartItem : cartItemList) {
            if (cartItem.getQuantity()>cartItem.getProduct().getQuantity()) {
                return "redirect:/cart";
            }
        }

        model.addAttribute("cart", cartItemList);

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

        model.addAttribute("total", total);
        model.addAttribute("userAddresses", userAddresses);
        model.addAttribute("paymentMethods", paymentMethodService.getAllPaymentMethods());

        //  Add cartCount
        int cartCount = userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).getCart().getCartItems().stream().map(x->x.getQuantity()).reduce(0,(a,b)->a+b);
        model.addAttribute("cartCount", cartCount);

        return "checkoutNew";
    }

    @PostMapping("/add-address-checkout")
    public String addAddress(@ModelAttribute("address") Address address) {

        //Already logged-in user block
        if (!userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).isEnabled()) {
            return "redirect:/logout";
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        User user = userService.findUserByEmail(userEmail);

        Address newAddress = new Address(user);
        newAddress.setStreetAddress(address.getStreetAddress());
        newAddress.setCity(address.getCity());
        newAddress.setState(address.getState());
        newAddress.setPostalCode(address.getPostalCode());
        newAddress.setCountry(address.getCountry());

        addressRepository.save(newAddress);

        return "redirect:/checkout";
    }

}
