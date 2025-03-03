package ua.yarynych.apiaccountmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.yarynych.apiaccountmanagement.entity.UserBankAccountRelation;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBankAccountRepository extends JpaRepository<UserBankAccountRelation, Long> {
    List<UserBankAccountRelation> findByUserId(Long userId);
    Optional<UserBankAccountRelation> findByBankAccountId(Long accountId);
}