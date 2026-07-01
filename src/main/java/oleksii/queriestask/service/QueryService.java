package oleksii.queriestask.service;

import oleksii.queriestask.datamodel.Query;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface QueryService {

    Long addQuery(Query query);

    Collection<Query> getQueries();

    List<Map<String, Object>> getQueryResults(long id);

}
