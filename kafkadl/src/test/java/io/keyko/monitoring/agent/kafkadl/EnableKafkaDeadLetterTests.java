package io.keyko.monitoring.agent.kafkadl;

import io.keyko.monitoring.agent.kafkadl.message.DummyDetails;
import io.keyko.monitoring.agent.kafkadl.message.DummyMessage;
import io.keyko.monitoring.agent.kafkadl.message.DummyMessageNoInterface;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {TestApplication.class, TestConfiguration.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(locations="classpath:application-test.properties")
public class EnableKafkaDeadLetterTests extends BaseKafkaDeadLetterTests {

    @Test
    public void testMessageProcessedCorrectly() {
        configureErrorCriteria(false, null);

        final DummyMessage message = sendMessageAndWait(1, 0);

        assertEquals(1, receivedMessagesOnMainTopic.size());
        assertEquals(message.getId(), receivedMessagesOnMainTopic.get(0).getId());
    }

    @Test
    public void testMessageFailureRecoverOnFirstDeadLetterRetry() {
        doFailureWithDeadLetterRecovery(1);
    }

    @Test
    public void testMessageFailureRecoverOnSecondDeadLetterRetry() {
        doFailureWithDeadLetterRecovery(2);
    }

    @Test
    public void testMessageFailureRecoverOnThirdDeadLetterRetry() {
        doFailureWithDeadLetterRecovery(3);
    }

    @Test
    public void testMessageFailureWithNoRecovery() {
        doFailureWithNoRecoveryTest();
    }

    @Test
    public void testNonInterfaceImplementingMessageFailureWithNoRecovery() {
        configureErrorCriteria(true, null);
        final int expectedMessageCount =
                DEFAULT_INTERNAL_RETRIES * DEFAULT_DEADLETTER_RETRIES + DEFAULT_INTERNAL_RETRIES;

        final DummyDetails details = new DummyDetails();
        details.setStringValue(STRING_VALUE);
        details.setBigIntegerValue(BIG_INT_VALUE);

        final DummyMessageNoInterface message = new DummyMessageNoInterface();
        message.setDetails(details);

        sendMessage(message);
        waitForMessages(expectedMessageCount, 1);

        assertEquals(expectedMessageCount, receivedMessagesOnMainTopic.size());
        assertEquals(1, receivedMessagesOnErrorTopic.size());
        assertEquals(message.getDetails(), receivedMessagesOnErrorTopic.get(0).getDetails());
    }

    @Test
    public void testDltNameConventionDLTSuffixContainsServiceId() {
        assertTrue(dltNameConvention.getDeadLetterTopicSuffix().contains("TestApplication"));
    }

    @Test
    public void testDltNameConventionErrorSuffixContainsServiceId() {
        assertTrue(dltNameConvention.getErrorTopicSuffix().contains("TestApplication"));
    }

    @Test
    public void testMessageFailureTriggersRetriesExhaustedHandler() {
        final DummyMessage message = doFailureWithNoRecoveryTest();

        message.setRetries(3);
        assertEquals(1, retriesExhaustedHandler.getFailedRecords().size());
        assertEquals(message, retriesExhaustedHandler.getFailedRecords().get(0).value());
    }

    private DummyMessage doFailureWithNoRecoveryTest() {
        configureErrorCriteria(true, null);
        final int expectedMessageCount =
                DEFAULT_INTERNAL_RETRIES * DEFAULT_DEADLETTER_RETRIES + DEFAULT_INTERNAL_RETRIES;
        final DummyMessage message = sendMessageAndWait(expectedMessageCount, 1);

        assertEquals(expectedMessageCount, receivedMessagesOnMainTopic.size());
        assertEquals(1, receivedMessagesOnErrorTopic.size());
        assertEquals(message.getId(), receivedMessagesOnErrorTopic.get(0).getId());

        return message;
    }

}
