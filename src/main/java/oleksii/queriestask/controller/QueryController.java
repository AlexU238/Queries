package oleksii.queriestask.controller;

import oleksii.queriestask.datamodel.Query;

import java.util.Collection;

public interface QueryController {

    public Long add(String query);

    public Collection<Query> findAll();

    public Object[][] executeById(Long id);

}
