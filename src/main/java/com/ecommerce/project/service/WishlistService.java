package com.ecommerce.project.service;

import com.ecommerce.project.entity.Product;
import com.ecommerce.project.entity.User;
import com.ecommerce.project.entity.Wishlist;
import com.ecommerce.project.entity.WishlistItem;
import com.ecommerce.project.repository.WishlistItemRepository;
import com.ecommerce.project.repository.WishlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class WishlistService {

    @Autowired
    WishlistRepository wishlistRepository;

    @Autowired
    WishlistItemRepository wishlistItemRepository;


    public boolean addProductToWishlist(User user, Product product) {

        Wishlist wishlist = user.getWishlist();
        if (wishlist==null) {
            wishlist = new Wishlist();
            wishlist.setUser(user);
            user.setWishlist(wishlist);
            wishlistRepository.save(wishlist);
        }

        Optional<WishlistItem> wishlistItemOptional =
                wishlistItemRepository.findWishlistItemByProductAndWishlist(product, wishlist);

        if (wishlistItemOptional.isEmpty()) {
            WishlistItem wishlistItem = new WishlistItem();
            wishlistItem.setProduct(product);
            wishlistItem.setWishlist(wishlist);
            wishlistItemRepository.save(wishlistItem);

            return true;
        } else {
            return false;
        }

    }

}
