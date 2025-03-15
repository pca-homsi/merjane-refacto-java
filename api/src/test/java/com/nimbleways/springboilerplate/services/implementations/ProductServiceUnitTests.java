package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@UnitTest
public class ProductServiceUnitTests {

    @Mock
    private NotificationService notificationService;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private OrderRepository orderRepository;
    @InjectMocks 
    private ProductService productService;

    @Test
    public void test() {
        // GIVEN
        Product product =new Product(null, 15, 0, ProductType.NORMAL, "RJ45 Cable", null, null, null);

        Mockito.when(productRepository.save(product)).thenReturn(product);

        // WHEN
        productService.notifyDelay(product.getLeadTime(), product);

        // THEN
        assertEquals(0, product.getAvailable());
        assertEquals(15, product.getLeadTime());
        Mockito.verify(productRepository, Mockito.times(1)).save(product);
        Mockito.verify(notificationService, Mockito.times(1)).sendDelayNotification(product.getLeadTime(), product.getName());
    }

    @Test
    public void shouldThrowExceptionWhenOrderNotFound() {
        // GIVEN
        Long orderId = 1L;
        Product product =new Product(null, 15, 0, ProductType.NORMAL, "RJ45 Cable", null, null, null);

        // WHEN
        Mockito.when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // THEN
        assertThrows(RuntimeException.class, () -> {
            productService.processOrder(orderId);
        });

        Mockito.verify(orderRepository, Mockito.times(1)).findById(orderId);
    }
}