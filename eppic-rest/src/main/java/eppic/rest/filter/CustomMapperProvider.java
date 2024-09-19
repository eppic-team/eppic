package eppic.rest.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import eppic.db.mongoutils.ConfigurableMapper;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * A custom object mapper provider that serves to configure the way objects are serialized
 * to json. For instance to produce snake case (but not only, any other behaviour is configurable with this).
 * <p>
 * Implemented from ideas here:
 * <p>
 * https://stackoverflow.com/questions/18872931/custom-objectmapper-with-jersey-2-2-and-jackson-2-1
 * <p>
 * https://stackoverflow.com/questions/36640127/jersey-json-switching-from-camel-case-to-underscores-snake-case
 *
 * @author Jose Duarte
 * @see ConfigurableMapper
 * @since 3.0
 */
@Provider
public class CustomMapperProvider implements ContextResolver<ObjectMapper> {

    private final ObjectMapper mapper;

    public CustomMapperProvider() {
        mapper = ConfigurableMapper.getMapper();
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }


}
