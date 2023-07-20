package groupone.userservice.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewMessageRequest {
    private String recipient;
    private String msgBody;
    private String subject;

}
