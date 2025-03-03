package ua.yarynych.apiaccountmanagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ua.yarynych.apiaccountmanagement.entity.BankAccount;
import ua.yarynych.apiaccountmanagement.entity.dto.transactions.AccountFundsDto;
import ua.yarynych.apiaccountmanagement.entity.dto.transactions.FundsTransferDto;
import ua.yarynych.apiaccountmanagement.service.TransactionsService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/transaction")
public class TransactionsController {

    private final TransactionsService transactionsService;

    @Operation(summary = "Make deposit to account", description = "Add taken amount to account")
    @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = BankAccount.class))})
    @PostMapping("/deposit")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_INTERNAL_USER')")
    public ResponseEntity<?> depositFunds(@RequestBody AccountFundsDto accountFundsDto, @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(transactionsService.depositFunds(accountFundsDto, token));
    }

    @Operation(summary = "Withdraw amount of account", description = "Take amount from account")
    @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = BankAccount.class))})
    @PostMapping("/withdraw")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_INTERNAL_USER')")
    public ResponseEntity<?> withdrawFunds(@RequestBody AccountFundsDto accountFundsDto, @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(transactionsService.withdrawFunds(accountFundsDto, token));
    }

    @Operation(summary = "Transfer amount", description = "Transfer amount from one account to another")
    @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = BankAccount.class)))})
    @PostMapping("/transfer")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_INTERNAL_USER')")
    public ResponseEntity<?> transferFunds(@RequestBody FundsTransferDto fundsTransferDto, @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(transactionsService.transferFunds(fundsTransferDto, token));
    }
}
