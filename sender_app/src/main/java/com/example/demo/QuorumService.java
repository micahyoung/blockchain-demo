package com.example.demo;

import com.example.demo.contracts.SimpleStorage;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.http.HttpService;
import org.web3j.quorum.Quorum;
import org.web3j.quorum.tx.ClientTransactionManager;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class QuorumService {
    static final BigInteger GAS_LIMIT = BigInteger.valueOf(300_000);

    private final Quorum quorum;

    public QuorumService(String nodeUrl) {
        this.quorum = Quorum.build(new HttpService(nodeUrl));
    }

    public String getAccount(Integer accountIndex) throws IOException {
        String account = getAccounts().get(accountIndex);

        return account;
    }

    private List<String> getAccounts() throws IOException {
        return quorum.ethAccounts().send().getAccounts();
    }

    public Optional<SimpleStorage> deployContract(String senderAccount, String receiverPubKey) throws IOException, ExecutionException, InterruptedException {
        ClientTransactionManager transactionManager = getClientTransactionManager(senderAccount, receiverPubKey);

        return Optional.ofNullable(SimpleStorage.deploy(quorum, transactionManager, BigInteger.ZERO, GAS_LIMIT, BigInteger.ZERO).get());
    }

    public SimpleStorage getContract(String contractAddress, String receiverPubKey) throws IOException {
        ClientTransactionManager transactionManager = getClientTransactionManager(getAccount(0), receiverPubKey);

        return SimpleStorage.load(contractAddress, quorum, transactionManager, BigInteger.ZERO, BigInteger.ZERO);
    }

    private ClientTransactionManager getClientTransactionManager(String senderAccount, String receiverPubKey) {
        return new ClientTransactionManager(
                quorum,
                senderAccount,
                Arrays.asList(receiverPubKey)
        );
    }

}
