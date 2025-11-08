package oleksii.queriestask.service;

import oleksii.queriestask.repository.JdbcTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import oleksii.queriestask.datamodel.Query;

import java.util.*;

@Service
public class RuntimeQueryService implements QueryService {

    private final Collection<Query> queriesToExecute;

    private final JdbcTemplateRepository jdbcTemplateRepository;

    private final Map<String, Object[][]> queriesExecuted;

    private long idCounter;

    @Autowired
    public RuntimeQueryService(JdbcTemplateRepository jdbcTemplateRepository) {
        this.queriesToExecute = new ArrayList<>();
        this.jdbcTemplateRepository = jdbcTemplateRepository;
        this.queriesExecuted = new HashMap<>();
        idCounter = 0;
    }

    @Override
    public Long addQuery(String query) {
        if (query.startsWith("\"") && query.endsWith("\"") && query.length() > 1) {
            query = query.substring(1, query.length() - 1);
        }
        queriesToExecute.add(Query.builder().id(idCounter).query(query).build());
        return idCounter++;
    }

    @Override
    public Collection<Query> getQueries() {
        return queriesToExecute;
    }

    @Override
    public Object[][] getQueryResults(long id) {
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

    //testing only
    long getIdCounter() {
        return idCounter;
    }

    //testing only
    Collection<Query> getQueriesToExecute() {
        return queriesToExecute;
    }

    //testing only
    Map<String, Object[][]> getQueriesExecuted() {
        return queriesExecuted;
    }
}
