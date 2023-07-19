package groupone.userservice.dto.request;

import lombok.*;

import java.io.Serializable;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserRegistrationRequest implements Serializable {
    private String recipient;
    private String msgBody;
    private String subject;
}
