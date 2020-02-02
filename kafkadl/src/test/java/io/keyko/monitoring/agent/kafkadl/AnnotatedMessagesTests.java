package io.keyko.monitoring.agent.kafkadl;

import io.keyko.monitoring.agent.kafkadl.message.AnnotatedDummyMessage;
import io.keyko.monitoring.agent.kafkadl.message.DummyDetails;
import io.keyko.monitoring.agent.kafkadl.message.DummyMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {TestApplication.class, TestConfiguration.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(locations="classpath:application-annotated-test.properties")
public class AnnotatedMessagesTests extends BaseKafkaDeadLetterTests {

    @Test
    public void testMessageFailureNonAnnotatedMessage() {
        configureErrorCriteria(true, null);
        final int expectedMessageCount = DEFAULT_INTERNAL_RETRIES;
        final DummyMessage message = sendMessageAndWait(expectedMessageCount, 0);

        assertEquals(expectedMessageCount, receivedMessagesOnMainTopic.size());
        assertEquals(message, receivedMessagesOnMainTopic.get(0));
    }

    @Test
    public void testMessageFailureAnnotatedMessage() throws Exception {
        configureErrorCriteria(true, null);
        final int expectedMessageCount =
                DEFAULT_INTERNAL_RETRIES * DEFAULT_DEADLETTER_RETRIES + DEFAULT_INTERNAL_RETRIES;

        final DummyDetails details = new DummyDetails();
        details.setStringValue(STRING_VALUE);
        details.setBigIntegerValue(BIG_INT_VALUE);

        final AnnotatedDummyMessage message = new AnnotatedDummyMessage();
        message.setDetails(details);

        sendMessage(message);
        waitForMessages(expectedMessageCount, 1);

        assertEquals(expectedMessageCount, receivedMessagesOnMainTopic.size());
        assertEquals(1, receivedMessagesOnErrorTopic.size());
        assertEquals(message.getDetails(), receivedMessagesOnErrorTopic.get(0).getDetails());
    }
}
