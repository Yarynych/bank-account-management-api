package ua.yarynych.apiaccountmanagement.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.yarynych.apiaccountmanagement.entity.CurrencyRate;
import ua.yarynych.apiaccountmanagement.repository.CurrencyRateRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrencyRateServiceTest {

    @Mock
    private CurrencyRateRepository currencyRateRepository;

    @InjectMocks
    private CurrencyRateService currencyRateService;

    @Test
    void testConvertCurrencySuccess() {
        BigDecimal amount = new BigDecimal("100");
        String fromCurrency = "USD";
        String toCurrency = "EUR";
        BigDecimal exchangeRate = new BigDecimal("0.85");
        CurrencyRate currencyRate = new CurrencyRate(1L, fromCurrency, toCurrency, exchangeRate, LocalDateTime.now());

        when(currencyRateRepository.findByBaseCurrencyAndTargetCurrency(fromCurrency, toCurrency))
                .thenReturn(Optional.of(currencyRate));

        BigDecimal result = currencyRateService.convertCurrency(amount, fromCurrency, toCurrency);

        assertNotNull(result);
        assertEquals(new BigDecimal("85.00"), result);
    }

    @Test
    void testConvertCurrency_RateNotFound() {
        when(currencyRateRepository.findByBaseCurrencyAndTargetCurrency("USD", "JPY"))
                .thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () ->
                currencyRateService.convertCurrency(new BigDecimal("100"), "USD", "JPY")
        );

        assertEquals("Exchange rate not found", exception.getMessage());
    }
}