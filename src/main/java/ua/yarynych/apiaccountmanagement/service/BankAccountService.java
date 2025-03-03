package ua.yarynych.apiaccountmanagement.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import ua.yarynych.apiaccountmanagement.entity.BankAccount;
import ua.yarynych.apiaccountmanagement.entity.OperationHistory;
import ua.yarynych.apiaccountmanagement.entity.User;
import ua.yarynych.apiaccountmanagement.entity.UserBankAccountRelation;
import ua.yarynych.apiaccountmanagement.entity.dto.transactions.CreateBankAccountRequest;
import ua.yarynych.apiaccountmanagement.entity.enums.Currency;
import ua.yarynych.apiaccountmanagement.entity.enums.Role;
import ua.yarynych.apiaccountmanagement.entity.exceptions.DatabaseNotFoundException;
import ua.yarynych.apiaccountmanagement.entity.exceptions.InappropriateRoleException;
import ua.yarynych.apiaccountmanagement.repository.BankAccountRepository;
import ua.yarynych.apiaccountmanagement.repository.UserBankAccountRepository;
import ua.yarynych.apiaccountmanagement.service.auth.JwtService;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final OperationHistoryService operationHistoryService;
    private final UserBankAccountRepository userBankAccountRepository;
    private final JwtService tokenProvider;
    private final UserService userService;

    public BankAccount findAccountById(Long id) {
        return bankAccountRepository.findById(id)
                .orElseThrow(() -> new DatabaseNotFoundException("Account not found"));
    }

    private String deleteAccountById(Long accountId) {
        bankAccountRepository.deleteById(accountId);
        return "Account deleted successfully";
    }

    @Transactional
    public BankAccount saveAccount(BankAccount account) {
        log.info("Saving new account: {}", account.getId());
        return bankAccountRepository.save(account);
    }

    @Transactional
    public BankAccount createBankAccount(CreateBankAccountRequest request, String token) {
        token = token.replace("Bearer ", "");
        String email = tokenProvider.read(token).getEmail();
        User user = userService.findUserByEmail(email, token);

        if (!Currency.isValid(request.getCurrency())) {
            throw new IllegalArgumentException("Invalid currency: " + request.getCurrency());
        }

        BankAccount bankAccount = new BankAccount();
        bankAccount.setCurrency(request.getCurrency());
        bankAccount.setAmount(BigDecimal.ZERO);

        bankAccount = saveAccount(bankAccount);

        UserBankAccountRelation relation = new UserBankAccountRelation();
        relation.setUser(user);
        relation.setBankAccount(bankAccount);

        userBankAccountRepository.save(relation);

        return bankAccount;
    }

    @Transactional
    public String closeBankAccountsByNumber(Long accountNumber, String token) {
        token = token.replace("Bearer ", "");
        String email = tokenProvider.read(token).getEmail();
        User user = userService.findUserByEmail(email, token);

        UserBankAccountRelation accountRelation = userBankAccountRepository.findByBankAccountId(accountNumber)
                .orElseThrow(() -> new DatabaseNotFoundException("Account not found"));

        if(!accountRelation.getUser().getId().equals(user.getId()) && !Role.ROLE_ADMIN.equals(user.getRole())) {
            throw new IllegalArgumentException("User couldn`t delete another user account details");
        }

        return deleteAccountById(accountNumber);
    }

    public List<BankAccount> getUserBankAccounts(String token) {
        token = token.replace("Bearer ", "");
        String email = tokenProvider.read(token).getEmail();
        User user = userService.findUserByEmail(email, token);

        List<UserBankAccountRelation> relations = userBankAccountRepository.findByUserId(user.getId());

        return relations.stream()
                .map(UserBankAccountRelation::getBankAccount)
                .collect(Collectors.toList());
    }

    public BankAccount getBankAccountsByNumber(Long accountNumber, String token) {
        token = token.replace("Bearer ", "");
        String email = tokenProvider.read(token).getEmail();
        User user = userService.findUserByEmail(email, token);

        UserBankAccountRelation accountRelation = userBankAccountRepository.findByBankAccountId(accountNumber)
                .orElseThrow(() -> new DatabaseNotFoundException("Account not found"));

        if(!accountRelation.getUser().getId().equals(user.getId()) && !Role.ROLE_ADMIN.equals(user.getRole())) {
            throw new IllegalArgumentException("User couldn`t see another user account details");
        }

        return findAccountById(accountNumber);
    }

    public List<BankAccount> getAllBankAccounts(String token) {
        token = token.replace("Bearer ", "");
        String email = tokenProvider.read(token).getEmail();
        User user = userService.findUserByEmail(email, token);

        if(!Role.ROLE_ADMIN.equals(user.getRole())) {
            throw new InappropriateRoleException("User with id " + user.getId() + ", isn`t admin");
        }

        return bankAccountRepository.findAll();
    }

    public List<OperationHistory> getHistoryOfOperationsByAccountId(Long accountNumber, String token) {
        token = token.replace("Bearer ", "");
        String email = tokenProvider.read(token).getEmail();
        User user = userService.findUserByEmail(email, token);

        UserBankAccountRelation accountRelation = userBankAccountRepository.findByBankAccountId(accountNumber)
                .orElseThrow(() -> new DatabaseNotFoundException("Account not found"));

        if(!accountRelation.getUser().getId().equals(user.getId()) && !Role.ROLE_ADMIN.equals(user.getRole())) {
            throw new IllegalArgumentException("User couldn`t see another user account operation history");
        }

        return operationHistoryService.getOperationsByBankAccountId(accountNumber);
    }
}
