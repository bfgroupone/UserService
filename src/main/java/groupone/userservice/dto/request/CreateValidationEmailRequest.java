package groupone.userservice.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class CreateValidationEmailRequest {
    @NotNull(message = "User ID is required")
    private int userId;
}
