package oleksii.queriestask.controller;

import oleksii.queriestask.datamodel.Query;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface QueryController {

    public Map<String, Object> add(Query query);

    public Collection<Query> findAll();

    public List<Map<String, Object>> executeById(Long id);

}
