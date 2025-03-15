package com.nimbleways.springboilerplate.services.implementations;

import java.time.LocalDate;
import java.util.Set;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;

@Service
@Slf4j
public class ProductService {

    @Autowired
    ProductRepository pr;

    @Autowired
    private OrderRepository or;

    @Autowired
    NotificationService ns;

    public Long processOrder(Long orderId) {
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
                            notifyDelay(leadTime, p);
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
                        handleSeasonalProduct(p);
                    }
                }
                case EXPIRABLE -> {
                    if (p.getAvailable() > 0 && p.getExpiryDate().isAfter(LocalDate.now())) {
                        p.setAvailable(p.getAvailable() - 1);
                        pr.save(p);
                    } else {
                        handleExpiredProduct(p);
                    }
                }
            }
        }
        return order.getId();
    }

    public void notifyDelay(int leadTime, Product p) {
        p.setLeadTime(leadTime);
        pr.save(p);
        ns.sendDelayNotification(leadTime, p.getName());
    }

    public void handleSeasonalProduct(Product p) {
        if (LocalDate.now().plusDays(p.getLeadTime()).isAfter(p.getSeasonEndDate())) {
            ns.sendOutOfStockNotification(p.getName());
            p.setAvailable(0);
            pr.save(p);
        } else if (p.getSeasonStartDate().isAfter(LocalDate.now())) {
            ns.sendOutOfStockNotification(p.getName());
            pr.save(p);
        } else {
            notifyDelay(p.getLeadTime(), p);
        }
    }

    public void handleExpiredProduct(Product p) {
        if (p.getAvailable() > 0 && p.getExpiryDate().isAfter(LocalDate.now())) {
            p.setAvailable(p.getAvailable() - 1);
            pr.save(p);
        } else {
            ns.sendExpirationNotification(p.getName(), p.getExpiryDate());
            p.setAvailable(0);
            pr.save(p);
        }
    }
}