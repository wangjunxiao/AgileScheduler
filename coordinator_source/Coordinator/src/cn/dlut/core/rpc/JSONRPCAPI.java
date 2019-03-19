package cn.dlut.core.rpc;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.dlut.core.rpc.service.MonitoringService;
import cn.dlut.core.rpc.service.ControllingService;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.handler.AbstractHandler;

/**
 * This class manages JSON RPC API services. It creates the monitoring, tenant
 * and admin services. It implements the main handler for incoming requests and
 * redirects them to the appropriate service.
 *
 */
public class JSONRPCAPI extends AbstractHandler {

    private final MonitoringService monitoringService;
    private final ControllingService controllingService;

    /**
     * Constructor for JSON RPC handler. Creates control, monitoring and admin
     * services.
     */
    public JSONRPCAPI() {
        this.controllingService = new ControllingService();
        this.monitoringService = new MonitoringService();
    }

    @Override
    public void handle(final String target, final Request baseRequest,
            final HttpServletRequest request, final HttpServletResponse response)
            throws IOException, ServletException {

        /*if (baseRequest.getAuthentication() == null
                || baseRequest.getAuthentication().equals(
                        Authentication.UNAUTHENTICATED)) {
            response.sendError(Response.SC_UNAUTHORIZED, "Permission denied.");
            baseRequest.setHandled(true);

            return;
        }*/
        if (target.equals("/status")) {
            this.monitoringService.handle(request, response);
        } else if (target.equals("/admin")) {
            this.controllingService.handle(request, response);
        } else {
            response.sendError(Response.SC_NOT_FOUND, target
                    + " is not a service offered by Coordinator");
        }
        baseRequest.setHandled(true);
    }

}
