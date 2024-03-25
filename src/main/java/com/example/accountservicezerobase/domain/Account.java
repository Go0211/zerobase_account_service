package com.example.accountservicezerobase.domain;

import com.example.accountservicezerobase.exception.AccountException;
import com.example.accountservicezerobase.type.AccountStatus;
import com.example.accountservicezerobase.type.ErrorCode;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Account {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private AccountUser accountUser;
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus;
    private Long balance;

    private LocalDateTime registeredAt;
    private LocalDateTime unRegisteredAt;

    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    public void useBalance(Long amount) {
        if (amount > balance) {
            throw  new AccountException(ErrorCode.AMOUNT_EXCEED_BALANCE);
        }

        balance -= amount;
    }

    public void cancelBalance(Long amount) {
        if (amount < 0) {
            throw  new AccountException(ErrorCode.INVALID_REQUEST);
        }

        balance += amount;
    }
}
