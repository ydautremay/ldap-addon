/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.ldap.internal;

import com.unboundid.ldap.sdk.ResultCode;
import org.seedstack.ldap.LdapException;
import org.seedstack.ldap.LdapService;
import org.seedstack.ldap.LdapUserContext;
import org.seedstack.seed.security.AuthenticationInfo;
import org.seedstack.seed.security.AuthenticationToken;
import org.seedstack.seed.security.Realm;
import org.seedstack.seed.security.RoleMapping;
import org.seedstack.seed.security.RolePermissionResolver;
import org.seedstack.seed.security.UsernamePasswordToken;
import org.seedstack.seed.security.AuthenticationException;
import org.seedstack.seed.security.IncorrectCredentialsException;
import org.seedstack.seed.security.UnsupportedTokenException;
import org.seedstack.seed.security.principals.PrincipalProvider;
import org.seedstack.seed.security.principals.Principals;
import org.seedstack.seed.security.principals.SimplePrincipalProvider;
import org.seedstack.ldap.LdapUserContextPrincipalProvider;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Set;

class LdapRealm implements Realm {

    private RoleMapping roleMapping;

    private RolePermissionResolver rolePermissionResolver;

    @Inject
    private LdapService ldapService;

    @Override
    public Set<String> getRealmRoles(PrincipalProvider<?> identityPrincipal, Collection<PrincipalProvider<?>> otherPrincipals) {
        SimplePrincipalProvider dnPrincipalProvider = Principals.getSimplePrincipalByName(otherPrincipals, "dn");
        try {
            LdapUserContext userContext;
            if (dnPrincipalProvider != null) {
                userContext = ldapService.createUserContext(dnPrincipalProvider.getValue());
            } else {
                String identity = identityPrincipal.getPrincipal().toString();
                userContext = ldapService.findUser(identity);
            }
            return ldapService.retrieveUserGroups(userContext);
        } catch (LdapException e) {
            throw new AuthenticationException(e.getCause().getMessage());
        }
    }

    @Override
    public AuthenticationInfo getAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        if (!(token instanceof UsernamePasswordToken)) {
            throw new UnsupportedTokenException("LdapRealm only supports UsernamePasswordToken");
        }
        UsernamePasswordToken userNamePasswordToken = (UsernamePasswordToken) token;
        try {
            LdapUserContext userContext = ldapService.findUser(userNamePasswordToken.getUsername());
            ldapService.authenticate(userContext, new String(userNamePasswordToken.getPassword()));

            AuthenticationInfo authcInfo = new AuthenticationInfo(userNamePasswordToken.getUsername(), userNamePasswordToken.getPassword());
            authcInfo.getOtherPrincipals().add(new SimplePrincipalProvider("dn", userContext.getDn()));
            authcInfo.getOtherPrincipals().add(Principals.fullNamePrincipal(ldapService.getAttributeValue(userContext, "cn")));
            authcInfo.getOtherPrincipals().add(new LdapUserContextPrincipalProvider(userContext));
            return authcInfo;
        } catch (LdapException ex) {
            com.unboundid.ldap.sdk.LDAPException e = (com.unboundid.ldap.sdk.LDAPException) ex.getCause();
            switch (e.getResultCode().intValue()) {
                case ResultCode.INVALID_CREDENTIALS_INT_VALUE:
                    throw new IncorrectCredentialsException(e.getMessage());
                default:
                    throw new AuthenticationException(e.getMessage());
            }
        }
    }

    @Override
    public RoleMapping getRoleMapping() {
        return this.roleMapping;
    }

    @Override
    public RolePermissionResolver getRolePermissionResolver() {
        return this.rolePermissionResolver;
    }

    /**
     * Setter roleMapping
     *
     * @param roleMapping the role mapping
     */
    @Inject
    public void setRoleMapping(@Named("LdapRealm-role-mapping") RoleMapping roleMapping) {
        this.roleMapping = roleMapping;
    }

    /**
     * Setter rolePermissionResolver
     *
     * @param rolePermissionResolver the rolePermissionResolver
     */
    @Inject
    public void setRolePermissionResolver(@Named("LdapRealm-role-permission-resolver") RolePermissionResolver rolePermissionResolver) {
        this.rolePermissionResolver = rolePermissionResolver;
    }

    @Override
    public Class<? extends AuthenticationToken> supportedToken() {
        return UsernamePasswordToken.class;
    }

}
