package oleksii.queriestask.service;

import oleksii.queriestask.datamodel.Query;
import oleksii.queriestask.repository.JdbcTemplateRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RuntimeQueryServiceTest {

    @Mock
    JdbcTemplateRepository jdbcTemplateRepository;

    @InjectMocks
    RuntimeQueryService runtimeQueryService;

    static String testQueryContent;

    static Query testQuery1;
    static Query testQuery2;

    static List<Map<String, Object>> mockResult;

    @BeforeAll
    static void setup(){
        testQueryContent = "SELECT * FROM users";
        testQuery1=Query.builder().id(1L).query(testQueryContent).build();
        testQuery2=Query.builder().id(2L).query(testQueryContent).build();
        mockResult= List.of(
                Map.of("id", 1, "name", "TestName1"),
                Map.of("id", 2, "name", "TestName2"),
                Map.of("id", 3, "name", "TestName3")
        );
    }

    @Test
    void addOneQueryTest(){
        assertThat(runtimeQueryService.getQueriesToExecute().size()).isEqualTo(0);

        Long id = runtimeQueryService.addQuery(testQueryContent);

        assertThat(id).isEqualTo(0L);

        assertThat(runtimeQueryService.getQueriesToExecute().size()).isEqualTo(1);

        assertThat(runtimeQueryService.getQueries().stream().anyMatch(q->q.getId()==1L));

        assertThat(runtimeQueryService.getIdCounter()).isEqualTo(1);
    }

    @Test
    void addTwoQueryTest(){
        assertThat(runtimeQueryService.getQueriesToExecute().size()).isEqualTo(0L);

        runtimeQueryService.addQuery(testQueryContent);
        runtimeQueryService.addQuery(testQueryContent);

        assertThat(runtimeQueryService.getQueriesToExecute().size()).isEqualTo(1);

        assertThat(runtimeQueryService.getIdCounter()).isEqualTo(1L);
    }

    @Test
    void getAllQueriesToExecuteTest(){
        runtimeQueryService.getQueriesToExecute().add(testQuery1);
        runtimeQueryService.getQueriesToExecute().add(testQuery2);

        assertThat(runtimeQueryService.getQueriesToExecute().size()).isEqualTo(1);

        assertThat(runtimeQueryService.getQueries().size()).isEqualTo(runtimeQueryService.getQueriesToExecute().size());

        assertThat(runtimeQueryService.getQueriesToExecute()).contains(testQuery1);
    }

    @Test
    void getQueryResultsTest(){
        runtimeQueryService.getQueriesToExecute().add(testQuery1);
        runtimeQueryService.getQueriesToExecute().add(testQuery2);

        assertThat(runtimeQueryService.getQueriesToExecute().size()).isEqualTo(1);

        assertThat(runtimeQueryService.getQueriesToExecute().stream().anyMatch(q->q.getId()==1L));

        assertThat(runtimeQueryService.getQueriesExecuted().size()).isEqualTo(0);

        when(jdbcTemplateRepository.getQueryResultList(testQueryContent)).thenReturn(mockResult);

        Object[][] result = runtimeQueryService.getQueryResults(1L);

        assertThat(result.length).isEqualTo(3);

        assertThat(runtimeQueryService.getQueriesToExecute().size()).isEqualTo(1);

        assertThat(runtimeQueryService.getQueriesExecuted().size()).isEqualTo(1);
    }

    @Test
    void getQueryResultsNotFoundTest(){
        assertThat(runtimeQueryService.getQueriesToExecute().size()).isEqualTo(0);

        assertThrows(NullPointerException.class,() -> {runtimeQueryService.getQueryResults(0L);});
    }

    @Test
    void getQueryResultsFailTest(){
        runtimeQueryService.getQueriesToExecute().add(testQuery1);

        assertThat(runtimeQueryService.getQueriesToExecute().stream().anyMatch(q->q.getId()==1L));

        assertThat(runtimeQueryService.getQueriesExecuted().size()).isEqualTo(0);

        when(jdbcTemplateRepository.getQueryResultList(testQueryContent)).thenThrow(new DataAccessException("Test Exception") {});

        assertThrows(IllegalStateException.class,() -> {runtimeQueryService.getQueryResults(1L);});

        assertThat(runtimeQueryService.getQueriesToExecute().size()).isEqualTo(1);
    }
}
