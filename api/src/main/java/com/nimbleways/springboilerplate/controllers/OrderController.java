package com.nimbleways.springboilerplate.controllers;

import com.nimbleways.springboilerplate.dto.product.ProcessOrderResponse;
import com.nimbleways.springboilerplate.services.implementations.ProductService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("{orderId}/processOrder")
    public ResponseEntity<ProcessOrderResponse> processOrder(@PathVariable Long orderId) {
        try {
            Long id = ps.processOrder(orderId);
            return ResponseEntity.ok(new ProcessOrderResponse(id)); // Return success response
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(null); // Return error response
        }
    }
}
