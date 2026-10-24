package nl.tekle.mini.payments.api.common;

// Unchecked, so @Transactional rolls back automatically when it is thrown.
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
