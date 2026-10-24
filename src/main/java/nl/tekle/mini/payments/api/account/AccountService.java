package nl.tekle.mini.payments.api.account;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import nl.tekle.mini.payments.api.common.NotFoundException;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public Account create(CreateAccountRequest request) {
        return accountRepository.save(new Account(request.owner(), request.initialBalance()));
    }

    // No paging: fine for a demo with a handful of accounts, not for millions of rows.
    @Transactional(readOnly = true)
    public List<Account> findAll() {
        return accountRepository.findAllByOrderByCreatedAtAsc();
    }

    @Transactional(readOnly = true)
    public Account get(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Account " + id + " not found"));
    }
}
