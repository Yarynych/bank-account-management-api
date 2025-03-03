package ua.yarynych.apiaccountmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.yarynych.apiaccountmanagement.entity.CurrencyRate;

import java.util.Optional;

@Repository
public interface CurrencyRateRepository extends JpaRepository<CurrencyRate, Long> {
    Optional<CurrencyRate> findByBaseCurrencyAndTargetCurrency(String baseCurrency, String targetCurrency);
}
