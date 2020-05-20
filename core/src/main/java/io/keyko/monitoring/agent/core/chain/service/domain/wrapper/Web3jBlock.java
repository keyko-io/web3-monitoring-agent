package io.keyko.monitoring.agent.core.chain.service.domain.wrapper;

import io.keyko.monitoring.agent.core.chain.service.domain.Block;
import io.keyko.monitoring.agent.core.chain.service.domain.Transaction;
import io.keyko.monitoring.agent.core.utils.ModelMapperFactory;
import lombok.Data;
import org.modelmapper.ModelMapper;
import org.web3j.crypto.Keys;
import org.web3j.protocol.core.methods.response.EthBlock;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class Web3jBlock implements Block {

    private BigInteger number;
    private String hash;
    private String parentHash;
    private BigInteger nonce;
    private String sha3Uncles;
    private String logsBloom;
    private String transactionsRoot;
    private String stateRoot;
    private String receiptsRoot;
    private String author;
    private String miner;
    private String mixHash;
    private BigInteger difficulty;
    private BigInteger totalDifficulty;
    private String extraData;
    private BigInteger size;
    private BigInteger gasLimit;
    private BigInteger gasUsed;
    private BigInteger timestamp;
    private List<Transaction> transactions;
    private List<String> uncles;
    private List<String> sealFields;
    private String nodeName;

    public Web3jBlock(EthBlock.Block web3jBlock, String nodeName) {
        final ModelMapper modelMapper = ModelMapperFactory.getInstance().createModelMapper();
        modelMapper.typeMap(
                EthBlock.Block.class, Web3jBlock.class)
                .addMappings(mapper -> {
                    mapper.skip(Web3jBlock::setTransactions);
                    if (web3jBlock.getDifficultyRaw() == null) {
                        mapper.skip(Web3jBlock::setDifficulty);
                    }
                    if (web3jBlock.getGasLimitRaw() == null) {
                        mapper.skip(Web3jBlock::setGasLimit);
                    }
                    //Nonce can be null which throws exception in web3j when
                    //calling getNonce (because of attempted hex conversion)
                    if (web3jBlock.getNonceRaw() == null) {
                        mapper.skip(Web3jBlock::setNonce);
                    }
                });

        modelMapper.map(web3jBlock, this);

        transactions = convertTransactions(web3jBlock.getTransactions());

        this.nodeName = nodeName;
    }

    private List<Transaction> convertTransactions(List<EthBlock.TransactionResult> toConvert) {
        return toConvert.stream()
                .map(tx -> {
                    org.web3j.protocol.core.methods.response.Transaction transaction = (org.web3j.protocol.core.methods.response.Transaction) tx.get();

                    transaction.setFrom(Keys.toChecksumAddress(transaction.getFrom()));

                    if (transaction.getTo() != null && !transaction.getTo().isEmpty()) {
                        transaction.setTo(Keys.toChecksumAddress(transaction.getTo()));
                    }

                    return new Web3jTransaction(transaction);
                })
                .collect(Collectors.toList());
    }
}
