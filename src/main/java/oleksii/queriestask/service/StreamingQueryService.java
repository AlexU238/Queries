package oleksii.queriestask.service;

import java.io.IOException;
import java.io.OutputStream;

public interface StreamingQueryService extends QueryService{

void streamQueryResults(Long id, OutputStream outputStream) throws IOException;

}
