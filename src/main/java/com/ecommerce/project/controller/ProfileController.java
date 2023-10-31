package com.ecommerce.project.controller;

import com.ecommerce.project.entity.Address;
import com.ecommerce.project.entity.User;
import com.ecommerce.project.repository.AddressRepository;
import com.ecommerce.project.repository.UserRepository;
import com.ecommerce.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Optional;

@Controller
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private AddressRepository addressRepository;

    @GetMapping("/profile")
    public String getProfile(Model model) {

        //Already logged-in user block
        if (!userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).isEnabled()) {
            return "redirect:/logout";
        }

        //  Add cartCount
        int cartCount = userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).getCart().getCartItems().stream().map(x->x.getQuantity()).reduce(0,(a,b)->a+b);
        model.addAttribute("cartCount", cartCount);

        return "profile";
    }

    @GetMapping("/profileDetails")
    public String getProfileDetails(Model model, Principal principal) {

        //Already logged-in user block
        if (!userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).isEnabled()) {
            return "redirect:/logout";
        }

        //  Add cartCount
        int cartCount = userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).getCart().getCartItems().stream().map(x->x.getQuantity()).reduce(0,(a,b)->a+b);
        model.addAttribute("cartCount", cartCount);

        //Get the currently logged-in user
        User user = userService.findUserByEmail(principal.getName());
        model.addAttribute("user", user);

        return "profileDetails";
    }

    @GetMapping("/addresses")
    public String listAddresses(Model model) {

        //Already logged-in user block
        if (!userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).isEnabled()) {
            return "redirect:/logout";
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName(); // Get the user's email from authentication

        // Find the user by email and retrieve their addresses
        User user = userService.findUserByEmail(userEmail);
        model.addAttribute("userAddresses", user.getAddresses());

        //  Add cartCount
        int cartCount = userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).getCart().getCartItems().stream().map(x->x.getQuantity()).reduce(0,(a,b)->a+b);
        model.addAttribute("cartCount", cartCount);

        return "addresses";
    }

    @GetMapping("/add-address")
    public String showAddAddressForm(Model model) {

        //Already logged-in user block
        if (!userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).isEnabled()) {
            return "redirect:/logout";
        }

        model.addAttribute("address", new Address());

        //  Add cartCount
        int cartCount = userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).getCart().getCartItems().stream().map(x->x.getQuantity()).reduce(0,(a,b)->a+b);
        model.addAttribute("cartCount", cartCount);

        return "add-address";
    }

    @PostMapping("/add-address")
    public String addAddress(@ModelAttribute("address") Address address) {

        //Already logged-in user block
        if (!userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).isEnabled()) {
            return "redirect:/logout";
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        User user = userService.findUserByEmail(userEmail);

        Address newAddress = new Address(user); // Create a new Address with the User
        newAddress.setStreetAddress(address.getStreetAddress());
        newAddress.setCity(address.getCity());
        newAddress.setState(address.getState());
        newAddress.setPostalCode(address.getPostalCode());
        newAddress.setCountry(address.getCountry());

        addressRepository.save(newAddress);

        return "redirect:/addresses";
    }

    @GetMapping("/addresses/delete/{id}")
    public String deleteAddress(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        //Already logged-in user block
        if (!userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).isEnabled()) {
            return "redirect:/logout";
        }

        Optional<Address> addressOptional = addressRepository.findById(id);
        if (addressOptional.isPresent()) {
            try {
                addressRepository.deleteById(id);
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("cannotDelete", "This address is associated with an order and cannot be deleted");
                return "redirect:/addresses";
            }
        }

        return "redirect:/addresses";
    }

    @GetMapping("/addresses/edit/{id}")
    public String editAddress(@PathVariable Long id, Model model) {

        //Already logged-in user block
        if (!userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).isEnabled()) {
            return "redirect:/logout";
        }

        Optional<Address> addressOptional = addressRepository.findById(id);
        Address address = addressOptional.get();

        model.addAttribute("address", address);

        //  Add cartCount
        int cartCount = userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).getCart().getCartItems().stream().map(x->x.getQuantity()).reduce(0,(a,b)->a+b);
        model.addAttribute("cartCount", cartCount);

        return "edit-address";
    }

    @PostMapping("/edit-address/{id}")
    public String postEditAddress(@PathVariable Long id,
                                  @ModelAttribute Address addressFromForm) {

        //Already logged-in user block
        if (!userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).isEnabled()) {
            return "redirect:/logout";
        }

        Optional<Address> addressOptional = addressRepository.findById(id);
        Address address = addressOptional.get();

        addressFromForm.setId(address.getId());
        addressFromForm.setUser(address.getUser());

        addressRepository.save(addressFromForm);

        return "redirect:/addresses";
    }

}
