package oleksii.queriestask.service;

import oleksii.queriestask.datamodel.Query;
import java.util.Collection;

public interface QueryService {

    Long addQuery(Query query);

    Collection<Query> getQueries();

    Object[][] getQueryResults(long id);

}
