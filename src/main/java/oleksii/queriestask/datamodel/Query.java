package oleksii.queriestask.datamodel;

import org.springframework.stereotype.Component;

import java.nio.file.LinkOption;

@Component
public class Query {

    private long id;

    private String query;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
