package groupone.userservice.dto.request;

import lombok.*;

@Data
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPatchRequest {
    String email;
    String firstName;
    String lastName;
    String password;
    String profileImageURL;
}