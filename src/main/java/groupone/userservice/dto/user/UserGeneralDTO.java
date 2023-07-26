package groupone.userservice.dto.user;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserGeneralDTO {
    private Integer userId;
    private String firstName;
    private String lastName;
    private String profileImageURL;
}
