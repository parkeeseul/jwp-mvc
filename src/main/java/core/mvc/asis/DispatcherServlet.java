package core.mvc.asis;

import core.mvc.HandlerMapping;
import core.mvc.ModelAndView;
import core.mvc.tobe.AnnotationHandlerMapping;
import core.mvc.tobe.HandlerExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@WebServlet(name = "dispatcher", urlPatterns = "/", loadOnStartup = 1)
public class DispatcherServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String CONTROLLER_PATH = "next.controller";

    // TODO 추후 삭제
    private static final String DEFAULT_REDIRECT_PREFIX = "redirect:";

    private static final Logger logger = LoggerFactory.getLogger(DispatcherServlet.class);

    private static final List<HandlerMapping> handlerMappings = new ArrayList<>();

    private AnnotationHandlerMapping mapping;
    private LegacyHandlerMapping rm;

    @Override
    public void init() throws ServletException {
        mapping = new AnnotationHandlerMapping(CONTROLLER_PATH);
        mapping.initialize();

        // TODO 추후 삭제
        rm = new LegacyHandlerMapping();
        rm.initMapping();

        handlerMappings.add(mapping);
        handlerMappings.add(rm);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String requestUri = req.getRequestURI();
        logger.debug("Method : {}, Request URI : {}", req.getMethod(), requestUri);

        Object handler = getHandler(req);
        ModelAndView modelAndView;
        try {
            if (handler instanceof Controller) {
                modelAndView = ((Controller) handler).execute(req, resp);
            } else if (handler instanceof HandlerExecution) {
                modelAndView = ((HandlerExecution) handler).handle(req, resp);
            } else {
                throw new ServletException("Invalid handler");
            }
            modelAndView.render(req, resp);
        } catch (Throwable e) {
            logger.error("Exception : {}", e);
            throw new ServletException(e.getMessage());
        }
    }

    private Object getHandler(HttpServletRequest req) {
        for (HandlerMapping handlerMapping : handlerMappings) {
            Object handler = handlerMapping.getHandler(req);
            if (Objects.nonNull(handler)) {
                return handler;
            }
        }
        return null;
    }
}
