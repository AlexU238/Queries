package oleksii.queriestask.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Repository
public class JdbcTemplateRepository {

    JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcTemplateRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public List<Map<String,Object>> getQueryResultList(String sql){
        return jdbcTemplate.queryForList(sql);
    }

}
