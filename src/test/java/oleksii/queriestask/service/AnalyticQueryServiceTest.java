package oleksii.queriestask.service;

import com.clickhouse.client.api.ServerException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import oleksii.queriestask.datamodel.Query;
import oleksii.queriestask.repository.ClickHouseRepository;
import oleksii.queriestask.repository.QueryRepository;
import oleksii.queriestask.util.RowProcessor;
import oleksii.queriestask.util.factory.RowProcessorFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.datasource.init.UncategorizedScriptException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AnalyticQueryServiceTest {

    @Mock
    private QueryRepository queryRepository;

    @Mock
    private ClickHouseRepository clickHouseRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private RowProcessorFactory factory;

    @Mock
    private RowProcessor rowProcessor;

    @InjectMocks
    private AnalyticQueryService service;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testAddQuery() {
        Query inputQuery = new Query();
        inputQuery.setQuery("SELECT * FROM   users\nWHERE id = 1");

        when(queryRepository.save(any(Query.class))).thenAnswer(invocation -> {
            Query savedQuery = invocation.getArgument(0);
            savedQuery.setId(100L); // Simulating generated ID
            return savedQuery;
        });

        Long generatedId = service.addQuery(inputQuery);

        assertEquals(100L, generatedId);
        assertEquals("select * from users where id = 1", inputQuery.getQuery());
        verify(queryRepository, times(1)).save(inputQuery);
    }

    @Test
    public void testGetQueries() {
        Query query1 = new Query();
        Query query2 = new Query();
        List<Query> expectedQueries = Arrays.asList(query1, query2);

        when(queryRepository.findAll()).thenReturn(expectedQueries);

        Collection<Query> actualQueries = service.getQueries();

        assertEquals(2, actualQueries.size());
        verify(queryRepository, times(1)).findAll();
    }

    @Test
    public void testGetQueryResultsSuccess() {
        long queryId = 1L;
        Query mockQuery = new Query();
        mockQuery.setId(queryId);
        mockQuery.setQuery("select * from users");

        List<Map<String, Object>> mockResult = new ArrayList<>();
        Map<String, Object> row = new HashMap<>();
        row.put("id", 1);
        row.put("username", "john_doe");
        mockResult.add(row);

        when(queryRepository.findById(queryId)).thenReturn(Optional.of(mockQuery));
        when(clickHouseRepository.getQueryResultList(mockQuery.getQuery())).thenReturn(mockResult);

        List<Map<String, Object>> actualResult = service.getQueryResults(queryId);

        assertNotNull(actualResult);
        assertEquals(1, actualResult.size());
        assertEquals("john_doe", actualResult.get(0).get("username"));

        verify(queryRepository, times(1)).findById(queryId);
        verify(clickHouseRepository, times(1)).getQueryResultList(mockQuery.getQuery());
    }

    @Test
    public void testGetQueryResultsNotFoundThrowsException() {
        long nonExistentId = 999L;
        when(queryRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> {
            service.getQueryResults(nonExistentId);
        });

        verify(queryRepository, times(1)).findById(nonExistentId);
        verifyNoInteractions(clickHouseRepository);
    }

    @Test
    public void testGetQueryResultServerException() {
        long queryId = 1L;
        Query mockQuery = new Query();
        mockQuery.setId(queryId);
        mockQuery.setQuery("select * from users");

        List<Map<String, Object>> mockResult = new ArrayList<>();
        Map<String, Object> row = new HashMap<>();
        row.put("id", 1);
        row.put("username", "john_doe");
        mockResult.add(row);

        DataAccessException concreteException = new UncategorizedScriptException("ClickHouse syntax error", new RuntimeException());

        when(queryRepository.findById(queryId)).thenReturn(Optional.of(mockQuery));
        when(clickHouseRepository.getQueryResultList(mockQuery.getQuery())).thenThrow(concreteException);

        assertThrows(IllegalStateException.class, () -> {
            service.getQueryResults(1);
        });

    }

    @Test
    void testStreamQueryResultsSuccess() throws IOException, SQLException {
        long queryId = 1L;
        Query mockQuery = new Query();
        mockQuery.setId(queryId);
        mockQuery.setQuery("select * from users");

        when(factory.create(any(JsonGenerator.class)))
                .thenAnswer(invocation ->
                        new RowProcessor(invocation.getArgument(0))
                );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        when(queryRepository.findById(queryId)).thenReturn(Optional.of(mockQuery));
        when(clickHouseRepository.getJdbcTemplate()).thenReturn(jdbcTemplate);

        // Mock ResultSet & Metadata
        ResultSet mockRs = mock(ResultSet.class);
        ResultSetMetaData mockMetaData = mock(ResultSetMetaData.class);

        when(mockRs.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getColumnCount()).thenReturn(2);
        when(mockMetaData.getColumnLabel(1)).thenReturn("id");
        when(mockMetaData.getColumnLabel(2)).thenReturn("name");

        when(mockRs.getObject(1)).thenReturn(1L);
        when(mockRs.getObject(2)).thenReturn("Alice");

        // Intercept JdbcTemplate and execute the real RowProcessor
        doAnswer(invocation -> {
            RowCallbackHandler handler = invocation.getArgument(1);
            handler.processRow(mockRs);
            return null;
        }).when(jdbcTemplate).query(eq(mockQuery.getQuery()), any(RowCallbackHandler.class));

        service.streamQueryResults(queryId, outputStream);

        ObjectMapper mapper = new ObjectMapper();

        JsonNode expected =
                mapper.readTree("[{\"id\":1,\"name\":\"Alice\"}]");

        JsonNode actual =
                mapper.readTree(outputStream.toString(StandardCharsets.UTF_8));

        assertEquals(expected, actual);
    }

    @Test
    void testStreamQueryResultsFail(){

        long queryId = 1L;
        Query mockQuery = new Query();
        mockQuery.setId(queryId);
        mockQuery.setQuery("select * from users");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        when(queryRepository.findById(queryId)).thenReturn(Optional.of(mockQuery));
        when(clickHouseRepository.getJdbcTemplate()).thenReturn(jdbcTemplate);

        doAnswer(invocation -> {
            RowCallbackHandler handler = invocation.getArgument(1);
            ResultSet mockResultSet = mock(ResultSet.class);

            // Mock ResultSet to throw an exception when reading columns/data
            when(mockResultSet.getMetaData()).thenThrow(new SQLException("Exception"));

            // Triggers processRow which catches exception and rethrows
            handler.processRow(mockResultSet);
            return null;
        }).when(jdbcTemplate).query(eq(mockQuery.getQuery()), any(RowCallbackHandler.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                service.streamQueryResults(queryId, outputStream)
        );

        // Verifies the outer streamQueryResults try-catch block caught the row failure
        assertEquals("Streaming failed", exception.getMessage());
    }

}
