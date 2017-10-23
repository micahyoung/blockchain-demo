package com.example.demo;

import com.example.demo.contracts.SimpleStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Controller
@SpringBootApplication
public class DemoApplication {

    @Autowired
    EthereumService ethereumService;

    @Autowired
    QuorumService quorumService;

    @RequestMapping(value = "/send", method = RequestMethod.POST)
    public String send(@RequestParam("wei") String wei, RedirectAttributes redirAttrs) throws IOException {
        String senderAccount = ethereumService.getAccount(0);
        String receiverAccount = ethereumService.getAccount(1);
        BigInteger value = Convert.toWei(wei, Convert.Unit.WEI).toBigInteger();

        ethereumService.unlockAccount(senderAccount);

        Transaction transaction = ethereumService.buildTransaction(senderAccount, receiverAccount, value);

        Optional.of(ethereumService.sendTransaction(transaction)).ifPresent(ethSendTransaction -> {
            Optional.ofNullable(ethSendTransaction.getTransactionHash())
                    .ifPresent(hash -> redirAttrs.addAttribute("transactionHash", hash));

            Optional.ofNullable(ethSendTransaction.getError())
                    .ifPresent(error -> redirAttrs.addAttribute("transactionError", error.getMessage()));
        });

        return "redirect:/";
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String pub(Model model) throws IOException {
        String senderAccount = ethereumService.getAccount(0);
        String receiverAccount = ethereumService.getAccount(1);

        model.addAttribute("senderAccount", senderAccount);
        model.addAttribute("receiverAccount", receiverAccount);
        model.addAttribute("senderBalance", ethereumService.getAccountBalance(senderAccount));
        model.addAttribute("receiverBalance", ethereumService.getAccountBalance(receiverAccount));
        ethereumService.getPendingTransaction().ifPresent(transaction -> {
            model.addAttribute("pendingTransactionFrom", transaction.getFrom());
            model.addAttribute("pendingTransactionTo", transaction.getTo());
            model.addAttribute("pendingTransactionValue", transaction.getValue());
        });
        ethereumService.getLastTransaction().ifPresent(transaction -> {
            model.addAttribute("lastTransactionFrom", transaction.getFrom());
            model.addAttribute("lastTransactionTo", transaction.getTo());
            model.addAttribute("lastTransactionValue", transaction.getValue());
        });


        return "pub";
    }

    @RequestMapping(value = "/privSend", method = RequestMethod.POST)
    public String privateSend(@RequestParam("receiverPubKey") String receiverPubKey,
                              @RequestParam("value") BigInteger value,
                              RedirectAttributes redirAttrs) throws IOException, ExecutionException, InterruptedException {
        String senderAccount = quorumService.getAccount(0);
        redirAttrs.addAttribute("receiverPubKey", receiverPubKey);

        quorumService.deployContract(senderAccount, receiverPubKey)
                .ifPresent(hash -> redirAttrs.addAttribute("contractAddress", hash.getContractAddress()));

        return "redirect:/priv";
    }


    @RequestMapping(value = "/priv", method = RequestMethod.GET)
    public String priv(@RequestParam(value = "contractAddress", required = false, defaultValue = "") String contractAddress,
                       @RequestParam(value = "receiverPubKey", required = false, defaultValue = "") String receiverPubKey,
                       Model model) throws IOException, ExecutionException, InterruptedException {

        String senderAccount = quorumService.getAccount(0);
        model.addAttribute("senderAccount", senderAccount);
        if (!contractAddress.isEmpty()) {

            SimpleStorage simpleStorage = quorumService.getContract(contractAddress, receiverPubKey);
            model.addAttribute("lastContractAddress", simpleStorage.getContractAddress());
            Optional.ofNullable(simpleStorage.get().get()).ifPresent(storageContractFuture -> {
                model.addAttribute("lastTransactionValue", storageContractFuture.getValue());
            });
        }

        return "priv";
    }


    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}