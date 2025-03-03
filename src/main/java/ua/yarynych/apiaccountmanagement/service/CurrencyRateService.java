package ua.yarynych.apiaccountmanagement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.yarynych.apiaccountmanagement.repository.CurrencyRateRepository;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CurrencyRateService {

    private final CurrencyRateRepository currencyRateRepository;

    public BigDecimal convertCurrency(BigDecimal amount, String fromCurrency, String toCurrency) {
        return currencyRateRepository.findByBaseCurrencyAndTargetCurrency(fromCurrency, toCurrency)
                .map(rate -> amount.multiply(rate.getExchangeRate()))
                .orElseThrow(() -> new RuntimeException("Exchange rate not found"));
    }
}
