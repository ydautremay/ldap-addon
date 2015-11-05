/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.ldap.api;

import org.seedstack.seed.security.principals.PrincipalProvider;

/**
 * A PrincipalProvider for a LDAPUserContext
 */
public class LDAPUserContextPrincipalProvider implements PrincipalProvider<LDAPUserContext> {

    private LDAPUserContext userContext;

    /**
     * Constructor with the LDAPuserContext
     *
     * @param userContext the LDAPUserContext contained by this principal
     */
    public LDAPUserContextPrincipalProvider(LDAPUserContext userContext) {
        this.userContext = userContext;
    }

    /**
     * Returns the LDAPUserContext
     *
     * @return the LDAPuserContext
     */
    @Override
    public LDAPUserContext getPrincipal() {
        return userContext;
    }

}
