package oleksii.queriestask.controller;

import oleksii.queriestask.service.QueryService;
import oleksii.queriestask.service.RuntimeQueryService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(RuntimeQueryController.class)
public class RuntimeQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean(name = "runtimeQueryService")
    private QueryService service;

    @Test
    void addTest() throws Exception {

        Mockito.when(service.addQuery("SELECT * FROM test")).thenReturn(0L);

        mockMvc.perform(post("/queries")
                .contentType(MediaType.TEXT_PLAIN)
                .content("SELECT * FROM test"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(0));
    }

}
