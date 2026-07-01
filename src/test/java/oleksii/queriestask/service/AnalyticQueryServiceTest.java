package oleksii.queriestask.service;

import oleksii.queriestask.datamodel.Query;
import oleksii.queriestask.repository.JdbcTemplateRepository;
import oleksii.queriestask.repository.QueryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AnalyticQueryServiceTest {

    @Mock
    private QueryRepository queryRepository;

    @Mock
    private JdbcTemplateRepository jdbcTemplateRepository;

    @InjectMocks
    private AnalyticQueryService service;

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
    public void testGetQueryResults_Success() {
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
        when(jdbcTemplateRepository.getQueryResultList(mockQuery.getQuery())).thenReturn(mockResult);

        List<Map<String, Object>> actualResult = service.getQueryResults(queryId);

        assertNotNull(actualResult);
        assertEquals(1, actualResult.size());
        assertEquals("john_doe", actualResult.get(0).get("username"));

        verify(queryRepository, times(1)).findById(queryId);
        verify(jdbcTemplateRepository, times(1)).getQueryResultList(mockQuery.getQuery());
    }

    @Test
    public void testGetQueryResults_NotFound_ThrowsException() {
        long nonExistentId = 999L;
        when(queryRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> {
            service.getQueryResults(nonExistentId);
        });

        verify(queryRepository, times(1)).findById(nonExistentId);
        verifyNoInteractions(jdbcTemplateRepository);
    }
}
