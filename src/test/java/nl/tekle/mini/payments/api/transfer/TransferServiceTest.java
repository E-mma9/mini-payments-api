package nl.tekle.mini.payments.api.transfer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import nl.tekle.mini.payments.api.account.Account;
import nl.tekle.mini.payments.api.account.AccountRepository;
import nl.tekle.mini.payments.api.common.BusinessRuleException;
import nl.tekle.mini.payments.api.common.NotFoundException;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    private final UUID senderId = UUID.randomUUID();
    private final UUID receiverId = UUID.randomUUID();

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransferRepository transferRepository;

    private TransferService transferService;

    @BeforeEach
    void setUp() {
        transferService = new TransferService(accountRepository, transferRepository);
    }

    @Test
    void successfulTransferUpdatesBothBalances() {
        Account sender = new Account("Alice", new BigDecimal("100.00"));
        Account receiver = new Account("Bob", new BigDecimal("50.00"));
        when(accountRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(accountRepository.findById(receiverId)).thenReturn(Optional.of(receiver));
        when(transferRepository.save(any(Transfer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transfer transfer = transferService.transfer(
                new TransferRequest(senderId, receiverId, new BigDecimal("30.00")));

        assertThat(sender.getBalance()).isEqualByComparingTo("70.00");
        assertThat(receiver.getBalance()).isEqualByComparingTo("80.00");
        assertThat(transfer.getFromAccountId()).isEqualTo(senderId);
        assertThat(transfer.getToAccountId()).isEqualTo(receiverId);
        assertThat(transfer.getAmount()).isEqualByComparingTo("30.00");
    }

    @Test
    void insufficientFundsThrowsAndSavesNothing() {
        Account sender = new Account("Alice", new BigDecimal("10.00"));
        Account receiver = new Account("Bob", new BigDecimal("50.00"));
        when(accountRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(accountRepository.findById(receiverId)).thenReturn(Optional.of(receiver));

        assertThatThrownBy(() -> transferService.transfer(
                new TransferRequest(senderId, receiverId, new BigDecimal("30.00"))))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Insufficient funds");

        assertThat(sender.getBalance()).isEqualByComparingTo("10.00");
        assertThat(receiver.getBalance()).isEqualByComparingTo("50.00");
        verify(transferRepository, never()).save(any());
    }

    @Test
    void sameAccountThrows() {
        assertThatThrownBy(() -> transferService.transfer(
                new TransferRequest(senderId, senderId, new BigDecimal("30.00"))))
                .isInstanceOf(BusinessRuleException.class);

        verifyNoInteractions(accountRepository, transferRepository);
    }

    @Test
    void unknownAccountThrows() {
        when(accountRepository.findById(senderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transferService.transfer(
                new TransferRequest(senderId, receiverId, new BigDecimal("30.00"))))
                .isInstanceOf(NotFoundException.class);

        verify(transferRepository, never()).save(any());
    }
}
