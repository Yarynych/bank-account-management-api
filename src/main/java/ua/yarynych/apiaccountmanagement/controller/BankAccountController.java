package ua.yarynych.apiaccountmanagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ua.yarynych.apiaccountmanagement.entity.BankAccount;
import ua.yarynych.apiaccountmanagement.entity.OperationHistory;
import ua.yarynych.apiaccountmanagement.entity.User;
import ua.yarynych.apiaccountmanagement.entity.dto.transactions.CreateBankAccountRequest;
import ua.yarynych.apiaccountmanagement.service.BankAccountService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/accounts")
@Tag(name = "Accounts logic", description = "Logic for working with users accounts in bank")
public class BankAccountController {

    private final BankAccountService bankAccountService;

    @Operation(summary = "Create new account for user", description = "Extracts the user from token and create account for him and connection with him")
    @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = BankAccount.class))})
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_INTERNAL_USER')")
    public ResponseEntity<?> createBankAccount(@RequestBody CreateBankAccountRequest request, @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(bankAccountService.createBankAccount(request, token));
    }

    @Operation(summary = "Extract accounts of user", description = "Extracts the user from token and return the list of all his active accounts")
    @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = BankAccount.class)))})
    @GetMapping("/get")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_INTERNAL_USER')")
    public ResponseEntity<?> getUserBankAccounts(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(bankAccountService.getUserBankAccounts(token));
    }

    @Operation(summary = "Extract accounts by number", description = "Extracts the account by its number")
    @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = BankAccount.class))})
    @GetMapping("/number")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_INTERNAL_USER')")
    public ResponseEntity<?> getBankAccountsByNumber(@RequestParam Long accountNumber, @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(bankAccountService.getBankAccountsByNumber(accountNumber, token));
    }

    @Operation(summary = "Close opened accounts", description = "Close account of user. Deleted its from db")
    @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))})
    @PostMapping("/close")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_INTERNAL_USER')")
    public ResponseEntity<?> closeBankAccountsByNumber(@RequestParam Long accountNumber, @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(bankAccountService.closeBankAccountsByNumber(accountNumber, token));
    }

    @Operation(summary = "Extract all accounts in system", description = "Extracts all opened accounts in system. For admins only")
    @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = BankAccount.class)))})
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<?> getAllBankAccounts(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(bankAccountService.getAllBankAccounts(token));
    }

    @Operation(summary = "Extract all accounts transactions", description = "Extracts all transactions by account number")
    @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = OperationHistory.class)))})
    @GetMapping("/operations/history")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_INTERNAL_USER')")
    public ResponseEntity<?> getAllAccountTransactions(@RequestParam Long accountNumber, @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(bankAccountService.getHistoryOfOperationsByAccountId(accountNumber, token));
    }
}

