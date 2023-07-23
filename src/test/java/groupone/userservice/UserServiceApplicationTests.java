package groupone.userservice;

import groupone.userservice.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = TestSecurityConfig.class)
class UserServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
