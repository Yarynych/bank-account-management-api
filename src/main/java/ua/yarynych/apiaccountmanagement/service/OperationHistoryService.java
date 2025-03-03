package ua.yarynych.apiaccountmanagement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.yarynych.apiaccountmanagement.entity.OperationHistory;
import ua.yarynych.apiaccountmanagement.repository.OperationHistoryRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OperationHistoryService {

    private final OperationHistoryRepository operationHistoryRepository;

    public OperationHistory saveOperation(Long bankAccountId, Long userId, String operationType, BigDecimal amount, String description) {
        OperationHistory operation = new OperationHistory();
        operation.setBankAccountId(bankAccountId);
        operation.setUserId(userId);
        operation.setOperationType(operationType);
        operation.setAmount(amount);
        operation.setOperationDate(LocalDateTime.now());
        operation.setDescription(description);

        return operationHistoryRepository.save(operation);
    }

    public List<OperationHistory> getOperationsByBankAccountId(Long bankAccountId) {
        return operationHistoryRepository.findByBankAccountId(bankAccountId);
    }
}
