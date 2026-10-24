package nl.tekle.mini.payments.api.transfer;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransferRepository extends JpaRepository<Transfer, UUID> {

    @Query("""
            select t from Transfer t
            where t.fromAccountId = :accountId or t.toAccountId = :accountId
            order by t.createdAt desc
            """)
    List<Transfer> findAllForAccount(@Param("accountId") UUID accountId);
}
