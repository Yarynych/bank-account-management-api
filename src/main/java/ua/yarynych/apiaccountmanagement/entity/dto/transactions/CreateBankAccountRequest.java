package ua.yarynych.apiaccountmanagement.entity.dto.transactions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateBankAccountRequest {
    private String currency;
}
