package oleksii.queriestask.controller;

import oleksii.queriestask.datamodel.Query;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface QueryController {

     Map<String, Object> add(Query query);

     void delete(Long id);

     Collection<Query> findAll();

    List<Map<String, Object>> executeById(Long id);

}
