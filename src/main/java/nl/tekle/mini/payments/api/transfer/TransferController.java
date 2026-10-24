package nl.tekle.mini.payments.api.transfer;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping("/transfers")
    @ResponseStatus(HttpStatus.CREATED)
    public Transfer create(@Valid @RequestBody TransferRequest request) {
        return transferService.transfer(request);
    }

    // Lives here rather than in AccountController because it is a transfer query.
    @GetMapping("/accounts/{accountId}/transfers")
    public List<Transfer> findAllForAccount(@PathVariable UUID accountId) {
        return transferService.findAllForAccount(accountId);
    }
}
