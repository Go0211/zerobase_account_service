package com.example.accountservicezerobase.service;


import com.example.accountservicezerobase.domain.Account;
import com.example.accountservicezerobase.domain.AccountUser;
import com.example.accountservicezerobase.dto.AccountDto;
import com.example.accountservicezerobase.exception.AccountException;
import com.example.accountservicezerobase.repository.AccountRepository;
import com.example.accountservicezerobase.repository.AccountUserRepository;
import com.example.accountservicezerobase.type.AccountStatus;
import com.example.accountservicezerobase.type.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountUserRepository accountUserRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void createAccountSuccess() {
        AccountUser accountUser = AccountUser.builder()
                .id(12L)
                .name("a")
                .build();

        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn((Optional.of(accountUser)));
        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.of(Account.builder()
                        .accountNumber("1000000121")
                        .build()));
        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .accountUser(accountUser)
                        .accountNumber("1000000129")
                        .build());

        ArgumentCaptor<Account> captor
                = ArgumentCaptor.forClass(Account.class);

        // when
        AccountDto accountDto
                = accountService.createAccount(1L, 1000L);
        // then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(12L, accountDto.getUserId());
        assertEquals("1000000122", captor.getValue().getAccountNumber());
    }

    @Test
    void deleteAccountSuccess() {
        AccountUser accountUser = AccountUser.builder()
                .id(12L)
                .name("a")
                .build();

        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn((Optional.of(accountUser)));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(accountUser)
                        .balance(0L)
                        .accountNumber("1000000121")
                        .build()));

        ArgumentCaptor<Account> captor
                = ArgumentCaptor.forClass(Account.class);

        // when
        AccountDto accountDto
                = accountService.deleteAccount(1L, "1234567890");
        // then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(12L, accountDto.getUserId());
        assertEquals("1000000121", captor.getValue().getAccountNumber());
        assertEquals(AccountStatus.UNREGISTERED, captor.getValue().getAccountStatus());
    }

    @Test
    void createAccount_UserNotFound() {
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        // when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.createAccount(1L, 1000L));
        // then
        assertEquals(ErrorCode.USER_NOT_FOUND, accountException.getErrorCode());
    }

    @Test
    void createAccount_maxAccountIs10() {
        AccountUser accountUser = AccountUser.builder()
                .id(12L)
                .name("a")
                .build();

        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn((Optional.of(accountUser)));
        given(accountRepository.countByAccountUser(any()))
                .willReturn(10);
        // when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.createAccount(1L, 1000L));
        // then
        assertEquals(ErrorCode.MAX_ACCOUNT_PER_USER_10, accountException.getErrorCode());
    }

    @Test
    void deleteAccount_UserNotFound() {
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        // when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));
        // then
        assertEquals(ErrorCode.USER_NOT_FOUND, accountException.getErrorCode());
    }

    @Test
    void deleteAccount_AccountNotFound() {
        AccountUser accountUser = AccountUser.builder()
                .id(12L)
                .name("a")
                .build();

        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn((Optional.of(accountUser)));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());

        // when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));
        // then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, accountException.getErrorCode());
    }

    @Test
    void deleteAccountFailed_userUnMath() {
        AccountUser accountUser1 = AccountUser.builder()
                .id(12L)
                .name("a")
                .build();
        AccountUser accountUser2 = AccountUser.builder()
                .id(13L)
                .name("b")
                .build();

        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn((Optional.of(accountUser1)));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(accountUser2)
                        .balance(0L)
                        .accountNumber("1000000121")
                        .build()));

        // when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));
        // then
        assertEquals(ErrorCode.USER_ACCOUNT_UN_MATCH, accountException.getErrorCode());
    }

    @Test
    void deleteAccountFailed_balanceNotEmpty() {
        AccountUser accountUser1 = AccountUser.builder()
                .id(12L)
                .name("a")
                .build();

        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn((Optional.of(accountUser1)));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(accountUser1)
                        .balance(100L)
                        .accountNumber("1000000121")
                        .build()));

        // when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));
        // then
        assertEquals(ErrorCode.BALANCE_NOT_EMPTY, accountException.getErrorCode());
    }

    @Test
    void deleteAccountFailed_alreadyUnregistered() {
        AccountUser accountUser1 = AccountUser.builder()
                .id(12L)
                .name("a")
                .build();

        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn((Optional.of(accountUser1)));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(accountUser1)
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .balance(100L)
                        .accountNumber("1000000121")
                        .build()));

        // when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));
        // then
        assertEquals(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED, accountException.getErrorCode());
    }

    @Test
    void successGetAccountsByUserId() {
        // given
        AccountUser accountUser1 = AccountUser.builder()
                .id(12L)
                .name("a")
                .build();
        List<Account> accountList = Arrays.asList(
                Account.builder()
                        .accountUser(accountUser1)
                        .accountNumber("1234567890")
                        .balance(1000L)
                        .build(),
                Account.builder()
                        .accountUser(accountUser1)
                        .accountNumber("1234567891")
                        .balance(2000L)
                        .build(),
                Account.builder()
                        .accountUser(accountUser1)
                        .accountNumber("1234567892")
                        .balance(3000L)
                        .build()
        );

        given(accountUserRepository.findById(anyLong()))
                .willReturn((Optional.of(accountUser1)));
        given(accountRepository.findByAccountUser(any()))
                .willReturn(accountList);
        // when
        List<AccountDto> accountDtoList = accountService.getAccountByUserId(12L);

        // then
        assertEquals(3, accountDtoList.size());
        assertEquals("1234567890", accountDtoList.get(0).getAccountNumber());
        assertEquals(1000L, accountDtoList.get(0).getBalance());
        assertEquals("1234567891", accountDtoList.get(1).getAccountNumber());
        assertEquals(2000L, accountDtoList.get(1).getBalance());
        assertEquals("1234567892", accountDtoList.get(2).getAccountNumber());
        assertEquals(3000L, accountDtoList.get(2).getBalance());
    }

    @Test
    void failedToGetAccounts() {
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        // when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.getAccountByUserId(1L));
        // then
        assertEquals(ErrorCode.USER_NOT_FOUND, accountException.getErrorCode());
    }
}
