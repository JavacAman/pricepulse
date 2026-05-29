package com.pricepulse.scheduler;

import com.pricepulse.entity.PriceAlert;
import com.pricepulse.entity.Product;
import com.pricepulse.repository.PriceAlertRepository;
import com.pricepulse.repository.ProductRepository;
import com.pricepulse.service.EmailService;
import com.pricepulse.strategy.PriceUpdateStrategy;
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
    private final PriceUpdateStrategy priceUpdateStrategy;

    @Scheduled(fixedRate = 10000)
    public void checkPriceDrops() {
        log.info("Running price drop check...");

        List<Product> products = productRepository.findAll();

        for (Product product : products) {
            .roduct.getCurrentPrice());
            product.setCurrentPrice(newPrice);
            productRepository.save(product);

            log.info("Product: {} | New Price: {}",
                    product.getName(), newPrice);

            List<PriceAlert> alerts = priceAlertRepository
                    .findByProductIdAndIsTriggeredFalse(
                            product.getId());

            for (PriceAlert alert : alerts) {
                if (newPrice <= alert.getTargetPrice()) {
                    log.info("Price drop detected! Alerting: {}",
                            alert.getUser().getEmail());

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