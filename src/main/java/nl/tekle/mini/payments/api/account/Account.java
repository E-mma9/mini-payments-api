package nl.tekle.mini.payments.api.account;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String owner;

    // BigDecimal stores exact decimal values; double/float cannot represent amounts like 0.10 exactly.
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    // Optimistic locking: an UPDATE only succeeds if nobody changed the row since we read it,
    // so two concurrent transfers can never overwrite each other's balance change.
    @Version
    private Long version;

    // Required by JPA to instantiate the entity when loading it from the database.
    protected Account() {
    }

    public Account(String owner, BigDecimal initialBalance) {
        if (initialBalance.signum() < 0) {
            throw new IllegalArgumentException("Initial balance must not be negative");
        }
        this.owner = owner;
        this.balance = initialBalance;
        this.createdAt = Instant.now();
    }

    public void debit(BigDecimal amount) {
        requirePositive(amount);
        // Last line of defence for the "never negative" rule; TransferService checks this first.
        if (balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Balance cannot become negative");
        }
        balance = balance.subtract(amount);
    }

    public void credit(BigDecimal amount) {
        requirePositive(amount);
        balance = balance.add(amount);
    }

    private static void requirePositive(BigDecimal amount) {
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }

    public UUID getId() {
        return id;
    }

    public String getOwner() {
        return owner;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
