package ua.yarynych.apiaccountmanagement.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.yarynych.apiaccountmanagement.entity.BankAccount;
import ua.yarynych.apiaccountmanagement.entity.User;
import ua.yarynych.apiaccountmanagement.entity.dto.transactions.AccountFundsDto;
import ua.yarynych.apiaccountmanagement.entity.dto.transactions.FundsTransferDto;
import ua.yarynych.apiaccountmanagement.entity.enums.Operations;
import ua.yarynych.apiaccountmanagement.service.auth.JwtService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionsService {

    private final UserService userService;
    private final JwtService tokenProvider;
    private final BankAccountService bankAccountService;
    private final CurrencyRateService currencyRateService;
    private final OperationHistoryService operationHistoryService;

    @Transactional
    public BankAccount depositFunds(AccountFundsDto deposit, String token) {
        token = token.replace("Bearer ", "");
        String email = tokenProvider.read(token).getEmail();
        User user = userService.findUserByEmail(email, token);

        BigDecimal amount = deposit.getAmount();

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than zero");
        }

        BankAccount account = bankAccountService.findAccountById(deposit.getAccountId());

        if(!deposit.getCurrency().equals(account.getCurrency())) {
            amount = currencyRateService.convertCurrency(amount, deposit.getCurrency(), account.getCurrency());
        }

        account.setAmount(account.getAmount().add(amount));
        operationHistoryService.saveOperation(account.getId(), user.getId(), Operations.DEPOSIT.name(), amount, deposit.getDescription());
        return bankAccountService.saveAccount(account);
    }

    @Transactional
    public BankAccount withdrawFunds(AccountFundsDto deposit, String token) {
        token = token.replace("Bearer ", "");
        String email = tokenProvider.read(token).getEmail();
        User user = userService.findUserByEmail(email, token);

        BigDecimal amount = deposit.getAmount();

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be greater than zero");
        }

        BankAccount account = bankAccountService.findAccountById(deposit.getAccountId());

        if(!deposit.getCurrency().equals(account.getCurrency())) {
            amount = currencyRateService.convertCurrency(amount, deposit.getCurrency(), account.getCurrency());
        }

        if (account.getAmount().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }

        account.setAmount(account.getAmount().subtract(amount));
        operationHistoryService.saveOperation(account.getId(), user.getId(), Operations.WITHDRAW.name(), amount, deposit.getDescription());
        return bankAccountService.saveAccount(account);
    }

    @Transactional
    public List<BankAccount> transferFunds(FundsTransferDto fundsTransferDto, String token) {
        token = token.replace("Bearer ", "");
        String email = tokenProvider.read(token).getEmail();
        User user = userService.findUserByEmail(email, token);

        if (fundsTransferDto.getAmount() == null || fundsTransferDto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than zero");
        }
        BigDecimal amount = fundsTransferDto.getAmount();

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than zero");
        }

        BankAccount fromAccount = bankAccountService.findAccountById(fundsTransferDto.getFromAccountId());
        BankAccount toAccount = bankAccountService.findAccountById(fundsTransferDto.getToAccountId());

        if (fromAccount == null || toAccount == null) {
            throw new IllegalStateException("One of the accounts does not exist.");
        }

        fromAccount.setAmount(fromAccount.getAmount() == null ? BigDecimal.ZERO : fromAccount.getAmount());
        toAccount.setAmount(toAccount.getAmount() == null ? BigDecimal.ZERO : toAccount.getAmount());

        if (!fundsTransferDto.getCurrency().equals(fromAccount.getCurrency())) {
            BigDecimal convertedAmount = currencyRateService.convertCurrency(amount, fundsTransferDto.getCurrency(), fromAccount.getCurrency());
            if (convertedAmount == null) {
                throw new IllegalStateException("Currency conversion failed.");
            }
            amount = convertedAmount;
        }

        if (fromAccount.getAmount().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }

        BigDecimal transferAmount = amount;
        if (!fromAccount.getCurrency().equals(toAccount.getCurrency())) {
            transferAmount = currencyRateService.convertCurrency(amount, fromAccount.getCurrency(), toAccount.getCurrency());
            if (transferAmount == null) {
                throw new IllegalStateException("Currency conversion failed.");
            }
        }

        fromAccount.setAmount(fromAccount.getAmount().subtract(amount));
        toAccount.setAmount(toAccount.getAmount().add(transferAmount));

        List<BankAccount> accounts = new ArrayList<>();
        accounts.add(bankAccountService.saveAccount(fromAccount));
        accounts.add(bankAccountService.saveAccount(toAccount));

        operationHistoryService.saveOperation(fromAccount.getId(), user.getId(), Operations.WITHDRAW.name(), amount, fundsTransferDto.getDescription());
        operationHistoryService.saveOperation(toAccount.getId(), user.getId(), Operations.DEPOSIT.name(), transferAmount, fundsTransferDto.getDescription());
        return accounts;
    }
}
