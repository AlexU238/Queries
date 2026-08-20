package oleksii.queriestask.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import oleksii.queriestask.datamodel.Query;
import oleksii.queriestask.service.StreamingQueryService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnalyticQueryController.class)
public class AnalyticQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean(name = "analyticQueryService")
    private StreamingQueryService service;

    private static final String PATH = "/queries";

    private static final String PATH_TO_ZERO_ID_RESULT = "/queries/0/results";

    private static final String PATH_TO_ZERO_ID_RESULT_STREAM = "/queries/0/results/stream";

    private static final String QUERY = "SELECT * FROM test";

    private static final Query testQuery = Query.builder().id(1L).query(QUERY).build();

    @Test
    void testAdd() throws Exception {
        when(service.addQuery(testQuery)).thenReturn(1L);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(testQuery);
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/queries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(1)));
    }

    @Test
    void testFindAll() throws Exception {
        when(service.getQueries()).thenReturn(
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

    @Test
    void testExecuteById() throws Exception {
        List<Map<String, Object>> result = List.of(
                Map.of(
                        "id", 1,
                        "status", 0,
                        "name", "Test",
                        "type", 'T'
                )
        );

        when(service.getQueryResults(0L)).thenReturn(result);

        mockMvc.perform(get(PATH_TO_ZERO_ID_RESULT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value(0))
                .andExpect(jsonPath("$[0].name").value("Test"))
                .andExpect(jsonPath("$[0].type").value("T"));
    }

    @Test
    void testExecuteByIdNotFound() throws Exception {
        when(service.getQueryResults(0L)).thenThrow(NullPointerException.class);

        mockMvc.perform(get(PATH_TO_ZERO_ID_RESULT)).andExpect(status().isNotFound());
    }

    @Test
    void testExecuteByIdBadRequest() throws Exception {
        when(service.getQueryResults(0L)).thenThrow(IllegalStateException.class);

        mockMvc.perform(get(PATH_TO_ZERO_ID_RESULT)).andExpect(status().isBadRequest());
    }

    @Test
    void testStreamQueryResultsSuccess() throws Exception {
        Long id = 0L;
        when(service.getQueryById(id)).thenReturn(Optional.of(new Query()));

        String mockJson = "[{\"id\":1,\"status\":0,\"name\":\"Test\",\"type\":\"T\"}]";
        doAnswer(invocation -> {
            OutputStream os = invocation.getArgument(1);
            os.write(mockJson.getBytes(StandardCharsets.UTF_8));
            return null;
        }).when(service).streamQueryResults(eq(id), any(OutputStream.class));

        MvcResult result = mockMvc.perform(get(PATH_TO_ZERO_ID_RESULT_STREAM))
                .andExpect(request().asyncStarted())
                .andReturn();

        //Dispatch async event and verify stream contents
        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value(0))
                .andExpect(jsonPath("$[0].name").value("Test"))
                .andExpect(jsonPath("$[0].type").value("T"));
    }

    @Test
    void testStreamQueryResultsServiceException() throws Exception {
        long id = 0L;
        when(service.getQueryById(id)).thenReturn(Optional.of(new Query()));

        doThrow(new IllegalStateException("ClickHouse failure"))
                .when(service).streamQueryResults(eq(id), any(OutputStream.class));

        MvcResult result = mockMvc.perform(get(PATH_TO_ZERO_ID_RESULT_STREAM))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }

    @Test
    void testStreamQueryResultsNotFound() throws Exception {
        Long id = 0L;
        when(service.getQueryById(id)).thenReturn(Optional.empty());

        mockMvc.perform(get(PATH_TO_ZERO_ID_RESULT_STREAM)).andExpect(status().isNotFound());
    }
}
