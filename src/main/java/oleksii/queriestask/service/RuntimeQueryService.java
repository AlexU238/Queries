package oleksii.queriestask.service;

import oleksii.queriestask.repository.JdbcTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
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
    public void addQuery(String query) {
        queriesToExecute.add(Query.builder().id(idCounter++).query(query).build());
    }

    @Override
    public Collection<Query> getQueries() {
        return queriesToExecute;
    }

    @Override
    public Object[][] getQueryResults(long id) {

        String queryToExecute=queriesToExecute.stream()
                .filter(q -> q.getId() == id)
                .findFirst()
                .get()
                .getQuery();

        queriesToExecute.removeIf(q -> q.getId() == id);

        if(queriesExecuted.containsKey(queryToExecute)) {return queriesExecuted.get(queryToExecute);}

        List<Map<String,Object>>queryResult;

        try{
            queryResult=jdbcTemplateRepository.getQueryResultList(queryToExecute);
        }catch (Exception e){
            //add logs
            return new Object[0][];
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
