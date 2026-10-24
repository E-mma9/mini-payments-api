package nl.tekle.mini.payments.api.account;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// @Digits rejects amounts with more than 2 decimals instead of letting the database round them.
public record CreateAccountRequest(
        @NotBlank String owner,
        @NotNull @DecimalMin("0.00") @Digits(integer = 17, fraction = 2) BigDecimal initialBalance) {
}
