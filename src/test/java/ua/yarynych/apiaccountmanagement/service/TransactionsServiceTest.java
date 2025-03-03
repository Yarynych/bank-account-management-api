package ua.yarynych.apiaccountmanagement.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.yarynych.apiaccountmanagement.entity.BankAccount;
import ua.yarynych.apiaccountmanagement.entity.User;
import ua.yarynych.apiaccountmanagement.entity.auth.UserAuthDetails;
import ua.yarynych.apiaccountmanagement.entity.dto.transactions.AccountFundsDto;
import ua.yarynych.apiaccountmanagement.entity.dto.transactions.FundsTransferDto;
import ua.yarynych.apiaccountmanagement.entity.enums.Role;
import ua.yarynych.apiaccountmanagement.service.auth.JwtService;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionsServiceTest {

    @InjectMocks
    private TransactionsService transactionsService;

    @Mock
    private UserService userService;

    @Mock
    private JwtService tokenProvider;

    @Mock
    private BankAccountService bankAccountService;

    @Mock
    private CurrencyRateService currencyRateService;

    @Mock
    private OperationHistoryService operationHistoryService;

    private static final String TOKEN = "Bearer validToken";
    private static final String EMAIL = "user@example.com";
    private static final Long USER_ID = 1L;
    private static final Long ACCOUNT_ID = 100L;
    private static final Long ACCOUNT_ID_2 = 200L;

    private User user;
    private BankAccount account;
    private BankAccount account2;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(USER_ID);
        user.setEmail(EMAIL);

        account = new BankAccount();
        account.setId(ACCOUNT_ID);
        account.setCurrency("USD");
        account.setAmount(new BigDecimal(1000));

        account2 = new BankAccount();
        account2.setId(ACCOUNT_ID_2);
        account2.setCurrency("EUR");
        account2.setAmount(new BigDecimal(500));

        when(tokenProvider.read(anyString())).thenReturn(UserAuthDetails.build(EMAIL, Role.ROLE_INTERNAL_USER.name()));
        when(userService.findUserByEmail(eq(EMAIL), anyString())).thenReturn(user);
    }

    @Test
    void depositFundsShouldIncreaseBalance() {
        AccountFundsDto deposit = new AccountFundsDto(ACCOUNT_ID, new BigDecimal("200"), "USD", "Deposit test");

        when(bankAccountService.findAccountById(ACCOUNT_ID)).thenReturn(account);
        when(bankAccountService.saveAccount(any(BankAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BankAccount updatedAccount = transactionsService.depositFunds(deposit, TOKEN);

        assertEquals(new BigDecimal("1200"), updatedAccount.getAmount());
        verify(operationHistoryService).saveOperation(eq(ACCOUNT_ID), eq(USER_ID), eq("DEPOSIT"), eq(new BigDecimal("200")), eq("Deposit test"));
    }

    @Test
    void depositFundsShouldConvertCurrencyAndIncreaseBalance() {
        AccountFundsDto deposit = new AccountFundsDto(ACCOUNT_ID, new BigDecimal("100"), "EUR", "Deposit in EUR");

        when(bankAccountService.findAccountById(ACCOUNT_ID)).thenReturn(account);
        when(currencyRateService.convertCurrency(new BigDecimal("100"), "EUR", "USD")).thenReturn(new BigDecimal("110"));
        when(bankAccountService.saveAccount(any(BankAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BankAccount updatedAccount = transactionsService.depositFunds(deposit, TOKEN);

        assertEquals(new BigDecimal("1110"), updatedAccount.getAmount());
        verify(operationHistoryService).saveOperation(eq(ACCOUNT_ID), eq(USER_ID), eq("DEPOSIT"), eq(new BigDecimal("110")), eq("Deposit in EUR"));
    }

    @Test
    void withdrawFundsShouldDecreaseBalance() {
        AccountFundsDto withdrawal = new AccountFundsDto(ACCOUNT_ID, new BigDecimal("300"), "USD", "Withdraw test");

        when(bankAccountService.findAccountById(ACCOUNT_ID)).thenReturn(account);
        when(bankAccountService.saveAccount(any(BankAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BankAccount updatedAccount = transactionsService.withdrawFunds(withdrawal, TOKEN);

        assertEquals(new BigDecimal("700"), updatedAccount.getAmount());
        verify(operationHistoryService).saveOperation(eq(ACCOUNT_ID), eq(USER_ID), eq("WITHDRAW"), eq(new BigDecimal("300")), eq("Withdraw test"));
    }

    @Test
    void withdrawFunds_ShouldThrowException_WhenInsufficientFunds() {
        AccountFundsDto withdrawal = new AccountFundsDto(ACCOUNT_ID, new BigDecimal("2000"), "USD", "Withdraw test");

        when(bankAccountService.findAccountById(ACCOUNT_ID)).thenReturn(account);

        assertThrows(IllegalStateException.class, () -> transactionsService.withdrawFunds(withdrawal, TOKEN));
    }

    @Test
    void transferFundsShouldTransferCorrectAmountBetweenAccounts() {
        account.setAmount(new BigDecimal("1000"));
        account2.setAmount(new BigDecimal("500"));

        FundsTransferDto transfer = new FundsTransferDto(
                ACCOUNT_ID, ACCOUNT_ID_2, new BigDecimal("100"), "USD", "Transfer test"
        );

        when(bankAccountService.findAccountById(ACCOUNT_ID)).thenReturn(account);
        when(bankAccountService.findAccountById(ACCOUNT_ID_2)).thenReturn(account2);
        when(currencyRateService.convertCurrency(any(), any(), any())).thenReturn(new BigDecimal("100"));
        when(bankAccountService.saveAccount(any(BankAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<BankAccount> updatedAccounts = transactionsService.transferFunds(transfer, TOKEN);

        assertEquals(new BigDecimal("900"), updatedAccounts.get(0).getAmount()); // 1000 - 100
        assertEquals(new BigDecimal("600"), updatedAccounts.get(1).getAmount()); // 500 + 100

        verify(operationHistoryService).saveOperation(
                eq(ACCOUNT_ID), eq(USER_ID), eq("WITHDRAW"), eq(new BigDecimal("100")), eq("Transfer test")
        );
        verify(operationHistoryService).saveOperation(
                eq(ACCOUNT_ID_2), eq(USER_ID), eq("DEPOSIT"), eq(new BigDecimal("100")), eq("Transfer test")
        );
    }

    @Test
    void transferFundsShouldConvertCurrencyDuringTransfer() {
        FundsTransferDto transfer = new FundsTransferDto(ACCOUNT_ID, ACCOUNT_ID_2, new BigDecimal("100"), "USD", "Transfer test");

        when(bankAccountService.findAccountById(ACCOUNT_ID)).thenReturn(account);
        when(bankAccountService.findAccountById(ACCOUNT_ID_2)).thenReturn(account2);
        when(currencyRateService.convertCurrency(new BigDecimal("100"), "USD", "EUR")).thenReturn(new BigDecimal("90"));
        when(bankAccountService.saveAccount(any(BankAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<BankAccount> updatedAccounts = transactionsService.transferFunds(transfer, TOKEN);

        assertEquals(new BigDecimal("900"), updatedAccounts.get(0).getAmount());
        assertEquals(new BigDecimal("590"), updatedAccounts.get(1).getAmount());

        verify(operationHistoryService).saveOperation(eq(ACCOUNT_ID), eq(USER_ID), eq("WITHDRAW"), eq(new BigDecimal("100")), eq("Transfer test"));
        verify(operationHistoryService).saveOperation(eq(ACCOUNT_ID_2), eq(USER_ID), eq("DEPOSIT"), eq(new BigDecimal("90")), eq("Transfer test"));
    }

    @Test
    void transferFundsShouldThrowException_WhenInsufficientFunds() {
        FundsTransferDto transfer = new FundsTransferDto(ACCOUNT_ID, ACCOUNT_ID_2, new BigDecimal("2000"), "USD", "Transfer test");

        when(bankAccountService.findAccountById(ACCOUNT_ID)).thenReturn(account);

        assertThrows(IllegalStateException.class, () -> transactionsService.transferFunds(transfer, TOKEN));
    }
}
