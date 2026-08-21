package oleksii.queriestask.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public interface StreamResultsControllerAddOn {

    ResponseEntity<StreamingResponseBody> streamQueryResults(long id);

}
