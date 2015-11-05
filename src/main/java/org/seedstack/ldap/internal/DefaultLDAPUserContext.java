/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.ldap.internal;

import org.seedstack.ldap.LdapUserContext;

import java.util.HashMap;
import java.util.Map;

class DefaultLdapUserContext implements LdapUserContext {

    private static final long serialVersionUID = -8670518172669970580L;

    private String dn;

    private Map<String, String> knownAttributes = new HashMap<String, String>();

    DefaultLdapUserContext(String dn) {
        this.dn = dn;
    }

    @Override
    public String getDn() {
        return dn;
    }

    Map<String, String> getKnownAttributes() {
        return knownAttributes;
    }

}
