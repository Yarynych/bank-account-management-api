package ua.yarynych.apiaccountmanagement.entity.dto.transactions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountFundsDto {
    private Long accountId;
    private BigDecimal amount;
    private String currency;
    private String description;
}
