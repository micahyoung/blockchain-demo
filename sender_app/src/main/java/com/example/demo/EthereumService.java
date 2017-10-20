package com.example.demo;

import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.parity.Parity;
import org.web3j.protocol.parity.methods.response.PersonalUnlockAccount;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public class EthereumService {
    static final BigInteger GAS_PRICE = BigInteger.valueOf(90_000);
    static final BigInteger GAS_LIMIT = BigInteger.valueOf(90_000);

    private Parity parity;
    private String senderAccountPassword;

    public EthereumService(String nodeUrl, String senderAccountPassword) {
        this.senderAccountPassword = senderAccountPassword;
        this.parity = Parity.build(new HttpService(nodeUrl));
    }

    public String getAccount(Integer accountIndex) throws IOException {
        String account = getAccounts().get(accountIndex);

        return account;
    }

    public Boolean unlockAccount(String account) throws IOException {
        PersonalUnlockAccount personalUnlockAccount = parity.personalUnlockAccount(account, senderAccountPassword, BigInteger.valueOf(30)).send();

        if (!personalUnlockAccount.accountUnlocked()) {
            throw new IOException();
        }

        return true;
    }

    private List<String> getAccounts() throws IOException {
        return parity.ethAccounts().send().getAccounts();
    }

    private BigInteger getNonce(String account) throws IOException {
        EthGetTransactionCount ethGetTransactionCount = parity.ethGetTransactionCount(
                account, DefaultBlockParameterName.LATEST).send();

        BigInteger nonce = ethGetTransactionCount.getTransactionCount();

        return nonce;
    }

    public Transaction buildTransaction(String senderAccount, String receiverAccount, BigInteger value) throws IOException {
        BigInteger nonce = getNonce(senderAccount);

        Transaction transaction = Transaction.createEtherTransaction(
                senderAccount,
                nonce,
                EthereumService.GAS_PRICE,
                EthereumService.GAS_LIMIT,
                receiverAccount,
                value
        );

        return transaction;
    }

    public EthSendTransaction sendTransaction(Transaction transaction) throws IOException {
        return parity.ethSendTransaction(transaction).send();
    }

    public String getAccountBalance(String account) throws IOException {
        return parity.ethGetBalance(
                account, DefaultBlockParameterName.LATEST).send().getBalance().toString();
    }


    public Optional<org.web3j.protocol.core.methods.response.Transaction> getLastTransaction(String account) throws IOException {
        return parity.ethGetTransactionByBlockNumberAndIndex(DefaultBlockParameterName.LATEST, BigInteger.ZERO).send().getTransaction();
    }

    public Optional<org.web3j.protocol.core.methods.response.Transaction> getPendingTransaction(String senderAccount) throws IOException {
        return parity.ethGetTransactionByBlockNumberAndIndex(DefaultBlockParameterName.PENDING, BigInteger.ZERO).send().getTransaction();
    }
}
