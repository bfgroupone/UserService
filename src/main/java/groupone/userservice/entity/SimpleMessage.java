package groupone.userservice.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@Builder
@ToString
public class SimpleMessage implements Serializable {
    private String recipient;
    private String msgBody;
    private String subject;

//    @Override
//    public String toString() {
//        return "SimpleMessage{" +
//                "recipient='" + recipient + '\'' +
//                ", msgBody='" + msgBody + '\'' +
//                ", subject='" + subject + '\'' +
//                '}';
//    }
}
