package nl.tekle.mini.payments.api.account;

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

    @Transactional(readOnly = true)
    public Account get(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Account " + id + " not found"));
    }
}
