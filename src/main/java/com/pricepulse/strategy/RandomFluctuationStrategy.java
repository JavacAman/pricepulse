package com.pricepulse.strategy;

import org.springframework.stereotype.Component;

@Component
public class RandomFluctuationStrategy
        implements PriceUpdateStrategy {

    @Override
    public Double updatePrice(Double currentPrice) {
        double fluctuation = (Math.random() * 0.4) - 0.2;
        double newPrice = currentPrice * (1 + fluctuation);
        return Math.round(newPrice * 100.0) / 100.0;
    }
}