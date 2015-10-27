/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.ldap.internal;

import com.google.inject.AbstractModule;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import org.seedstack.seed.security.ldap.api.LDAPSupport;

class LdapSecurityModule extends AbstractModule {

    private LDAPConnectionPool ldapConnectionPool;

    LdapSecurityModule(LDAPConnectionPool ldapConnectionPool) {
        this.ldapConnectionPool = ldapConnectionPool;
    }

    @Override
    protected void configure() {
        bind(LDAPConnectionPool.class).toInstance(ldapConnectionPool);
        bind(LDAPSupport.class).to(DefaultLDAPSupport.class);
    }
}
