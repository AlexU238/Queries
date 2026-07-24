package oleksii.queriestask.repository;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Getter
@Repository
public class JdbcTemplateRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcTemplateRepository( @Qualifier("clickhouseJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String,Object>> getQueryResultList(String sql){
        return jdbcTemplate.queryForList(sql);
    }

}
