/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.ldap;

import org.seedstack.seed.security.principals.PrincipalProvider;

/**
 * A PrincipalProvider for a LdapUserContext.
 */
public class LdapUserContextPrincipalProvider implements PrincipalProvider<LdapUserContext> {

    private LdapUserContext userContext;

    /**
     * Constructor with the LdapUserContext.
     *
     * @param userContext the LdapUserContext contained by this principal
     */
    public LdapUserContextPrincipalProvider(LdapUserContext userContext) {
        this.userContext = userContext;
    }

    /**
     * Returns the LdapUserContext.
     *
     * @return the LdapUserContext
     */
    @Override
    public LdapUserContext getPrincipal() {
        return userContext;
    }

}
