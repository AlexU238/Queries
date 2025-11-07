package oleksii.queriestask.controller;

import oleksii.queriestask.datamodel.Query;
import oleksii.queriestask.service.QueryService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(RuntimeQueryController.class)
public class RuntimeQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean(name = "runtimeQueryService")
    private QueryService service;

    private static final String PATH = "/queries";

    private static final String QUERY = "SELECT * FROM test";

    @Test
    void addTest() throws Exception {

        Mockito.when(service.addQuery(QUERY)).thenReturn(0L);

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(QUERY))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(0));
    }

    @Test
    void findAllTest() throws Exception {

        Mockito.when(service.getQueries()).thenReturn(
                List.of(
                        Query.builder().id(0L).query(QUERY).build(),
                        Query.builder().id(1L).query(QUERY).build()
                ));

        mockMvc.perform(get(PATH))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(0))
                .andExpect(jsonPath("$[0].query").value(QUERY))
                .andExpect(jsonPath("$[1].id").value(1L))
                .andExpect(jsonPath("$[1].query").value(QUERY));

    }

}
