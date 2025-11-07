package oleksii.queriestask.controller;

import oleksii.queriestask.datamodel.Query;
import oleksii.queriestask.service.QueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Map;

@RestController
@RequestMapping("/queries")
public class RuntimeQueryController implements QueryController {

    QueryService queryService;

    @Autowired
    public RuntimeQueryController(@Qualifier("runtimeQueryService") QueryService queryService) {
        this.queryService = queryService;
    }

    @PostMapping("")
    @Override
    public Map<String, Object> add(@RequestBody String query) {
        return Map.of("id",queryService.addQuery(query));
    }

    @GetMapping("")
    @Override
    public Collection<Query> findAll() {
        return queryService.getQueries();
    }

    @GetMapping("/execute")
    @Override
    public Object[][] executeById(@RequestParam("id") Long id) {
        return queryService.getQueryResults(id);
    }
}
