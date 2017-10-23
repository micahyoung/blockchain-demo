package com.example.demo;

import com.example.demo.contracts.SimpleStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Controller
public class QuorumController {
    @Autowired
    QuorumService quorumService;


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


}
