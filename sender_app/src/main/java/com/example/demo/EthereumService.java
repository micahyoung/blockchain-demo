package com.example.demo;

import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.parity.Parity;
import org.web3j.protocol.parity.methods.response.PersonalUnlockAccount;
import rx.Subscription;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

public class EthereumService {
    static final BigInteger GAS_PRICE = BigInteger.valueOf(90_000);
    static final BigInteger GAS_LIMIT = BigInteger.valueOf(90_000);
    private final Subscription subcription;
    private List<String> transactionHashes;

    private Parity parity;
    private String senderAccountPassword;

    public EthereumService(String nodeUrl, String senderAccountPassword) {
        this.senderAccountPassword = senderAccountPassword;
        this.parity = Parity.build(new HttpService(nodeUrl));
    }

    public String getSenderAccount() throws IOException {
        String account = getAccounts().get(0);

        PersonalUnlockAccount personalUnlockAccount = parity.personalUnlockAccount(account, senderAccountPassword).send();
        if (!personalUnlockAccount.accountUnlocked()) {
            return "account did not unlock";
        }

        return account;
    }


    public String getReceiverAccount() throws IOException {
        return getAccounts().get(1);
    }

    private List<String> getAccounts() throws IOException {
        return parity.ethAccounts().send().getAccounts();
    }

    private BigInteger getNonce() throws IOException {
        EthGetTransactionCount ethGetTransactionCount = parity.ethGetTransactionCount(
                getSenderAccount(), DefaultBlockParameterName.LATEST).send();

        BigInteger nonce = ethGetTransactionCount.getTransactionCount();

        return nonce;
    }

    public Transaction buildTransaction(String senderAccount, String receiverAccount, BigInteger value) throws IOException {
        BigInteger nonce = getNonce();

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

    public String getLatestBlock() throws IOException {
        return parity.ethGetBlockByNumber(
                DefaultBlockParameterName.LATEST, true).send().getBlock().getHash();
    }
}
