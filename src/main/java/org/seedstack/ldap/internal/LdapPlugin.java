/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.ldap.internal;

import com.google.common.collect.Lists;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.core.AbstractPlugin;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.spi.configuration.ConfigurationProvider;
import org.seedstack.seed.core.utils.ConfigurationUtils;
import org.seedstack.seed.security.internal.SecurityPlugin;

import java.util.Collection;

public class LdapPlugin extends AbstractPlugin {

    public static final String LDAP_CONFIG_PREFIX = "org.seedstack.ldap";

    private static final String CHOSEN_REALMS = ConfigurationUtils.buildKey(SecurityPlugin.SECURITY_PREFIX, "realms");
    private static final String SERVER_HOST_PROP = "server-host";
    private static final String SERVER_PORT_PROP = "server-port";
    private static final String NUM_CONNECTIONS_PROP = "num-connections";
    private static final String ACCOUNT_DN_PROP = "account-dn";
    private static final String ACCOUNT_PASSWORD_PROP = "account-password";
    private static final int DEFAULT_NUM_CONNECTIONS = 10;
    private static final int DEFAULT_SERVER_PORT = 389;

    private LDAPConnectionPool ldapConnectionPool;
    private boolean startPlugin;

    @Override
    public String name() {
        return "ldap";
    }

    @Override
    public InitState init(InitContext initContext) {
        Configuration configuration = initContext.dependency(ConfigurationProvider.class).getConfiguration();
        String[] realms = configuration.getStringArray(CHOSEN_REALMS);
        startPlugin = ArrayUtils.contains(realms, LdapRealm.class.getSimpleName());

        if (startPlugin) {
            Configuration ldapConfiguration = configuration.subset(LDAP_CONFIG_PREFIX);
            // Initialize ldap pool connection
            String host = ldapConfiguration.getString(SERVER_HOST_PROP);
            if (host == null) {
                throw SeedException.createNew(LdapErrorCodes.NO_HOST_DEFINED).put("hostPropName", LDAP_CONFIG_PREFIX + "." + SERVER_HOST_PROP);
            }
            int port = ldapConfiguration.getInt(SERVER_PORT_PROP, DEFAULT_SERVER_PORT);
            int numConnections = ldapConfiguration.getInt(NUM_CONNECTIONS_PROP, DEFAULT_NUM_CONNECTIONS);
            String accountDn = StringUtils.join(ldapConfiguration.getStringArray(ACCOUNT_DN_PROP), ',');
            LDAPConnection connection;
            try {
                connection = new LDAPConnection(host, port, accountDn, ldapConfiguration.getString(ACCOUNT_PASSWORD_PROP));
                ldapConnectionPool = new LDAPConnectionPool(connection, numConnections);
            } catch (LDAPException e) {
                switch (e.getResultCode().intValue()) {
                    case ResultCode.NO_SUCH_OBJECT_INT_VALUE:
                        throw SeedException.wrap(e, LdapErrorCodes.NO_SUCH_ACCOUNT).put("account", accountDn)
                                .put("propName", LDAP_CONFIG_PREFIX + "." + ACCOUNT_DN_PROP);
                    case ResultCode.INVALID_CREDENTIALS_INT_VALUE:
                        throw SeedException.wrap(e, LdapErrorCodes.INVALID_CREDENTIALS).put("account", accountDn)
                                .put("passwordPropName", LDAP_CONFIG_PREFIX + "." + ACCOUNT_PASSWORD_PROP)
                                .put("userPropName", LDAP_CONFIG_PREFIX + "." + ACCOUNT_DN_PROP);
                    case ResultCode.CONNECT_ERROR_INT_VALUE:
                        throw SeedException.wrap(e, LdapErrorCodes.CONNECT_ERROR).put("host", host).put("port", port)
                                .put("hostPropName", LDAP_CONFIG_PREFIX + "." + SERVER_HOST_PROP)
                                .put("portPropName", LDAP_CONFIG_PREFIX + "." + SERVER_PORT_PROP);
                    default:
                        throw SeedException.wrap(e, LdapErrorCodes.LDAP_ERROR).put("message", e.getMessage()).put("host", host).put("port", port)
                                .put("account", accountDn);
                }
            }
        }
        return InitState.INITIALIZED;
    }

    @Override
    public Collection<Class<?>> requiredPlugins() {
        return Lists.<Class<?>>newArrayList(ConfigurationProvider.class);
    }

    @Override
    public Object nativeUnitModule() {
        if (startPlugin) {
            return new LdapModule(ldapConnectionPool);
        }
        return null;
    }

    @Override
    public void stop() {
        if (ldapConnectionPool != null) {
            ldapConnectionPool.close();
        }
    }

}
