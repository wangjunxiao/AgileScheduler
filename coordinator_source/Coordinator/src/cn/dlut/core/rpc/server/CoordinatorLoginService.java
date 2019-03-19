package cn.dlut.core.rpc.server;

import javax.management.remote.JMXPrincipal;
import javax.security.auth.Subject;

import org.eclipse.jetty.security.DefaultIdentityService;
import org.eclipse.jetty.security.DefaultUserIdentity;
import org.eclipse.jetty.security.IdentityService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.server.UserIdentity.Scope;

/**
 * Implements a login service by checking username and password and mapping
 * users to their role/resource.
 */
public class CoordinatorLoginService implements LoginService {

    private final IdentityService identityService = new DefaultIdentityService();

    @Override
    public IdentityService getIdentityService() {
        return this.identityService;
    }

    @Override
    public String getName() {
        return JettyServer.REALM;
    }

    @Override
    public UserIdentity login(final String username, final Object credentials) {
        return new CoordinatorAuthenticatedUser(username, (String) credentials)
                .getUserIdentity();
    }

    @Override
    public void logout(final UserIdentity arg0) {
    }

    @Override
    public void setIdentityService(final IdentityService arg0) {
    }

    @Override
    public boolean validate(final UserIdentity arg0) {

        return false;
    }

    /**
     * An authenticated user has an identity and can be logged out.
     */
    public class CoordinatorAuthenticatedUser implements Authentication.User {

        private final String user;
        @SuppressWarnings("unused")
        private final String password;

        /**
         * Creates an authenticated user.
         *
         * @param username
         *            the user's name
         * @param password
         *            the user's password
         */
        public CoordinatorAuthenticatedUser(final String username,
                final String password) {
            this.user = username;
            this.password = password;
        }

        @Override
        public String getAuthMethod() {
            return "BASIC";
        }

        @Override
        public UserIdentity getUserIdentity() {
            // TODO: need to return the correct identity for this user.
            // Permitting specific logins for now with no passwords
        	
            if (this.user.equals("admin")) {
                return new DefaultUserIdentity(new Subject(), new JMXPrincipal(
                        this.user), new String[] {"admin", "status"});
            } else if (this.user.equals("status")) {
                return new DefaultUserIdentity(new Subject(), new JMXPrincipal(
                        this.user), new String[] {"status"});
            } else {
            	return new DefaultUserIdentity(new Subject(), new JMXPrincipal(
                        this.user), new String[] {"admin", "status"});
            	//return null;
            }
        }

        @Override
        public boolean isUserInRole(final Scope scope, final String role) {
            System.out.println("role " + role);
            return true;
        }

        @Override
        public void logout() {
            // TODO: remove any acquired tokens.
        }
    }

}
