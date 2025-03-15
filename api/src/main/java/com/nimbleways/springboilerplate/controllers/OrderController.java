package com.nimbleways.springboilerplate.controllers;

import com.nimbleways.springboilerplate.dto.product.ProcessOrderResponse;
import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.implementations.ProductService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@Slf4j
public class OrderController {
    @Autowired
    private ProductService ps;

    @Autowired
    private ProductRepository pr;

    @Autowired
    private OrderRepository or;

    @PostMapping("{orderId}/processOrder")
    @ResponseStatus(HttpStatus.OK)
    public ProcessOrderResponse processOrder(@PathVariable Long orderId) {
        Order order = or.findById(orderId).get();
        log.info("order={}", order);
        Set<Product> products = order.getItems();
        for (Product p : products) {
            switch (p.getType()) {
                case NORMAL -> {
                    if (p.getAvailable() > 0) {
                        p.setAvailable(p.getAvailable() - 1);
                        pr.save(p);
                    } else {
                        int leadTime = p.getLeadTime();
                        if (leadTime > 0) {
                            ps.notifyDelay(leadTime, p);
                        }
                    }
                }
                case SEASONAL -> {
                    // Add new season rules
                    if ((LocalDate.now().isAfter(p.getSeasonStartDate()) && LocalDate.now().isBefore(p.getSeasonEndDate())
                        && p.getAvailable() > 0)) {
                        p.setAvailable(p.getAvailable() - 1);
                        pr.save(p);
                    } else {
                        ps.handleSeasonalProduct(p);
                    }
                }
                case EXPIRABLE -> {
                    if (p.getAvailable() > 0 && p.getExpiryDate().isAfter(LocalDate.now())) {
                        p.setAvailable(p.getAvailable() - 1);
                        pr.save(p);
                    } else {
                        ps.handleExpiredProduct(p);
                    }
                }
            }
        }

        return new ProcessOrderResponse(order.getId());
    }
}
