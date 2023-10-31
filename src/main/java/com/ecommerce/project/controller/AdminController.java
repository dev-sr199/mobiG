package com.ecommerce.project.controller;

import com.ecommerce.project.aws.service.StorageService;
import com.ecommerce.project.dto.ProductDTO;
import com.ecommerce.project.entity.*;
import com.ecommerce.project.service.*;
import jakarta.validation.Valid;
import okhttp3.internal.http.RetryAndFollowUpInterceptor;
import org.aspectj.weaver.IClassFileProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.boot.Banner;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.model.IModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class AdminController {

    @Autowired
    CategoryService categoryService;

    @Autowired
    ProductService productService;

    @Autowired
    UserService userService;

    @Autowired
    OrderService orderService;

    @Autowired
    OrderStatusService orderStatusService;

    @Autowired
    CouponService couponService;

    @Autowired
    AdminDashboardService adminDashboardService;

    @Autowired
    ChartService chartService;

    @Autowired
    StorageService storageService;

    @GetMapping("/admin/coupons")
    public String getCoupons(Model model) {
        model.addAttribute("allCoupons", couponService.getAllCoupons());
        return "coupons";
    }

    @GetMapping("/admin/createCoupon")
    public String createCoupon(Model model) {

        model.addAttribute("coupon", new Coupon());
        return "createCoupon";
    }

    @PostMapping("/admin/createCoupon")
    public String postCoupon(@ModelAttribute("coupon") Coupon coupon, @ModelAttribute("status") int status,  Model model) {

        couponService.saveCoupon(coupon);
        if (status==1)
            model.addAttribute("couponCreated", "Coupon was created successfully");
        if (status==2)
            model.addAttribute("couponCreated", "Coupon was edited successfully");
        model.addAttribute("allCoupons", couponService.getAllCoupons());
        return "coupons";
    }

    @GetMapping("/admin/coupons/delete/{couponId}")
    public String deleteCoupon(@PathVariable("couponId") int couponId, Model model) {
        couponService.deleteCouponById(couponId);
        model.addAttribute("couponDeleted", "Coupon was deleted successfully");
        model.addAttribute("allCoupons", couponService.getAllCoupons());
        return "coupons";
    }

    @GetMapping("/admin/coupons/edit/{couponId}")
    public String editCoupon(@PathVariable("couponId") int couponId, Model model) {

        Coupon coupon = couponService.getCouponById(couponId).get();
        model.addAttribute("coupon", coupon);
        model.addAttribute("edit", "edit");
        return "createCoupon";
    }

    @GetMapping("/admin/categories")
    public String getCategories(@ModelAttribute("exception") String exception,
                                @ModelAttribute("deleted") String deleted,
                                Model model) {

        if (!exception.isEmpty())
            model.addAttribute("exceptionCheck", "Exception Check");
        if (!deleted.isEmpty())
            model.addAttribute("deletedCheck", "Deleted Check");
        model.addAttribute("categories", categoryService.getAllCategories());
        return "categories";
    }

    @PostMapping("/admin/categories")
    public String searchCategories(@ModelAttribute("name") String name, Model model) {

        List<Category> categoryList = categoryService.getAllCategories();

        List<Category> searchResult = new ArrayList<>();

        for (Category category : categoryList) {
            if (category.getName().toLowerCase().contains(name.toLowerCase())) {
                searchResult.add(category);
            }
        }

        if (searchResult.isEmpty()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("searchError", "Category could not be found.");
            return "categories";
        }

        model.addAttribute("categories", searchResult);
        return "categories";

    }

    @GetMapping("/admin/categories/add")
    public String addCategories(Model model) {
        model.addAttribute("category", new Category());
        return "categoriesAdd";
    }

    @PostMapping("/admin/categories/add")
    public String postAddCategories(
            @Valid @ModelAttribute("category") Category category,
            BindingResult bindingResult,
            Model model
            ) {

        if (bindingResult.hasErrors()){
            return "categoriesAdd";
        }

        Optional<Category> existing = categoryService.getCategoryByName(category.getName());

        if (existing.isPresent()) {
            model.addAttribute("category", new Category());
            model.addAttribute("categoryError", "Category already exists.");
            return "categoriesAdd";
        }

        categoryService.addCategory(category);
        return "redirect:/admin/categories";
    }

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {

        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);

        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @GetMapping("/admin/categories/delete/{id}")
    public String deleteCategory(@PathVariable("id") int id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategoryById(id);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("exception", "Cannot delete this category as products under this category have been ordered.");
            return "redirect:/admin/categories";
        }
        redirectAttributes.addFlashAttribute("deleted", "Category and the products under it have been deleted.");
        return "redirect:/admin/categories";
    }

    @GetMapping("/admin/categories/update/{id}")
    public String updateCategory(@PathVariable int id, Model model) {
        Optional<Category> category = categoryService.getCategoryById(id);
        if (category.isPresent()) {
            model.addAttribute("category", category.get());
            return "categoriesAdd";
        }
        return "404";
    }

    @GetMapping("/admin/products")
    public String getProducts(@ModelAttribute("exception") String exception,
                              @ModelAttribute("deleted") String deleted,
                              @ModelAttribute("addOrUpdate") String addOrUpdate,
                              Model model) {

        if (!addOrUpdate.isEmpty())
            model.addAttribute("addOrUpdateCheck", "addOrUpdateCheck");
        if (!exception.isEmpty())
            model.addAttribute("exceptionCheck", "Exception Check");
        if (!deleted.isEmpty())
            model.addAttribute("deletedCheck", "Deleted Check");
        List<Product> products = productService.getAllProducts();
        model.addAttribute("urlList", storageService.getUrlList(products));
        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "products";
    }

    @GetMapping("/admin/products/filter/{id}")
    public String filterProductByCategory(@PathVariable("id") int id, Model model) {

        List<Product> products = productService.getAllProductsByCategoryId(id);
        model.addAttribute("urlList", storageService.getUrlList(products));
        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "products";
    }

    @PostMapping("/admin/products")
    public String searchProducts(@ModelAttribute("name") String name, Model model) {

        List<Product> productList = productService.getAllProducts();

        List<Product> searchResult = new ArrayList<>();

        for (Product product : productList) {
            if (product.getName().toLowerCase().contains(name.toLowerCase())) {
                searchResult.add(product);
            }
        }

        if (searchResult.isEmpty()) {
            model.addAttribute("products", productService.getAllProducts());
            model.addAttribute("searchError", "Your search did not match any products.");
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("urlList", storageService.getUrlList(productService.getAllProducts()));
            return "products";
        }

        model.addAttribute("urlList", storageService.getUrlList(searchResult));
        model.addAttribute("products", searchResult);
        model.addAttribute("categories", categoryService.getAllCategories());

        return "products";

    }

    @GetMapping("/admin/product/delete/{id}")
    public String deleteProduct(@PathVariable("id") long id, RedirectAttributes redirectAttributes) {
        try {
            productService.removeProductById(id);
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute("exception", "Cannot delete a product that has already been ordered.");
            return "redirect:/admin/products";
        }

        redirectAttributes.addFlashAttribute("deleted", "The product was deleted.");
        return "redirect:/admin/products";
    }
    @GetMapping("/admin/users")
    public String getUsers(Model model) {
        model.addAttribute("users", userService.findAll().stream().filter(User::isVerified).toList());
        return "users";
    }

    @GetMapping("/admin/users/delete/{id}")
    public String deleteUser(@PathVariable("id") Integer id) {
        userService.deleteById(id);
        return "redirect:/admin/users";
    }

    @GetMapping("/admin/users/enable/{id}")
    public String enableUser(@PathVariable("id") Integer id) {
        User user = userService.findById(id);
        user.setEnabled(true);
        userService.save(user);
        return "redirect:/admin/users";
    }

    @GetMapping("/admin/users/disable/{id}")
    public String disableUser(@PathVariable("id") Integer id) {
        User user = userService.findById(id);
        user.setEnabled(false);
        userService.save(user);
        return "redirect:/admin/users";
    }

    @GetMapping("/admin/orders")
    public String adminOrders(Model model) {
        List<Order> userOrders = orderService.getAllOrders();
        Collections.reverse(userOrders);
        model.addAttribute("userOrders", userOrders);
        return "ordersAdmin";
    }

    @GetMapping("/admin/orders/{orderId}")
    public String viewOrderDetails(@PathVariable Long orderId, Model model) {
        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            return "redirect:/admin/orders";
        }

        model.addAttribute("statuses", orderStatusService.findAll());
        model.addAttribute("order", order);

        //13-10-2023 coupon check
        List<OrderItem> orderItems = order.getOrderItems();
        double actualTotal=0.0;
        for (OrderItem orderItem : orderItems) {
            actualTotal += orderItem.getProduct().getPrice() * orderItem.getQuantity();
        }
        if (order.getTotalPrice()!=actualTotal)
            model.addAttribute("couponApplied", "Coupon Applied!");

        model.addAttribute("urlList", storageService.getUrlListForSingleOrder(order));

        return "orderDetailsAdmin";
    }

    @PostMapping("/admin/orders/{orderId}")
    public String orderStatus(
            @PathVariable("orderId") Long orderId,
            @ModelAttribute("status") int status,
            Model model) {

        OrderStatus orderStatus = orderStatusService.findById(status);

        Order order = orderService.getOrderById(orderId);
        order.setOrderStatus(orderStatus);
        orderService.saveOrder(order);

        model.addAttribute("statuses", orderStatusService.findAll());
        model.addAttribute("order", order);

        //13-10-2023 coupon check
        List<OrderItem> orderItems = order.getOrderItems();
        double actualTotal=0.0;
        for (OrderItem orderItem : orderItems) {
            actualTotal += orderItem.getProduct().getPrice() * orderItem.getQuantity();
        }
        if (order.getTotalPrice()!=actualTotal)
            model.addAttribute("couponApplied", "Coupon Applied!");

        model.addAttribute("urlList", storageService.getUrlListForSingleOrder(order));
        return "orderDetailsAdmin";
    }

    @GetMapping("/admin/inventory")
    public String getInventory(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("urlList", storageService.getUrlList(productService.getAllProducts()));
        return "inventory";
    }

    @GetMapping("/admin/inventory/update/{id}")
    public String updateStock(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id).get();
        model.addAttribute("product", product);
        return "updateStock";
    }

    @PostMapping("/admin/inventory/addStock/{id}")
    public String addStock(@PathVariable Long id, @ModelAttribute("addStock") long add, Model model) {
        Product product = productService.getProductById(id).get();
        product.setQuantity(product.getQuantity() + add);
        productService.addProduct(product);

        List<Product> products = new ArrayList<>();
        products.add(product);
        model.addAttribute("urlList", storageService.getUrlList(products));
        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("stockStatus", "Stock added successfully");
        return "inventory";
    }

    @PostMapping("/admin/inventory/removeStock/{id}")
    public String removeStock(@PathVariable Long id, @ModelAttribute("removeStock") long subtract, Model model) {

        Product product = productService.getProductById(id).get();

        if (product.getQuantity()<subtract) {
            List<Product> products = new ArrayList<>();
            products.add(product);
            model.addAttribute("products", products);
            model.addAttribute("urlList", storageService.getUrlList(products));
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("stockStatus", "Could not remove required stock due to insufficient stock in inventory");
            return "inventory";
        }

        product.setQuantity(product.getQuantity() - subtract);
        productService.addProduct(product);

        List<Product> products = new ArrayList<>();
        products.add(product);
        model.addAttribute("products", products);
        model.addAttribute("urlList", storageService.getUrlList(products));
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("stockStatus", "Stock removed successfully");
        return "inventory";
    }

    @PostMapping("/admin/inventory")
    public String searchInventory(@ModelAttribute("name") String name, Model model) {

        List<Product> productList = productService.getAllProducts();

        List<Product> searchResult = new ArrayList<>();

        for (Product product : productList) {
            if (product.getName().toLowerCase().contains(name.toLowerCase())) {
                searchResult.add(product);
            }
        }

        if (searchResult.isEmpty()) {
            model.addAttribute("products", productService.getAllProducts());
            model.addAttribute("searchError", "Your search did not match any products.");
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("urlList", storageService.getUrlList(productService.getAllProducts()));
            return "inventory";
        }

        model.addAttribute("products", searchResult);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("urlList", storageService.getUrlList(searchResult));

        return "inventory";

    }

    @GetMapping("/admin/inventory/filter/{id}")
    public String filterInventoryByCategory(@PathVariable("id") int id, Model model) {

        List<Product> products = productService.getAllProductsByCategoryId(id);
        model.addAttribute("products", products);
        model.addAttribute("urlList", storageService.getUrlList(products));
        model.addAttribute("categories", categoryService.getAllCategories());
        return "inventory";
    }

    @GetMapping("/admin/salesReport")
    public String salesReport(Model model) {

        List<Order> userOrders = orderService.getAllOrders();
        List<Order> filteredUserOrders = new ArrayList<>(userOrders.stream().filter(order -> order.getOrderStatus().getId() != 6).toList());
        Collections.reverse(filteredUserOrders);

        model.addAttribute("ALL", "ALL");

        model.addAttribute("userOrders", filteredUserOrders);
        model.addAttribute("orderFilter", "All orders");
        model.addAttribute("totalOrders", filteredUserOrders.size());
        model.addAttribute("totalSales", filteredUserOrders.stream().map(Order::getTotalPrice).reduce(0.0, Double::sum));
        return "salesReport";

    }

    @PostMapping("/admin/salesReport")
    public String filterSalesReport(@ModelAttribute("dateFilter") String dateFilter, Model model) {

        List<Order> userOrders = orderService.getAllOrders();
        List<Order> filteredUserOrders = new ArrayList<>(userOrders.stream().filter(order -> order.getOrderStatus().getId() != 6).toList());
        Collections.reverse(filteredUserOrders);

        List<LocalDate> localDateList = new ArrayList<>();
        List<Order> modelOrders = new ArrayList<>();

        for (Order order : filteredUserOrders) {
            LocalDate localDate = order.getOrderDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            localDateList.add(localDate);
        }

        LocalDate today = LocalDate.now();

        switch (dateFilter) {
            case "ALL" -> {
                return "redirect:/admin/salesReport";
            }
            case "DAILY" -> {
                for (int i=0; i<localDateList.size(); i++) {
                    LocalDate localDate = localDateList.get(i);
                    if (localDate.isEqual(today)) {
                        modelOrders.add(filteredUserOrders.get(i));
                    }
                }

                model.addAttribute("DAILY", "DAILY");

                model.addAttribute("userOrders", modelOrders);
                model.addAttribute("orderFilter", "Daily orders");
                model.addAttribute("totalOrders", modelOrders.size());
                model.addAttribute("totalSales", modelOrders.stream().map(Order::getTotalPrice).reduce(0.0, Double::sum));
                return "salesReport";
            }
            case "WEEKLY" -> {

                LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
                LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));

                for (int i=0; i<localDateList.size(); i++) {
                    LocalDate localDate = localDateList.get(i);
                    if (localDate.isAfter(startOfWeek.minusDays(1)) && localDate.isBefore(endOfWeek.plusDays(1))) {
                        modelOrders.add(filteredUserOrders.get(i));
                    }
                }

                model.addAttribute("WEEKLY", "WEEKLY");


                model.addAttribute("userOrders", modelOrders);
                model.addAttribute("orderFilter", "Weekly orders");
                model.addAttribute("totalOrders", modelOrders.size());
                model.addAttribute("totalSales", modelOrders.stream().map(Order::getTotalPrice).reduce(0.0, Double::sum));
                return "salesReport";

            }
            case "MONTHLY" -> {

                LocalDate startOfMonth = today.withDayOfMonth(1);
                LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());

                for (int i=0; i<localDateList.size(); i++) {
                    LocalDate localDate = localDateList.get(i);
                    if (localDate.isAfter(startOfMonth.minusDays(1)) && localDate.isBefore(endOfMonth.plusDays(1))) {
                        modelOrders.add(filteredUserOrders.get(i));
                    }
                }

                model.addAttribute("MONTHLY", "MONTHLY");

                model.addAttribute("userOrders", modelOrders);
                model.addAttribute("orderFilter", "Monthly orders");
                model.addAttribute("totalOrders", modelOrders.size());
                model.addAttribute("totalSales", modelOrders.stream().map(Order::getTotalPrice).reduce(0.0, Double::sum));
                return "salesReport";
            }
            case "YEARLY" -> {

                LocalDate startOfYear = today.withDayOfYear(1);
                LocalDate endOfYear = today.withDayOfYear(today.lengthOfYear());

                for (int i=0; i<localDateList.size(); i++) {
                    LocalDate localDate = localDateList.get(i);
                    if (localDate.isAfter(startOfYear.minusDays(1)) && localDate.isBefore(endOfYear.plusDays(1))) {
                        modelOrders.add(filteredUserOrders.get(i));
                    }
                }

                model.addAttribute("YEARLY", "YEARLY");

                model.addAttribute("userOrders", modelOrders);
                model.addAttribute("orderFilter", "Yearly orders");
                model.addAttribute("totalOrders", modelOrders.size());
                model.addAttribute("totalSales", modelOrders.stream().map(Order::getTotalPrice).reduce(0.0, Double::sum));
                return "salesReport";

            }
        }

        return "redirect:/admin/salesReport";
    }

    @GetMapping("/admin")
    public String adminHome(Model model) {

        List<Order> userOrders = adminDashboardService.getAllOrdersToDashboard();
        List<Order> filteredUserOrders = adminDashboardService.getAllNonCancelledOrdersInReverseOrderToDashboard();
        List<LocalDate> localDateList = adminDashboardService.getLocalDateList();

        //Daily orders
        List<Order> dailyOrders = adminDashboardService.getDailyOrders();
        model.addAttribute("dailyOrders", dailyOrders.size());
        model.addAttribute("dailySales", Math.round(dailyOrders.stream().map(Order::getTotalPrice).reduce(0.0, Double::sum) * 100.0)/100.0);

        //Weekly Orders
        List<Order> weeklyOrders = adminDashboardService.getWeeklyOrders();
        model.addAttribute("weeklyOrders", weeklyOrders.size());
        model.addAttribute("weeklySales", Math.round(weeklyOrders.stream().map(Order::getTotalPrice).reduce(0.0, Double::sum) * 100.0)/100.0);

        //Monthly orders
        List<Order> monthlyOrders = adminDashboardService.getMonthlyOrders();
        model.addAttribute("monthlyOrders", monthlyOrders.size());
        model.addAttribute("monthlySales", Math.round(monthlyOrders.stream().map(Order::getTotalPrice).reduce(0.0, Double::sum) * 100.0)/100.0);

        //Yearly Orders
        List<Order> yearlyOrders = adminDashboardService.getYearlyOrders();
        model.addAttribute("yearlyOrders", yearlyOrders.size());
        model.addAttribute("yearlySales", Math.round(yearlyOrders.stream().map(Order::getTotalPrice).reduce(0.0, Double::sum) * 100.0)/100.0);

        //All Time Orders
        model.addAttribute("allTimeOrders", filteredUserOrders.size());
        model.addAttribute("allTimeSales", Math.round(filteredUserOrders.stream().map(Order::getTotalPrice).reduce(0.0,Double::sum) * 100.0)/100.0);

        //Stat Update
        model.addAttribute("totalOrdersIncl", userOrders.size());
        model.addAttribute("totalOrders", filteredUserOrders.size());
        model.addAttribute("ordersPlaced", filteredUserOrders.stream().filter(x->x.getOrderStatus().getId()==1).toList().size());
        model.addAttribute("ordersPacked", filteredUserOrders.stream().filter(x->x.getOrderStatus().getId()==2).toList().size());
        model.addAttribute("ordersShipped", filteredUserOrders.stream().filter(x->x.getOrderStatus().getId()==3).toList().size());
        model.addAttribute("ordersInTransit", filteredUserOrders.stream().filter(x->x.getOrderStatus().getId()==4).toList().size());
        model.addAttribute("ordersDelivered", filteredUserOrders.stream().filter(x->x.getOrderStatus().getId()==5).toList().size());
        model.addAttribute("ordersCancelled", userOrders.stream().filter(x->x.getOrderStatus().getId()==6).toList().size());

        //Chart Weekly (Last 7 days)
        model.addAttribute("subtitleWeeklyOrders", "Orders weekly (last 7 days)");
        model.addAttribute("subtitleWeeklyRevenue", "Revenue weekly (last 7 days)");
        model.addAttribute("chartDataWeeklyOrders", chartService.getChartDataWeeklyOrders());
        model.addAttribute("chartDataWeeklyRevenue", chartService.getChartDataWeeklyRevenue());

        //Chart Monthly
        List<String> barColors = Arrays.asList("black","maroon","red","purple","fuchsia","green","lime","olive","yellow","navy","blue","teal","aqua",
                "blueviolet","brown","burlywood","cadetblue","chartreuse","chocolate","cornflowerblue","crimson","cyan","darkblue","darkcyan",
                "darkgoldenrod","darkgreen","darkmagenta","darkorange","darkslategrey","firebrick");

        model.addAttribute("chartData", chartService.getChartDataMonthlyOrders().get(0));
        model.addAttribute("chartData2", chartService.getChartDataMonthlyOrders().get(1));
        model.addAttribute("barColors", barColors);
        model.addAttribute("chartData3", chartService.getChartDataMonthlyRevenue().get(0));
        model.addAttribute("chartData4", chartService.getChartDataMonthlyRevenue().get(1));

        //Chart yearly
        model.addAttribute("graphData1", chartService.getChartDataYearlyOrders().get(0));
        model.addAttribute("graphData2", chartService.getChartDataYearlyOrders().get(1));
        model.addAttribute("graphData3", chartService.getChartDataYearlyRevenue().get(0));
        model.addAttribute("graphData4", chartService.getChartDataYearlyRevenue().get(1));

        //Chart last 24 hours
        model.addAttribute("dailyList", chartService.getChartDataDailyOrders());
        model.addAttribute("dailyList2", chartService.getChartDataDailyRevenue());

        //Chart last 5 years
        model.addAttribute("labelFive", chartService.getLastFiveYears().get(0));
        model.addAttribute("dataFive", chartService.getLastFiveYears().get(1));
        model.addAttribute("labelFive2", chartService.getLastFiveYears2().get(0));
        model.addAttribute("dataFive2", chartService.getLastFiveYears2().get(1));

        //line chart 1
        model.addAttribute("lh1", chartService.getLineChart().get(0));
        model.addAttribute("gh1", chartService.getLineChart().get(1));
        model.addAttribute("lh2", chartService.getLineChart2().get(0));
        model.addAttribute("gh2", chartService.getLineChart2().get(1));

        //line chart 2
        model.addAttribute("hal1", chartService.getLineChartDaily1().get(0));
        model.addAttribute("hal2", chartService.getLineChartDaily1().get(1));
        model.addAttribute("gal1", chartService.getLineChartDaily2().get(0));
        model.addAttribute("gal2", chartService.getLineChartDaily2().get(1));

        //pie chart 1
        model.addAttribute("pie1", chartService.getPieChart());
        model.addAttribute("pie2", chartService.getPieChart2());

        return "adminHome";
    }

    @GetMapping("/admin/wallets")
    public String getAdminWallets(Model model) {

        model.addAttribute("users", userService.findAll().stream().filter(x->x.getWallet()!=null).toList());

        return "walletsAdmin";
    }

}
















