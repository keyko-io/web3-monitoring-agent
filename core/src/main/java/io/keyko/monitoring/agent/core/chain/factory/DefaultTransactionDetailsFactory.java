package io.keyko.monitoring.agent.core.chain.factory;

import io.keyko.monitoring.agent.core.chain.service.domain.Transaction;
import io.keyko.monitoring.agent.core.dto.transaction.TransactionDetails;
import io.keyko.monitoring.agent.core.dto.transaction.TransactionStatus;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Keys;

import java.math.BigInteger;

@Component
public class DefaultTransactionDetailsFactory implements TransactionDetailsFactory {

    private ModelMapper modelMapper = new ModelMapper();

    @Override
    public TransactionDetails createTransactionDetails(
            Transaction transaction, TransactionStatus status, String nodeName) {

        final TransactionDetails transactionDetails = new TransactionDetails();
        transactionDetails.setBlockHash(transaction.getBlockHash());
        transactionDetails.setHash(transaction.getHash());
        transactionDetails.setNonce(transaction.getNonce());
        transactionDetails.setContractAddress("");
        transactionDetails.setBlockNumber(new BigInteger(transaction.getBlockNumber().substring(2), 16));
        transactionDetails.setTransactionIndex(transaction.getTransactionIndex());
        transactionDetails.setFrom(transaction.getFrom());
        transactionDetails.setTo(transaction.getTo());
        transactionDetails.setValue(transaction.getValue());
        transactionDetails.setNodeName(nodeName);
        transactionDetails.setInput(transaction.getInput());
        transactionDetails.setRevertReason("");
        transactionDetails.setStatus(status);

        if (transaction.getCreates() != null) {
            transactionDetails.setContractAddress(Keys.toChecksumAddress(transaction.getCreates()));
        }

        return transactionDetails;
    }
}
