package oleksii.queriestask.service;

import oleksii.queriestask.datamodel.Query;
import java.util.Collection;

public interface QueryService {

    void addQuery(String query);

    Collection<Query> getQueries();

    Object[][] getQueryResults(long id);

}
