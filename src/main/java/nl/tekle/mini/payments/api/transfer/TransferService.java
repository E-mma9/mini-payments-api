package nl.tekle.mini.payments.api.transfer;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import nl.tekle.mini.payments.api.account.Account;
import nl.tekle.mini.payments.api.account.AccountRepository;
import nl.tekle.mini.payments.api.common.BusinessRuleException;
import nl.tekle.mini.payments.api.common.NotFoundException;

@Service
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;

    public TransferService(AccountRepository accountRepository, TransferRepository transferRepository) {
        this.accountRepository = accountRepository;
        this.transferRepository = transferRepository;
    }

    // One transaction: the debit, the credit and the new Transfer row are committed together,
    // or, if anything throws, all of them are rolled back. Money is never lost halfway.
    @Transactional
    public Transfer transfer(TransferRequest request) {
        if (request.fromAccountId().equals(request.toAccountId())) {
            throw new BusinessRuleException("Cannot transfer to the same account");
        }

        Account sender = findAccount(request.fromAccountId());
        Account receiver = findAccount(request.toAccountId());

        // compareTo instead of equals/<: BigDecimal.equals also compares scale (10.0 != 10.00).
        if (sender.getBalance().compareTo(request.amount()) < 0) {
            throw new BusinessRuleException("Insufficient funds");
        }

        // No accountRepository.save() needed: both accounts are managed by JPA inside this
        // transaction, so their changed balances are written on commit (dirty checking).
        sender.debit(request.amount());
        receiver.credit(request.amount());

        return transferRepository.save(
                new Transfer(request.fromAccountId(), request.toAccountId(), request.amount()));
    }

    @Transactional(readOnly = true)
    public List<Transfer> findAllForAccount(UUID accountId) {
        if (!accountRepository.existsById(accountId)) {
            throw new NotFoundException("Account " + accountId + " not found");
        }
        return transferRepository.findAllForAccount(accountId);
    }

    private Account findAccount(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Account " + id + " not found"));
    }
}
