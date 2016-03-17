/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.ldap;

/**
 * Seed exception for LDAP errors.
 */
public class LdapException extends Exception {

    private static final long serialVersionUID = -1042979744132023211L;

    /**
     * Constructor
     */
    public LdapException() {
        super();
    }

    /**
     * Constructor
     *
     * @param message message
     * @param cause   cause
     */
    public LdapException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor
     *
     * @param message message
     */
    public LdapException(String message) {
        super(message);
    }

    /**
     * Constructor
     *
     * @param cause cause
     */
    public LdapException(Throwable cause) {
        super(cause);
    }

}
