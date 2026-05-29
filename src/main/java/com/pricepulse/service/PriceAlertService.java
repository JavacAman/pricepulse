package com.pricepulse.service;

import com.pricepulse.entity.PriceAlert;
import com.pricepulse.entity.Product;
import com.pricepulse.entity.User;
import com.pricepulse.repository.PriceAlertRepository;
import com.pricepulse.repository.ProductRepository;
import com.pricepulse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PriceAlertService {

    private final PriceAlertRepository priceAlertRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public PriceAlert createAlert(Long userId,
                                  Long productId,
                                  Double targetPrice) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));

        PriceAlert alert = new PriceAlert();
        alert.setUser(user);
        alert.setProduct(product);
        alert.setTargetPrice(targetPrice);
        alert.setIsTriggered(false);

        return priceAlertRepository.save(alert);
    }

    public List<PriceAlert> getUserAlerts(Long userId) {
        return priceAlertRepository.findByUserId(userId);
    }

    public void deleteAlert(Long alertId) {
        priceAlertRepository.deleteById(alertId);
    }
}