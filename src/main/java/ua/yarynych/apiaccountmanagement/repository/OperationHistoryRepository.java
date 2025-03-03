package ua.yarynych.apiaccountmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.yarynych.apiaccountmanagement.entity.OperationHistory;

import java.util.List;

@Repository
public interface OperationHistoryRepository extends JpaRepository<OperationHistory, Long> {
    List<OperationHistory> findByBankAccountId(Long bankAccountId);
    List<OperationHistory> findByUserId(Long userId);
}
