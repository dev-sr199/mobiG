package com.ecommerce.project.aws.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.ecommerce.project.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class StorageService {

    @Value("${application.bucket.name}")
    private String bucketName;

    @Autowired
    private AmazonS3 s3Client;

    public String uploadFile(MultipartFile file) {
        File fileObj = convertMultipartFileToFile(file);
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        s3Client.putObject(new PutObjectRequest(bucketName, fileName, fileObj));
        fileObj.delete();
        return fileName;
    }

    private File convertMultipartFileToFile(MultipartFile file) {
        File convertedFile = new File(file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convertedFile)){
            fos.write(file.getBytes());
        } catch (IOException e) {
            log.error("Error converting MultipartFile to File", e);
        }
        return convertedFile;
    }

    public String generateS3ObjectUrl(String imageKey) {
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, imageKey);
        URL s3ImageUrl = s3Client.generatePresignedUrl(request);
        return s3ImageUrl.toString();
    }

    public List<String> getUrlList(List<Product> products) {
        List<String> urlList = new ArrayList<>();
        for (Product product : products) {
            String url = generateS3ObjectUrl(product.getImageName());
            urlList.add(url);
        }
        return urlList;
    }

    public List<String> getUrlListForSingleProduct(Product product) {
        List<String> urlList = new ArrayList<>();
        urlList.add(generateS3ObjectUrl(product.getImageName()));
        for (ProductImage productImage : product.getProductImages()) {
            urlList.add(generateS3ObjectUrl(productImage.getImageName()));
        }
        return urlList;
    }

    public List<String> getUrlListForSingleOrder(Order order) {
        List<String> urlList = new ArrayList<>();
        for (OrderItem orderItem : order.getOrderItems()) {
            urlList.add(generateS3ObjectUrl(orderItem.getProduct().getImageName()));
        }
        return urlList;
    }

    public List<String> getUrlListForSingleCart(Cart cart) {
        List<String> urlList = new ArrayList<>();
        for (CartItem cartItem : cart.getCartItems()) {
            urlList.add(generateS3ObjectUrl(cartItem.getProduct().getImageName()));
        }
        return urlList;
    }

    public List<String> getUrlListForSingleWishlist(Wishlist wishlist) {
        List<String> urlList = new ArrayList<>();
        for (WishlistItem wishlistItem : wishlist.getWishlistItems()) {
            urlList.add(generateS3ObjectUrl(wishlistItem.getProduct().getImageName()));
        }
        return urlList;
    }
}
