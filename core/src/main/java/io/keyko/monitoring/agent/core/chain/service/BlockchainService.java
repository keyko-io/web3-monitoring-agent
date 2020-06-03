package io.keyko.monitoring.agent.core.chain.service;

import io.keyko.monitoring.agent.core.chain.contract.ContractEventListener;
import io.keyko.monitoring.agent.core.chain.service.domain.Block;
import io.keyko.monitoring.agent.core.chain.service.domain.TransactionReceipt;
import io.keyko.monitoring.agent.core.dto.event.filter.ContractEventFilter;
import io.keyko.monitoring.agent.core.chain.block.BlockListener;
import io.keyko.monitoring.agent.core.model.EventFilterSubscription;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.core.DefaultBlockParameter;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

/**
 * Interface for a service that interacts directly with an Ethereum blockchain node.
 *
 * @author Craig Williams - craig.williams@consensys.net
 */
public interface BlockchainService {

    /**
     * @return The ethereum node name that this service is connected to.
     */
    String getNodeName();

    /**
     * Add a listener that gets notified when a new block is mined.
     *
     * @param blockListener the listener to add
     */
    void addBlockListener(BlockListener blockListener);

    /**
     * Remove a block listener than was previously added.
     *
     * @param blockListener the listener to remove
     */
    void removeBlockListener(BlockListener blockListener);

    /**
     * Register a contract event listener for the specified event filter, that gets triggered when an event
     * matching the filter is emitted within the Ethereum network.
     *
     * @param filter        The contract event filter that should be matched.
     * @param eventListener The listener to be triggered when a matching event is emitted
     * @return The registered subscription
     */
    EventFilterSubscription registerEventListener(ContractEventFilter filter, ContractEventListener eventListener);

    /**
     * @return the client version for the connected Ethereum node.
     */
    String getClientVersion();

    /**
     * @return the current block number of the network that the Ethereum node is connected to.
     */
    BigInteger getCurrentBlockNumber();

    /**
     * @param blockHash              The hash of the block to obtain
     * @param fullTransactionObjects If full transaction details should be populated
     * @return The block for the specified hash or nothing if a block with the specified hash does not exist.
     */
    Optional<Block> getBlock(String blockHash, boolean fullTransactionObjects);

    /**
     * @param blockNumber            The number of the block to retrieve
     * @param fullTransactionObjects If full transaction details should be populated
     * @return The block for the specified number or nothing if a block with the specified hash does not exist.
     */
    Optional<Block> getBlock(BigInteger blockNumber, boolean fullTransactionObjects);

    /**
     * Obtain the transaction receipt for a specified transaction id.
     *
     * @param txId the transaction id
     * @return the receipt for the transaction with the specified id.
     */
    TransactionReceipt getTransactionReceipt(String txId);

    /**
     * Connects to the Ethereum node and starts listening for new blocks
     */
    void connect();

    /**
     * Stops listening for new blocks from the ethereum node
     */
    void disconnect();

    /**
     * Reconnects to the Ethereum node (useful after node failure)
     */
    void reconnect();

    /**
     * @return true if the service is correctly connected to the ethereum node.
     */
    boolean isConnected();

    String getRevertReason(String from, String to, BigInteger blockNumber, String input);

    /**
     * Execute a remote read blockchain call
     *
     * @param contractAddress Smart Contract Address
     * @param function Function to call
     * @param blockNumber Specific Block Number related with the call
     * @return the list of returned values after the execution call
     */
    List<Type> executeReadCall(String contractAddress, Function function, BigInteger blockNumber);

    /**
     * Execute a remote read blockchain call
     *
     * @param contractAddress Smart Contract Address
     * @param function Function to call
     * @param blockParameter Specific Block Number related with the call
     * @return the list of returned values after the execution call
     */
    List<Type> executeReadCall(String contractAddress, Function function, DefaultBlockParameter blockParameter);

    /**
     * Execute a remote read blockchain call
     *
     * @param contractAddress Smart Contract Address
     * @param function Function to call
     * @return the list of returned values after the execution call
     */
    List<Type> executeReadCall(String contractAddress, Function function);
}
