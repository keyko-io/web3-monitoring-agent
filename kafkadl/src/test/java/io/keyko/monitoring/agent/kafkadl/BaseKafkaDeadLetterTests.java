package io.keyko.monitoring.agent.kafkadl;

import io.keyko.monitoring.agent.kafkadl.internal.DeadLetterTopicNameConvention;
import io.keyko.monitoring.agent.kafkadl.internal.util.JSON;
import io.keyko.monitoring.agent.kafkadl.message.DummyDetails;
import io.keyko.monitoring.agent.kafkadl.message.DummyMessage;
import io.keyko.monitoring.agent.kafkadl.message.TestMessage;
import junit.framework.TestCase;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.ClassRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.kafka.test.utils.ContainerTestUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class BaseKafkaDeadLetterTests {

    protected static final String KAFKA_LISTENER_CONTAINER_ID = "org.springframework.kafka.KafkaListenerEndpointContainer#0";

    protected static final int DEFAULT_INTERNAL_RETRIES = 3;

    protected static final int DEFAULT_DEADLETTER_RETRIES = 3;

    protected static final String STRING_VALUE = "0x8f981487bc415153a1a56cb5e7b17ec11e36bf2aeb6576bbb8fbf2e51ac9785c";

    protected static final BigInteger BIG_INT_VALUE = BigInteger.TEN;

    protected static boolean isBeforeFirstTest = true;

    @ClassRule
    public static EmbeddedKafkaRule embeddedKafka = new EmbeddedKafkaRule(1, true, 3, "testTopic");

    @LocalServerPort
    protected int port = 12345;

    @Autowired
    protected KafkaTemplate kafkaTemplate;

    @Autowired
    protected KafkaListenerEndpointRegistry registry;

    @Autowired
    protected DeadLetterTopicNameConvention dltNameConvention;

    @Autowired
    protected DummyRetriesExhaustedHandler retriesExhaustedHandler;

    protected List<TestMessage> receivedMessagesOnMainTopic;

    protected List<TestMessage> receivedMessagesOnErrorTopic;

    protected ErrorCriteria errorCriteria;

    @Before
    public void init() throws Exception {
        receivedMessagesOnMainTopic = new ArrayList<>();
        receivedMessagesOnErrorTopic = new ArrayList<>();
        retriesExhaustedHandler.getFailedRecords().clear();

        final MessageListenerContainer container = registry.getListenerContainer(KAFKA_LISTENER_CONTAINER_ID);

        // wait until the container has the required number of assigned partitions
        ContainerTestUtils.waitForAssignment(container, embeddedKafka.getEmbeddedKafka().getPartitionsPerTopic());

        if (isBeforeFirstTest) {
            isBeforeFirstTest = false;
            Thread.sleep(10000);
        }
    }

    @AfterClass
    public static void clearup() {
        isBeforeFirstTest = true;
    }

    @KafkaListener(topics = "testTopic", groupId = "testGroup")
    public void consumer(TestMessage message) {
        System.out.println(String.format("Message received: %s", JSON.stringify(message)));
        receivedMessagesOnMainTopic.add(message);

        if (errorCriteria.shouldError
                && (errorCriteria.recoverOnRetryNumber == null
                || !errorCriteria.recoverOnRetryNumber.equals(message.getRetries()))) {
            throw new IllegalStateException("Forced failure!");
        }
    }

    @KafkaListener(topics = "#{deadLetterTopicNameConvention.getErrorTopicName('testTopic')}",
            groupId = "testGroup")
    public void errorConsumer(TestMessage message) {
        System.out.println(String.format("Message received on error topic: %s", JSON.stringify(message)));
        receivedMessagesOnErrorTopic.add(message);
    }

    protected void doFailureWithDeadLetterRecovery(int retryNumberForRecovery) {
        configureErrorCriteria(true, retryNumberForRecovery);

        final DummyMessage message = sendMessage();

        assertCorrectMessagesReceived(message.getId(), retryNumberForRecovery);
    }

    protected DummyMessage sendMessage() {
        final DummyMessage message = createDummyMessage();

        sendMessage(message);

        return message;
    }

    protected void sendMessage(Object message) {
        kafkaTemplate.send("testTopic", message);
    }

    protected DummyMessage sendMessageAndWait(int expectedMessagesOnMainTopic,
                                              int expectedMessagesOnErrorTopic) {
        final DummyMessage message = sendMessage();
        waitForMessages(expectedMessagesOnMainTopic, expectedMessagesOnErrorTopic);

        return message;
    }

    protected DummyMessage createDummyMessage() {
        final DummyDetails details = new DummyDetails();
        details.setStringValue(STRING_VALUE);
        details.setBigIntegerValue(BIG_INT_VALUE);

        final DummyMessage message = new DummyMessage();
        message.setDetails(details);

        return message;
    }

    protected void configureErrorCriteria(boolean shouldError, Integer recoverOnRetryNumber) {
        errorCriteria = new ErrorCriteria(shouldError, recoverOnRetryNumber);
    }

    protected void waitForMessages(int expectedMessagesOnMainTopic,
                                   int expectedMessagesOnErrorTopic) {
        //Wait for an initial 2 seconds (this is usually enough time and is needed
        //in order to catch failures when no messages are expected on error topic but one arrives)
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Wait for another 8 seconds maximum if messages have not yet arrived
        final long startTime = System.currentTimeMillis();
        while(true) {
            if (receivedMessagesOnMainTopic.size() == expectedMessagesOnMainTopic
                    && receivedMessagesOnErrorTopic.size() == expectedMessagesOnErrorTopic) {
                break;
            }

            if (System.currentTimeMillis() > startTime + 8000) {
                final StringBuilder builder = new StringBuilder("Failed to receive all expected messages");
                builder.append("\n");
                builder.append("Expected main topic messages: " + expectedMessagesOnMainTopic);
                builder.append(", received: " + receivedMessagesOnMainTopic.size());
                builder.append("\n");
                builder.append("Expected error topic messages: " + expectedMessagesOnErrorTopic);
                builder.append(", received: " + receivedMessagesOnErrorTopic.size());

                TestCase.fail(builder.toString());
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected int getExpectedMessageCountOnRetryRecovery(int retryNumberForRecovery) {
        return DEFAULT_INTERNAL_RETRIES * retryNumberForRecovery + 1;
    }

    protected void assertCorrectMessagesReceived(String id, int retryNumberForRecovery) {
        waitForMessages(getExpectedMessageCountOnRetryRecovery(retryNumberForRecovery), 0);

        assertEquals(getExpectedMessageCountOnRetryRecovery(retryNumberForRecovery), receivedMessagesOnMainTopic.size());

        for(int i = 0; i < receivedMessagesOnMainTopic.size(); i++) {
            final TestMessage message = receivedMessagesOnMainTopic.get(i);
            assertEquals(id, message.getId());

            final Integer expectedRetryNumber = i / (DEFAULT_INTERNAL_RETRIES);

            assertEquals(expectedRetryNumber, message.getRetries());
        }
    }

    protected class ErrorCriteria {
        boolean shouldError;
        Integer recoverOnRetryNumber;

        private ErrorCriteria(boolean shouldError, Integer recoverOnRetryNumber) {
            this.shouldError = shouldError;
            this.recoverOnRetryNumber = recoverOnRetryNumber;
        }
    }
}
