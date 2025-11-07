package oleksii.queriestask.controller;

import oleksii.queriestask.datamodel.Query;

import java.util.Collection;
import java.util.Map;

public interface QueryController {

    public Map<String, Object> add(String query);

    public Collection<Query> findAll();

    public Object[][] executeById(Long id);

}
