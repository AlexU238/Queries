package oleksii.queriestask.service;

import oleksii.queriestask.repository.JdbcTemplateRepository;
import oleksii.queriestask.repository.QueryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import oleksii.queriestask.datamodel.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class AnalyticQueryService implements QueryService { //add remove query

    private final JdbcTemplateRepository jdbcTemplateRepository;

    private final QueryRepository queryRepository;


    @Autowired
    public AnalyticQueryService(JdbcTemplateRepository jdbcTemplateRepository, QueryRepository queryRepository) {
        this.queryRepository = queryRepository;
        this.jdbcTemplateRepository = jdbcTemplateRepository;
    }

    @Override
    public Long addQuery(Query query) {
        String normalizedSql = query.getQuery()
                .replaceAll("\\s+", " ") // Replaces all spaces/newlines with a single space
                .trim()
                .toLowerCase();
        query.setQuery(normalizedSql);
        queryRepository.save(query);

        return query.getId();
    }

    @Override
    public Collection<Query> getQueries() {
        return queryRepository.findAll();
    }

    @Override
    public List<Map<String, Object>> getQueryResults(long id) {

        Optional<Query> toExecute = queryRepository.findById(id);

        List<Map<String, Object>> result;

        if(toExecute.isPresent()){
            result = jdbcTemplateRepository.getQueryResultList(toExecute.get().getQuery());
        }else{
            throw new NoSuchElementException();
        }

        return result;
    }

}
