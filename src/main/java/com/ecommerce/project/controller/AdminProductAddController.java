package com.ecommerce.project.controller;

import com.ecommerce.project.aws.service.StorageService;
import com.ecommerce.project.dto.ProductDTO;
import com.ecommerce.project.entity.Product;
import com.ecommerce.project.entity.ProductImage;
import com.ecommerce.project.repository.ProductImageRepository;
import com.ecommerce.project.service.CategoryService;
import com.ecommerce.project.service.ProductImageService;
import com.ecommerce.project.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Controller
public class AdminProductAddController {

    @Autowired
    CategoryService categoryService;

    @Autowired
    ProductService productService;

    @Autowired
    ProductImageService productImageService;

    @Autowired
    StorageService storageService;

    @PostMapping("/admin/products/add")
    public String productAddPost(@Valid @ModelAttribute("productDTO") ProductDTO productDTO,
                                 BindingResult bindingResult,
                                 @RequestParam("productImage") List<MultipartFile> files,
                                 RedirectAttributes redirectAttributes,
                                 Model model) throws IOException {

        if (bindingResult.hasErrors()) {
            model.addAttribute("productDTO", productDTO);
            model.addAttribute("categories", categoryService.getAllCategories());
            return "productsAdd";
        }

        if (files.size()==1 && files.get(0).isEmpty()) {
            redirectAttributes.addFlashAttribute("imageRequired", "Select at least 1 image");
            redirectAttributes.addFlashAttribute("productDTO", productDTO);
            return "redirect:/admin/products/add";
        }

        if (productService.existsByName(productDTO.getName())) {
            redirectAttributes.addFlashAttribute("duplicateName", "Duplicate product name");
            redirectAttributes.addFlashAttribute("productDTO", productDTO);
            return "redirect:/admin/products/add";
        }

        Product product = new Product();
        product.setName(productDTO.getName());
        product.setCategory(categoryService.getCategoryById(productDTO.getCategoryId()).get());
        product.setPrice(productDTO.getPrice());
        product.setQuantity(productDTO.getQuantity());
        product.setDescription(productDTO.getDescription());

        String fileName = storageService.uploadFile(files.get(0));
        product.setImageName(fileName);

        List<ProductImage> productImageList = new ArrayList<>();

        for (int i=1; i<files.size();i++) {
            ProductImage productImage = new ProductImage();
            productImage.setProduct(product);
            fileName = storageService.uploadFile(files.get(i));
            productImage.setImageName(fileName);
            productImageList.add(productImage);
        }

        product.setProductImages(productImageList);
        productService.addProduct(product);
        redirectAttributes.addFlashAttribute("addOrUpdate", "Product was added successfully");
        return "redirect:/admin/products";
    }

    @PostMapping("/admin/products/update")
    public String productUpdatePost(@Valid @ModelAttribute("productDTO") ProductDTO productDTO,
                                 BindingResult bindingResult,
                                 @RequestParam("productImage") List<MultipartFile> files,
                                 RedirectAttributes redirectAttributes,
                                 Model model) throws IOException {

        if (bindingResult.hasErrors()) {
            model.addAttribute("productDTO", productDTO);
            model.addAttribute("categories", categoryService.getAllCategories());
            Product product = productService.getProductById(productDTO.getId()).get();
            model.addAttribute("product",product);
            model.addAttribute("urlList", storageService.getUrlListForSingleProduct(product));
            return "productsUpdate";
        }

        Product product = productService.getProductById(productDTO.getId()).get();

        if (!product.getName().equals(productDTO.getName())) {
            if (productService.existsByName(productDTO.getName())) {
                redirectAttributes.addFlashAttribute("duplicateName", "Duplicate product name");
                redirectAttributes.addFlashAttribute("productDTO", productDTO);
                return "redirect:/admin/product/update/" + productDTO.getId();
            }
        }

        product.setName(productDTO.getName());
        product.setCategory(categoryService.getCategoryById(productDTO.getCategoryId()).get());
        product.setPrice(productDTO.getPrice());
        product.setQuantity(productDTO.getQuantity());
        product.setDescription(productDTO.getDescription());

        if (!files.get(0).isEmpty()) {
            productImageService.deleteAllByProductId(productDTO.getId());
            String fileName = storageService.uploadFile(files.get(0));
            product.setImageName(fileName);

            List<ProductImage> productImageList = new ArrayList<>();

            for (int i=1; i<files.size();i++) {
                ProductImage productImage = new ProductImage();
                productImage.setProduct(product);
                fileName = storageService.uploadFile(files.get(i));
                productImage.setImageName(fileName);
                productImageList.add(productImage);
            }
            product.setProductImages(productImageList);
        }

        productService.addProduct(product);
        redirectAttributes.addFlashAttribute("addOrUpdate", "Product was updated successfully");
        return "redirect:/admin/products";
    }


    @GetMapping("/admin/product/update/{id}")
    public String updateProduct(@PathVariable long id,
                                @ModelAttribute("duplicateName") String duplicateName,
                                @ModelAttribute("productDTO") ProductDTO productDTOattr,
                                Model model) {

        Product product = productService.getProductById(id).get();

        if (duplicateName.isEmpty()) {
            ProductDTO productDTO = new ProductDTO();
            productDTO.setId(product.getId());
            productDTO.setName(product.getName());
            productDTO.setCategoryId(product.getCategory().getId());
            productDTO.setPrice(product.getPrice());
            productDTO.setQuantity(product.getQuantity());
            productDTO.setDescription(product.getDescription());

            model.addAttribute("urlList", storageService.getUrlListForSingleProduct(product));
            model.addAttribute("product", product);
            model.addAttribute("productDTO", productDTO);
            model.addAttribute("categories", categoryService.getAllCategories());

            return "productsUpdate";
        }

        model.addAttribute("urlList", storageService.getUrlListForSingleProduct(product));
        model.addAttribute("duplicateNameCheck", "duplicateNameCheck");
        model.addAttribute("product", product);
        model.addAttribute("productDTO", productDTOattr);
        model.addAttribute("categories", categoryService.getAllCategories());

        return "productsUpdate";

    }

    @GetMapping("/admin/products/add")
    public String addProduct(@ModelAttribute("imageRequired") String imageRequired,
                             @ModelAttribute("duplicateName") String duplicateName,
                             @ModelAttribute("productDTO") ProductDTO productDTO,
                             Model model) {

        if (!imageRequired.isEmpty())
            model.addAttribute("imageRequiredCheck", "imageRequiredCheck");

        if (!duplicateName.isEmpty())
            model.addAttribute("duplicateNameCheck", "duplicateNameCheck");

        model.addAttribute("categories", categoryService.getAllCategories());
        return "productsAdd";
    }

}
