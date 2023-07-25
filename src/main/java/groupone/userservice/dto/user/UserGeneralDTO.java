package groupone.userservice.dto.user;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserGeneralDTO {
    private String firstName;
    private String lastName;
    private String profileImageURL;
}
