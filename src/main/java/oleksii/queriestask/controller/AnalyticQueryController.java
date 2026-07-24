package oleksii.queriestask.controller;

import oleksii.queriestask.datamodel.Query;
import oleksii.queriestask.service.QueryService;
import oleksii.queriestask.service.StreamingQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/queries")
public class AnalyticQueryController implements QueryController {

    private final StreamingQueryService queryService;

    @Autowired
    public AnalyticQueryController(@Qualifier("analyticQueryService") StreamingQueryService queryService) {
        this.queryService = queryService;
    }

    @PostMapping
    @Override
    public Map<String, Object> add(@RequestBody Query query) {
        return Map.of("id",queryService.addQuery(query));
    }

    @GetMapping
    @Override
    public Collection<Query> findAll() {
        return queryService.getQueries();
    }

    @GetMapping("/{id}/results")
    @Override
    public List<Map<String, Object>> executeById(@PathVariable("id") Long query) {
        try{
            return queryService.getQueryResults(query);
        }catch (NullPointerException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }catch (IllegalStateException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/{id}/results/stream", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StreamingResponseBody> streamQueryResults(
            @PathVariable long id) {

        if(queryService.getQueryById(id).isEmpty()) return ResponseEntity.notFound().build();

        StreamingResponseBody stream = outputStream -> {
            queryService.streamQueryResults(id, outputStream);
        };

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(stream);
    }
}
