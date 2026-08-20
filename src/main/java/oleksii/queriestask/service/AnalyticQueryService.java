package oleksii.queriestask.service;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import oleksii.queriestask.repository.ClickHouseRepository;
import oleksii.queriestask.repository.QueryRepository;
import oleksii.queriestask.util.factory.RowProcessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import oleksii.queriestask.datamodel.Query;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

@Service("analyticQueryService")
public class AnalyticQueryService implements StreamingQueryService { //add remove query

    private final ClickHouseRepository clickHouseRepository;

    private final QueryRepository queryRepository;

    private final ObjectMapper objectMapper;

    private final RowProcessorFactory factory;

    @Autowired
    public AnalyticQueryService(ClickHouseRepository clickHouseRepository,
                                QueryRepository queryRepository,
                                ObjectMapper objectMapper,
                                RowProcessorFactory factory) {
        this.queryRepository = queryRepository;
        this.clickHouseRepository = clickHouseRepository;
        this.objectMapper = objectMapper;
        this.factory = factory;
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

        try{
           result= clickHouseRepository.getQueryResultList(toExecute.orElseThrow(NoSuchElementException::new).getQuery());
        }catch (DataAccessException e){
            throw new IllegalStateException();
        }

        return result;
    }

    @Override
    public Optional<Query> getQueryById(Long id) {
        return queryRepository.findById(id);
    }

    @Override
    public void streamQueryResults(Long id, OutputStream outputStream) throws IOException {
        Query query = queryRepository.findById(id)
                .orElseThrow(NoSuchElementException::new);

        try (JsonGenerator jsonGenerator = objectMapper.getFactory().createGenerator(outputStream)) {
            jsonGenerator.writeStartArray();
            JdbcTemplate template = clickHouseRepository.getJdbcTemplate();

            template.query(query.getQuery(), factory.create(jsonGenerator));

            jsonGenerator.writeEndArray();
            jsonGenerator.flush();
        }catch (DataAccessException e){
            throw new IllegalStateException();
        } catch (Exception e) {
            throw new RuntimeException("Streaming failed", e);
        }
    }
}
