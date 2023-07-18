package groupone.userservice.dto.response;


import groupone.userservice.entity.History;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class AllHistoryResponse {
    private List<History> historylist;
}
