package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigInteger;
import java.util.stream.Collectors;

@RestController
@SpringBootApplication
public class DemoApplication {

    @Autowired
    EthereumService ethereumService;

    @RequestMapping("/send")
    public String home(@RequestParam("wei") String wei) throws IOException {
        BigInteger value = Convert.toWei(wei, Convert.Unit.WEI).toBigInteger();

        String senderAccount = ethereumService.getSenderAccount();

        String receiverAccount = ethereumService.getReceiverAccount();

        Transaction transaction = ethereumService.buildTransaction(senderAccount, receiverAccount, value);

        EthSendTransaction transactionResponse = ethereumService.sendTransaction(transaction);

        return "sender:" + senderAccount + " receiver:" + receiverAccount + " value:" + value + " result:" + transactionResponse.getResult() + " hash:" + transactionResponse.getTransactionHash();
    }

    @RequestMapping("/status")
    public String home() throws IOException {
        String senderAccount = ethereumService.getSenderAccount();
        String receiverAccount = ethereumService.getReceiverAccount();

        String senderBalance = ethereumService.getAccountBalance(senderAccount);
        String receiverBalance = ethereumService.getAccountBalance(receiverAccount);

        String latestHashes = ethereumService.getTransactionHashes().stream().collect(Collectors.joining(", "));

        return "sender:" + senderAccount + " sender balance:" + senderBalance + " receiver:" + receiverAccount + " receiver_balance:" + receiverBalance + " latestHashes:" + latestHashes;
    }


    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}