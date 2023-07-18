package groupone.userservice.service;

import groupone.userservice.dto.response.AllHistoryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;


@FeignClient(name = "history-service", url="http://localhost:8083/history-service")
public interface RemoteHistoryService {

    @GetMapping("history/all")
    AllHistoryResponse getAllHistory();
}
