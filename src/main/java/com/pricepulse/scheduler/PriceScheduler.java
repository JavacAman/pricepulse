package com.pricepulse.scheduler;

import com.pricepulse.entity.PriceAlert;
import com.pricepulse.entity.Product;
import com.pricepulse.repository.PriceAlertRepository;
import com.pricepulse.repository.ProductRepository;
import com.pricepulse.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PriceScheduler {

    private final PriceAlertRepository priceAlertRepository;
    private final ProductRepository productRepository;
    private final EmailService emailService;

    // Runs every 3 hours
    @Scheduled(fixedRate = 10000)
    public void checkPriceDrops() {
        log.info("Running price drop check...");

        List<Product> products = productRepository.findAll();

        for (Product product : products) {
            // Simulate price fluctuation ±10%
            double fluctuation = (Math.random() * 0.2) - 0.1;
            double newPrice = product.getCurrentPrice() * (1 + fluctuation);
            newPrice = Math.round(newPrice * 100.0) / 100.0;
            product.setCurrentPrice(newPrice);
            productRepository.save(product);

            log.info("Product: {} | New Price: {}",
                    product.getName(), newPrice);

            // Check alerts for this product
            List<PriceAlert> alerts = priceAlertRepository
                    .findByProductIdAndIsTriggeredFalse(product.getId());

            for (PriceAlert alert : alerts) {
                if (newPrice <= alert.getTargetPrice()) {
                    log.info("Price drop detected! Alerting: {}",
                            alert.getUser().getEmail());

                    // Send actual email now!
                    emailService.sendPriceDropAlert(
                            alert.getUser().getEmail(),
                            product.getName(),
                            alert.getTargetPrice(),
                            newPrice
                    );

                    alert.setIsTriggered(true);
                    priceAlertRepository.save(alert);
                }
            }
        }
        log.info("Price drop check completed.");
    }
}