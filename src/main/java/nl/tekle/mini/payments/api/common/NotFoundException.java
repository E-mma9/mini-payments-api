package nl.tekle.mini.payments.api.common;

// Unchecked, so @Transactional rolls back automatically when it is thrown.
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
