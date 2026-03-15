package com.zenz.neopay.service.blockchain.watcher;

import com.zenz.neopay.service.blockchain.event.TransactionCreatedEvent;
import com.zenz.neopay.service.blockchain.event.TransactionExecutedEvent;
import io.reactivex.disposables.Disposable;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.tx.Contract;

import java.math.BigInteger;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EthereumWatcherService implements WatcherService {

    private final Web3j web3j;

    @Value("${ethereum.contract.transaction-contract-address}")
    private String transactionContractAddress;

    private Disposable eventsSubscription;

    private static final Event TRANSACTION_CREATED_EVENT = new Event(
            "TransactionCreated",
            List.of(
                    TypeReference.create(Bytes32.class, true),
                    TypeReference.create(Utf8String.class),
                    TypeReference.create(Address.class, true),
                    TypeReference.create(Address.class, true),
                    TypeReference.create(Address.class),
                    TypeReference.create(Uint256.class)
            )
    );

    private static final Event TRANSACTION_EXECUTED_EVENT = new Event(
            "TransactionExecuted",
            List.of(
                    TypeReference.create(Bytes32.class, true),
                    TypeReference.create(Utf8String.class),
                    TypeReference.create(Address.class, true),
                    TypeReference.create(Address.class, true),
                    TypeReference.create(Address.class),
                    TypeReference.create(Uint256.class)
            )
    );

    @PostConstruct
    @Override
    public void start() {
        log.info("Starting EthereumWatcherService for contract {}", transactionContractAddress);

        String transactionCreatedTopic = EventEncoder.encode(TRANSACTION_CREATED_EVENT);
        String transactionExecutedTopic = EventEncoder.encode(TRANSACTION_EXECUTED_EVENT);

        EthFilter filter = new EthFilter(
                DefaultBlockParameterName.LATEST,
                DefaultBlockParameterName.LATEST,
                transactionContractAddress
        );

        // OR on topic0 (event signature)
        filter.addOptionalTopics(transactionCreatedTopic, transactionExecutedTopic);

        eventsSubscription = web3j.ethLogFlowable(filter).subscribe(
                this::handleLog,
                error -> log.error("Ethereum log subscription failed", error)
        );

        log.info("Subscription created successfully");
    }

    private void handleLog(Log logObject) {
        try {
            if (logObject.getTopics() == null || logObject.getTopics().isEmpty()) {
                log.warn("Received log with no topics. txHash={}", logObject.getTransactionHash());
                return;
            }

            String eventSignature = logObject.getTopics().get(0);

            if (EventEncoder.encode(TRANSACTION_CREATED_EVENT).equals(eventSignature)) {
                handleTransactionCreatedLog(logObject);
                return;
            }

            if (EventEncoder.encode(TRANSACTION_EXECUTED_EVENT).equals(eventSignature)) {
                handleTransactionExecutedLog(logObject);
                return;
            }

            log.warn("Received unknown event signature {} txHash={}", eventSignature, logObject.getTransactionHash());
        } catch (Exception e) {
            log.error("Failed to process log. txHash={}", logObject.getTransactionHash(), e);
        }
    }

    private void handleTransactionCreatedLog(Log logObject) {
        var eventValues = Contract.staticExtractEventParameters(TRANSACTION_CREATED_EVENT, logObject);

        if (eventValues == null) {
            log.warn("Could not decode TransactionCreated. txHash={}", logObject.getTransactionHash());
            return;
        }

        String transactionKey = ((Bytes32) eventValues.getIndexedValues().get(0)).getValue().toString();
        String sender = ((Address) eventValues.getIndexedValues().get(1)).getValue();
        String recipient = ((Address) eventValues.getIndexedValues().get(2)).getValue();

        String transactionId = ((Utf8String) eventValues.getNonIndexedValues().get(0)).getValue();
        String token = ((Address) eventValues.getNonIndexedValues().get(1)).getValue();
        BigInteger amount = ((Uint256) eventValues.getNonIndexedValues().get(2)).getValue();

        log.info("=== TransactionCreated Event Received ===");
        log.info("Contract       : {}", logObject.getAddress());
        log.info("Tx Hash        : {}", logObject.getTransactionHash());
        log.info("Block No       : {}", logObject.getBlockNumber());
        log.info("TransactionKey : {}", transactionKey);
        log.info("TransactionId  : {}", transactionId);
        log.info("Sender         : {}", sender);
        log.info("Recipient      : {}", recipient);
        log.info("Token          : {}", token);
        log.info("Amount         : {}", amount);
    }

    private void handleTransactionExecutedLog(Log logObject) {
        var eventValues = Contract.staticExtractEventParameters(TRANSACTION_EXECUTED_EVENT, logObject);

        if (eventValues == null) {
            log.warn("Could not decode TransactionExecuted. txHash={}", logObject.getTransactionHash());
            return;
        }

        String transactionKey = ((Bytes32) eventValues.getIndexedValues().get(0)).getValue().toString();
        String sender = ((Address) eventValues.getIndexedValues().get(1)).getValue();
        String recipient = ((Address) eventValues.getIndexedValues().get(2)).getValue();

        String transactionId = ((Utf8String) eventValues.getNonIndexedValues().get(0)).getValue();
        String token = ((Address) eventValues.getNonIndexedValues().get(1)).getValue();
        BigInteger amount = ((Uint256) eventValues.getNonIndexedValues().get(2)).getValue();

        log.info("=== TransactionExecuted Event Received ===");
        log.info("Contract       : {}", logObject.getAddress());
        log.info("Tx Hash        : {}", logObject.getTransactionHash());
        log.info("Block No       : {}", logObject.getBlockNumber());
        log.info("TransactionKey : {}", transactionKey);
        log.info("TransactionId  : {}", transactionId);
        log.info("Sender         : {}", sender);
        log.info("Recipient      : {}", recipient);
        log.info("Token          : {}", token);
        log.info("Amount         : {}", amount);
    }

    @Override
    public void handleTransactionCreated(TransactionCreatedEvent event) {
    }

    @Override
    public void handleTransactionExecuted(TransactionExecutedEvent event) {
    }

    @PreDestroy
    @Override
    public void stop() {
        if (eventsSubscription != null && !eventsSubscription.isDisposed()) {
            eventsSubscription.dispose();
        }
    }
}