package oleksii.queriestask.controller;

import oleksii.queriestask.datamodel.Query;
import oleksii.queriestask.service.QueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.Map;

@RestController
public class RuntimeQueryController implements QueryController {

    private static final String QUERIES_PATH="/queries";
    QueryService queryService;

    @Autowired
    public RuntimeQueryController(@Qualifier("runtimeQueryService") QueryService queryService) {
        this.queryService = queryService;
    }

    @PostMapping(QUERIES_PATH)
    @Override
    public Map<String, Object> add(@RequestBody String query) {
        return Map.of("id",queryService.addQuery(query));
    }

    @GetMapping(QUERIES_PATH)
    @Override
    public Collection<Query> findAll() {
        return queryService.getQueries();
    }

    @GetMapping("/execute")
    @Override
    public Object[][] executeById(@RequestParam("query") Long query) {
        try{
            return queryService.getQueryResults(query);
        }catch (NullPointerException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }catch (IllegalStateException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }
}
