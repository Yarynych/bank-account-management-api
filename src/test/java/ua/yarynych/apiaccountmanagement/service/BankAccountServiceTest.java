package ua.yarynych.apiaccountmanagement.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.yarynych.apiaccountmanagement.entity.BankAccount;
import ua.yarynych.apiaccountmanagement.entity.OperationHistory;
import ua.yarynych.apiaccountmanagement.entity.User;
import ua.yarynych.apiaccountmanagement.entity.UserBankAccountRelation;
import ua.yarynych.apiaccountmanagement.entity.auth.UserAuthDetails;
import ua.yarynych.apiaccountmanagement.entity.dto.transactions.CreateBankAccountRequest;
import ua.yarynych.apiaccountmanagement.entity.enums.Role;
import ua.yarynych.apiaccountmanagement.entity.exceptions.DatabaseNotFoundException;
import ua.yarynych.apiaccountmanagement.entity.exceptions.InappropriateRoleException;
import ua.yarynych.apiaccountmanagement.repository.BankAccountRepository;
import ua.yarynych.apiaccountmanagement.repository.UserBankAccountRepository;
import ua.yarynych.apiaccountmanagement.service.auth.JwtService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankAccountServiceTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private OperationHistoryService operationHistoryService;

    @Mock
    private UserBankAccountRepository userBankAccountRepository;

    @Mock
    private JwtService tokenProvider;

    @Mock
    private UserService userService;

    @InjectMocks
    private BankAccountService bankAccountService;

    @Test
    void testFindAccountByIdSuccess() {
        Long accountId = 1L;
        BankAccount bankAccount = new BankAccount();
        bankAccount.setId(accountId);

        when(bankAccountRepository.findById(accountId)).thenReturn(Optional.of(bankAccount));

        BankAccount result = bankAccountService.findAccountById(accountId);

        assertNotNull(result);
        assertEquals(accountId, result.getId());

        verify(bankAccountRepository, times(1)).findById(accountId);
    }

    @Test
    void testFindAccountByIdThrowsException() {
        Long accountId = 1L;
        when(bankAccountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(DatabaseNotFoundException.class, () -> bankAccountService.findAccountById(accountId));

        verify(bankAccountRepository, times(1)).findById(accountId);
    }

    @Test
    void testSaveAccountSuccess() {
        BankAccount bankAccount = new BankAccount();
        when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(bankAccount);

        BankAccount result = bankAccountService.saveAccount(bankAccount);

        assertNotNull(result);
        verify(bankAccountRepository, times(1)).save(bankAccount);
    }

    @Test
    void testCreateBankAccountSuccess() {
        String token = "Bearer testToken";
        String email = "user@example.com";

        User user = new User();
        user.setEmail(email);

        CreateBankAccountRequest request = new CreateBankAccountRequest();
        request.setCurrency("USD");

        when(tokenProvider.read(anyString())).thenReturn(UserAuthDetails.build(email, Role.ROLE_INTERNAL_USER.name()));
        when(userService.findUserByEmail(anyString(), anyString())).thenReturn(user);
        when(bankAccountRepository.save(any(BankAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userBankAccountRepository.save(any(UserBankAccountRelation.class))).thenReturn(new UserBankAccountRelation());

        BankAccount result = bankAccountService.createBankAccount(request, token);

        assertNotNull(result);
        assertEquals("USD", result.getCurrency());
        assertEquals(BigDecimal.ZERO, result.getAmount());

        verify(userService, times(1)).findUserByEmail(email, token.replace("Bearer ", ""));
        verify(bankAccountRepository, times(1)).save(any(BankAccount.class));
        verify(userBankAccountRepository, times(1)).save(any(UserBankAccountRelation.class));
    }

    @Test
    void testCloseBankAccountsByNumberSuccess() {
        String token = "Bearer testToken";
        Long accountNumber = 1L;
        String email = "user@example.com";

        User user = new User();
        user.setId(2L);
        user.setEmail(email);

        UserBankAccountRelation relation = new UserBankAccountRelation();
        relation.setUser(user);

        when(tokenProvider.read(anyString())).thenReturn(UserAuthDetails.build(email, Role.ROLE_INTERNAL_USER.name()));
        when(userService.findUserByEmail(anyString(), anyString())).thenReturn(user);
        when(userBankAccountRepository.findByBankAccountId(accountNumber)).thenReturn(Optional.of(relation));
        doNothing().when(bankAccountRepository).deleteById(accountNumber);

        String result = bankAccountService.closeBankAccountsByNumber(accountNumber, token);

        assertEquals("Account deleted successfully", result);

        verify(bankAccountRepository, times(1)).deleteById(accountNumber);
    }

    @Test
    void testGetUserBankAccountsSuccess() {
        String token = "Bearer testToken";
        String email = "user@example.com";

        User user = new User();
        user.setId(1L);
        user.setEmail(email);

        UserBankAccountRelation relation1 = new UserBankAccountRelation();
        UserBankAccountRelation relation2 = new UserBankAccountRelation();
        relation1.setBankAccount(new BankAccount());
        relation2.setBankAccount(new BankAccount());

        when(tokenProvider.read(anyString())).thenReturn(UserAuthDetails.build(email, Role.ROLE_INTERNAL_USER.name()));
        when(userService.findUserByEmail(anyString(), anyString())).thenReturn(user);
        when(userBankAccountRepository.findByUserId(user.getId())).thenReturn(List.of(relation1, relation2));

        List<BankAccount> result = bankAccountService.getUserBankAccounts(token);

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(userBankAccountRepository, times(1)).findByUserId(user.getId());
    }

    @Test
    void testGetAllBankAccountsSuccessAdmin() {
        String token = "Bearer testToken";
        String email = "admin@example.com";

        User adminUser = new User();
        adminUser.setRole(Role.ROLE_ADMIN);
        adminUser.setEmail(email);

        BankAccount bankAccount1 = new BankAccount();
        BankAccount bankAccount2 = new BankAccount();

        when(tokenProvider.read(anyString())).thenReturn(UserAuthDetails.build(email, Role.ROLE_INTERNAL_USER.name()));
        when(userService.findUserByEmail(anyString(), anyString())).thenReturn(adminUser);
        when(bankAccountRepository.findAll()).thenReturn(List.of(bankAccount1, bankAccount2));

        List<BankAccount> result = bankAccountService.getAllBankAccounts(token);

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(bankAccountRepository, times(1)).findAll();
    }

    @Test
    void testGetAllBankAccountsFailNonAdmin() {
        String token = "Bearer testToken";
        String email = "user@example.com";

        User normalUser = new User();
        normalUser.setRole(Role.ROLE_INTERNAL_USER);
        normalUser.setEmail(email);

        when(tokenProvider.read(anyString())).thenReturn(UserAuthDetails.build(email, Role.ROLE_INTERNAL_USER.name()));
        when(userService.findUserByEmail(anyString(), anyString())).thenReturn(normalUser);

        assertThrows(InappropriateRoleException.class, () -> bankAccountService.getAllBankAccounts(token));

        verify(bankAccountRepository, never()).findAll();
    }

    @Test
    void testGetHistoryOfOperationsByAccountIdSuccess() {
        String token = "Bearer testToken";
        Long accountNumber = 1L;
        String email = "user@example.com";

        User user = new User();
        user.setId(1L);
        user.setEmail(email);

        UserBankAccountRelation relation = new UserBankAccountRelation();
        relation.setUser(user);

        OperationHistory operation1 = new OperationHistory();
        OperationHistory operation2 = new OperationHistory();

        when(tokenProvider.read(anyString())).thenReturn(UserAuthDetails.build(email, Role.ROLE_INTERNAL_USER.name()));
        when(userService.findUserByEmail(anyString(), anyString())).thenReturn(user);
        when(userBankAccountRepository.findByBankAccountId(accountNumber)).thenReturn(Optional.of(relation));
        when(operationHistoryService.getOperationsByBankAccountId(accountNumber)).thenReturn(List.of(operation1, operation2));

        List<OperationHistory> result = bankAccountService.getHistoryOfOperationsByAccountId(accountNumber, token);

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(operationHistoryService, times(1)).getOperationsByBankAccountId(accountNumber);
    }
}

