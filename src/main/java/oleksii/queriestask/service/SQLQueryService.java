package oleksii.queriestask.service;

import oleksii.queriestask.repository.JdbcTemplateRepository;
import oleksii.queriestask.repository.QueryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import oleksii.queriestask.datamodel.Query;

import java.util.*;

@Service
public class SQLQueryService implements QueryService { //add remove query

    private final Collection<Query> queriesToExecute; //replace with its own database/redis ?

    private final JdbcTemplateRepository jdbcTemplateRepository;

    private final QueryRepository queryRepository;

    private final Map<String, Object[][]> queriesExecuted;


    @Autowired
    public SQLQueryService(JdbcTemplateRepository jdbcTemplateRepository, QueryRepository queryRepository) {
        this.queryRepository = queryRepository;
        this.queriesToExecute = new HashSet<>();
        this.jdbcTemplateRepository = jdbcTemplateRepository;
        this.queriesExecuted = new HashMap<>();
    }

    @Override
    public Long addQuery(Query query) {

        queryRepository.save(query);

        return query.getId();
    }

    @Override
    public Collection<Query> getQueries() {
        return queriesToExecute;
    }

    @Override
    public Object[][] getQueryResults(long id) { //rework to be better?
        String queryToExecute=queriesToExecute.stream()
                .filter(q -> q.getId() == id)
                .findFirst().map(Query::getQuery)
                .orElseThrow(() -> new NullPointerException("Query with id: " + id + " not found"));

        if(queriesExecuted.containsKey(queryToExecute)) {return queriesExecuted.get(queryToExecute);}

        List<Map<String,Object>>queryResult;

        try{
            queryResult=jdbcTemplateRepository.getQueryResultList(queryToExecute);
        }catch (Exception e){
            throw new IllegalStateException("Failed to execute query", e);
        }

        Object[][] result=new Object[queryResult.size()][];

        int i = 0;
        for (Map<String, Object> row : queryResult) {
            result[i++]=row.values().toArray(); //does not preserve the order of columns
        }

        queriesExecuted.put(queryToExecute, result);

        return result;
    }

}
