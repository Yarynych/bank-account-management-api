package ua.yarynych.apiaccountmanagement.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.yarynych.apiaccountmanagement.entity.OperationHistory;
import ua.yarynych.apiaccountmanagement.repository.OperationHistoryRepository;
import ua.yarynych.apiaccountmanagement.service.OperationHistoryService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OperationHistoryServiceTest {

    @Mock
    private OperationHistoryRepository operationHistoryRepository;

    @InjectMocks
    private OperationHistoryService operationHistoryService;

    @Test
    void testSaveOperation_Success() {
        Long bankAccountId = 1L;
        Long userId = 2L;
        String operationType = "Deposit";
        BigDecimal amount = new BigDecimal("1000.00");
        String description = "Salary";

        OperationHistory operation = new OperationHistory();
        operation.setBankAccountId(bankAccountId);
        operation.setUserId(userId);
        operation.setOperationType(operationType);
        operation.setAmount(amount);
        operation.setOperationDate(LocalDateTime.now());
        operation.setDescription(description);

        when(operationHistoryRepository.save(any(OperationHistory.class)))
                .thenReturn(operation);

        OperationHistory result = operationHistoryService.saveOperation(bankAccountId, userId, operationType, amount, description);

        assertNotNull(result);
        assertEquals(bankAccountId, result.getBankAccountId());
        assertEquals(userId, result.getUserId());
        assertEquals(operationType, result.getOperationType());
        assertEquals(amount, result.getAmount());
        assertEquals(description, result.getDescription());

        verify(operationHistoryRepository, times(1)).save(any(OperationHistory.class));
    }

    @Test
    void testGetOperationsByBankAccountId_Success() {
        Long bankAccountId = 1L;
        List<OperationHistory> operations = List.of(new OperationHistory(), new OperationHistory());

        when(operationHistoryRepository.findByBankAccountId(bankAccountId))
                .thenReturn(operations);

        List<OperationHistory> result = operationHistoryService.getOperationsByBankAccountId(bankAccountId);

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(operationHistoryRepository, times(1)).findByBankAccountId(bankAccountId);
    }
}
