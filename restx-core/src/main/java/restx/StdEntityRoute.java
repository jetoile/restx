package restx;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import restx.jackson.Views;

import java.io.IOException;
import java.io.PrintWriter;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: xavierhanin
 * Date: 1/19/13
 * Time: 8:10 AM
 */
public abstract class StdEntityRoute extends StdRoute {
    protected final ObjectMapper mapper;
    protected final RestxLogLevel logLevel;

    /**
     * @deprecated Kept for backward compatibility with version <- 0.2.8
     */
    public StdEntityRoute(String name, ObjectMapper mapper, RestxRouteMatcher matcher) {
        this(name, mapper, matcher, HttpStatus.OK, RestxLogLevel.DEFAULT);
    }

    public StdEntityRoute(String name, ObjectMapper mapper, RestxRouteMatcher matcher, HttpStatus successStatus, RestxLogLevel logLevel) {
        super(name, matcher, successStatus);
        this.mapper = checkNotNull(mapper);
        this.logLevel = checkNotNull(logLevel);
    }

    @Override
    public void handle(RestxRouteMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
        resp.setLogLevel(logLevel);
        ctx.getLifecycleListener().onRouteMatch(this);
        Optional<?> result = doRoute(req, match);
        if (result.isPresent()) {
            resp.setStatus(getSuccessStatus().getCode());
            resp.setContentType("application/json");
            Object value = result.get();
            if (value instanceof Iterable) {
                value = Lists.newArrayList((Iterable) value);
            }
            ctx.getLifecycleListener().onBeforeWriteContent(this);
            writeValue(mapper, resp.getWriter(), value);
        } else {
            notFound(match,resp);
        }
    }

    protected void writeValue(ObjectMapper mapper, PrintWriter writer, Object value) throws IOException {
        getObjectWriter(mapper).writeValue(writer, value);
    }

    protected ObjectWriter getObjectWriter(ObjectMapper mapper) {
        return mapper.writerWithView(Views.Transient.class);
    }

    protected abstract Optional<?> doRoute(RestxRequest restxRequest, RestxRouteMatch match) throws IOException;
}
