package cn.dlut.core.rpc.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

/**
 * Abstract class to handle JSON requests.
 */
public abstract class AbstractService {

    private static Logger log = LogManager.getLogger(AbstractService.class
            .getName());

    /**
     * Handles the service request and stores the result in the response.
     *
     * @param request the request
     * @param response the response
     */
    public abstract void handle(HttpServletRequest request,
            HttpServletResponse response);

    /**
     * Parses the JSON request.
     *
     * @param request
     *            the request
     * @return the JSON object
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws JSONRPC2ParseException if JSON request is invalid
     */
    protected JSONRPC2Request parseJSONRequest(final HttpServletRequest request)
            throws IOException, JSONRPC2ParseException {
    	String JSONRPCString = request.getParameter("JSONRPCString").toString();
        AbstractService.log.info("---------JSON RPC request: {}",
        		JSONRPCString);
        return JSONRPC2Request.parse(JSONRPCString);
    }

    /**
     * Writes json object.
     *
     * @param response
     *            the response
     * @param jresp the JSON response
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    protected void writeJSONObject(final HttpServletResponse response,
            final JSONRPC2Response jresp) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader("Content-Type", "application/json; charset=utf-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setContentType("application/json; charset=utf-8");
        final String json = jresp.toJSONString();
        AbstractService.log.debug("---------JSON RPC response: {}", json);

        response.getWriter().println(json);
       
    }

    /**
     * Gets the exception stack trace in a string.
     *
     * @param e the exception
     * @return a string
     */
    protected static String stack2string(final Exception e) {
        PrintWriter pw = null;
        try {
            final StringWriter sw = new StringWriter();
            pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            return "------\r\n" + sw.toString() + "------\r\n";
        } catch (final Exception e2) {
            return "bad stack2string";
        } finally {
            if (pw != null) {
                try {
                    pw.close();
                } catch (final Exception ex) {
                    return "cannot convert exception: " + ex.toString();
                }
            }
        }
    }
}
