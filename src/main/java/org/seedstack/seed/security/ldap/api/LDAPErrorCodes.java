/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.security.ldap.api;

import org.seedstack.seed.core.api.ErrorCode;

/**
 * Enum for Error codes in LDAP security support
 */
public enum LDAPErrorCodes implements ErrorCode {
    LDAP_ERROR, NO_SUCH_ACCOUNT, INVALID_CREDENTIALS, CONNECT_ERROR, NO_HOST_DEFINED;
}
