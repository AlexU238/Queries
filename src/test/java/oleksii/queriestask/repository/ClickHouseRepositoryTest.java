package oleksii.queriestask.repository;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ClickHouseRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private ClickHouseRepository repository;

    private final String sql = "SELECT * FROM USERS";

    @Test
    void testGetQueryResultList_ReturnsEmptyList() {
        List<Map<String, Object>> emptyList = List.of();
        when(jdbcTemplate.queryForList(sql)).thenReturn(emptyList);

        List<Map<String, Object>> result = repository.getQueryResultList(sql);

        assertThat(result).isEmpty();
        verify(jdbcTemplate, times(1)).queryForList(sql);
    }

    @Test
    void testGetQueryResultList() {
        List<Map<String, Object>> expected = new ArrayList<>();
        expected.add(Map.of("id", 1, "name", "Alice"));

        when(jdbcTemplate.queryForList(sql)).thenReturn(expected);

        List<Map<String, Object>> result = repository.getQueryResultList(sql);

        assertThat(result).isEqualTo(expected);
        verify(jdbcTemplate, times(1)).queryForList(sql);
    }

    @Test
    void testComplexQuery() {
        List<Map<String, Object>> mockDbResult = List.of(
                Map.of("id", 1, "name", "Bob", "age", 30),
                Map.of("id", 2, "name", "Charlie", "age", 27)
        );

        String sql = "SELECT * FROM USERS WHERE age >= 27";
        when(jdbcTemplate.queryForList(sql)).thenReturn(mockDbResult);

        List<Map<String, Object>> result = repository.getQueryResultList(sql);

        assertThat(result)
                .hasSize(2)
                .extracting(m -> m.get("name"))
                .containsExactlyInAnyOrder("Bob", "Charlie");

        verify(jdbcTemplate, times(1)).queryForList(sql);
    }

}
