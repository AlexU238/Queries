package oleksii.queriestask.service;

import oleksii.queriestask.datamodel.Query;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface QueryService {

    Long addQuery(Query query);

    Optional<Query> getQueryById(Long id);

    Collection<Query> getQueries();

    List<Map<String, Object>> getQueryResults(long id);

}
