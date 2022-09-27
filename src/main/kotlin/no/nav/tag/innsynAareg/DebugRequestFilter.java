package no.nav.tag.innsynAareg;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collections;

@Component
@Order(1)
public class DebugRequestFilter implements Filter {
    static Logger log = LoggerFactory.getLogger(DebugRequestFilter.class);

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        log.info(
                "Logging Request  {} : {} Authorization(s): {}",
                req.getMethod(),
                req.getRequestURI(),
                String.join("; ", Collections.list(req.getHeaders("Authorization")))
        );
        chain.doFilter(request, response);
    }
}
