package cloud.study;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;

/**
 * TODO
 **/
@EnableBinding(Source.class)
public class MessageProvider {

    @Autowired
    private MessageChannel output;

    public String sendMsg() {
        String msg = "Hello,Spring-Cloud-Stream";
        output.send(MessageBuilder.withPayload(msg).build());
        return null;
    }
}
