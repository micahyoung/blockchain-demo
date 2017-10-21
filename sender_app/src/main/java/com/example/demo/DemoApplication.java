package com.example.demo;

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

@Controller
@SpringBootApplication
public class DemoApplication {

    @Autowired
    EthereumService ethereumService;

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
    public String home(Model model) throws IOException {
        String senderAccount = ethereumService.getAccount(0);
        String receiverAccount = ethereumService.getAccount(1);

        model.addAttribute("senderAccount", senderAccount);
        model.addAttribute("receiverAccount", receiverAccount);
        model.addAttribute("senderBalance", ethereumService.getAccountBalance(senderAccount));
        model.addAttribute("receiverBalance", ethereumService.getAccountBalance(receiverAccount));
        ethereumService.getPendingTransaction(senderAccount).ifPresent(transaction -> {
            model.addAttribute("pendingTransactionFrom", transaction.getFrom());
            model.addAttribute("pendingTransactionTo", transaction.getTo());
            model.addAttribute("pendingTransactionValue", transaction.getValue());
        });
        ethereumService.getLastTransaction(senderAccount).ifPresent(transaction -> {
            model.addAttribute("lastTransactionFrom", transaction.getFrom());
            model.addAttribute("lastTransactionTo", transaction.getTo());
            model.addAttribute("lastTransactionValue", transaction.getValue());
        });


        return "home";
    }


    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}