package oleksii.queriestask.repository;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class JdbcTemplateRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private JdbcTemplateRepository repository;

    private final String sql = "SELECT * FROM users";

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
    void testGetQueryResultList_whenJdbcTemplateThrowsException() {
        when(jdbcTemplate.queryForList(sql)).thenThrow(new DataAccessException("Test Exception") {});

        assertThatThrownBy(() -> repository.getQueryResultList(sql))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("Test Exception");

        verify(jdbcTemplate, times(1)).queryForList(sql);
    }
}
