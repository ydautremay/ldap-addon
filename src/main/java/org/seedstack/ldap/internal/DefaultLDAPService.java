/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.ldap.internal;

import com.unboundid.ldap.sdk.*;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authc.UnknownAccountException;
import org.seedstack.ldap.LdapService;
import org.seedstack.ldap.LdapUserContext;
import org.seedstack.ldap.LdapException;
import org.seedstack.seed.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.*;

class DefaultLdapService implements LdapService {

    private final static Logger LOGGER = LoggerFactory.getLogger(DefaultLdapService.class);

    @Configuration(LdapPlugin.LDAP_CONFIG_PREFIX + ".user-base")
    private String[] userBase;

    @Configuration(value = LdapPlugin.LDAP_CONFIG_PREFIX + ".user-class", mandatory = false)
    private String userObjectClass;

    @Configuration(value = LdapPlugin.LDAP_CONFIG_PREFIX + ".user-identity-attribute", defaultValue = "uid")
    private String userIdentityAttribute;

    @Configuration(value = LdapPlugin.LDAP_CONFIG_PREFIX + ".user-additional-attributes", mandatory = false)
    private String[] userAdditionalAttributes;

    @Configuration(LdapPlugin.LDAP_CONFIG_PREFIX + ".group-base")
    private String[] groupBase;

    @Configuration(value = LdapPlugin.LDAP_CONFIG_PREFIX + ".group-class", mandatory = false)
    private String groupObjectClass;

    @Configuration(value = LdapPlugin.LDAP_CONFIG_PREFIX + ".group-member-attribute", defaultValue = "member")
    private String groupMemberAttribute;

    @Inject
    private LDAPConnectionPool ldapConnectionPool;

    @Override
    public LdapUserContext createUserContext(String dn) {
        return internalCreateUser(dn);
    }

    private DefaultLdapUserContext internalCreateUser(String dn) {
        return new DefaultLdapUserContext(dn);
    }

    @Override
    public LdapUserContext findUser(String identityAttributeValue) throws LdapException {
        try {
            Filter userClassFilter;
            if (userObjectClass != null && !userObjectClass.isEmpty()) {
                userClassFilter = Filter.createEqualityFilter("objectClass", userObjectClass);
            } else {
                userClassFilter = Filter.createPresenceFilter("objectClass");
            }
            Filter filter = Filter.createANDFilter(userClassFilter, Filter.createEqualityFilter(userIdentityAttribute, identityAttributeValue));
            LOGGER.debug(filter.toString());
            String[] attributesToRetrieve;
            if (userAdditionalAttributes != null) {
                attributesToRetrieve = userAdditionalAttributes;
                if (!ArrayUtils.contains(attributesToRetrieve, "cn") || !ArrayUtils.contains(attributesToRetrieve, "CN")) {
                    ArrayUtils.add(attributesToRetrieve, "cn");
                }
            } else {
                attributesToRetrieve = new String[]{"cn"};
            }
            SearchResult searchResult = ldapConnectionPool.search(StringUtils.join(userBase, ','), SearchScope.SUB, filter, attributesToRetrieve);
            if (searchResult.getEntryCount() != 1) {
                throw new UnknownAccountException();
            }
            SearchResultEntry searchResultEntry = searchResult.getSearchEntries().get(0);
            String dn = searchResultEntry.getDN();
            DefaultLdapUserContext ldapUserContext = internalCreateUser(dn);
            ldapUserContext.getKnownAttributes().put("cn", searchResultEntry.getAttributeValue("cn"));
            return ldapUserContext;
        } catch (com.unboundid.ldap.sdk.LDAPException e) {
            throw new LdapException(e);
        }
    }

    @Override
    public void authenticate(LdapUserContext userContext, String password) throws LdapException {
        try {
            ldapConnectionPool.bindAndRevertAuthentication(userContext.getDn(), password);
        } catch (com.unboundid.ldap.sdk.LDAPException e) {
            throw new LdapException(e);
        }
    }

    @Override
    public String getAttributeValue(LdapUserContext userContext, String attribute) throws LdapException {
        if (((DefaultLdapUserContext) userContext).getKnownAttributes().get(attribute.toLowerCase()) != null) {
            return ((DefaultLdapUserContext) userContext).getKnownAttributes().get(attribute.toLowerCase());
        }
        return getAttributeValues(userContext, attribute).get(attribute);
    }

    @Override
    public Map<String, String> getAttributeValues(LdapUserContext userContext, String... attributes) throws LdapException {
        Map<String, String> result = new HashMap<String, String>();
        List<String> retainedAttr = new ArrayList<String>();
        Map<String, String> knownAttributes = ((DefaultLdapUserContext) userContext).getKnownAttributes();
        for (String attr : attributes) {
            if (knownAttributes.get(attr.toLowerCase()) == null) {
                retainedAttr.add(attr.toLowerCase());
            }
        }
        if (!retainedAttr.isEmpty()) {
            LOGGER.debug("Will connect to LDAP to retrieve attributes {}", retainedAttr);
            try {
                SearchResultEntry entry = ldapConnectionPool.getEntry(userContext.getDn(), retainedAttr.toArray(new String[retainedAttr.size()]));
                for (String attr : retainedAttr) {
                    knownAttributes.put(attr, entry.getAttributeValue(attr));
                }
            } catch (com.unboundid.ldap.sdk.LDAPException e) {
                throw new LdapException(e);
            }
        }
        for (String attr : attributes) {
            result.put(attr.toLowerCase(), knownAttributes.get(attr.toLowerCase()));
        }
        return result;
    }

    @Override
    public Set<String> retrieveUserGroups(LdapUserContext userContext) throws LdapException {
        Set<String> groups = new HashSet<String>();
        try {
            Filter groupClassFilter;
            if (groupObjectClass != null && !groupObjectClass.isEmpty()) {
                groupClassFilter = Filter.createEqualityFilter("objectClass", groupObjectClass);
            } else {
                groupClassFilter = Filter.createPresenceFilter("objectClass");
            }
            Filter filter = Filter.createANDFilter(groupClassFilter, Filter.createEqualityFilter(groupMemberAttribute, userContext.getDn()));
            LOGGER.debug(filter.toString());
            SearchResult searchResult = ldapConnectionPool.search(StringUtils.join(groupBase, ','), SearchScope.SUB, filter, "cn");
            for (SearchResultEntry entry : searchResult.getSearchEntries()) {
                groups.add(entry.getAttributeValue("cn"));
            }
            return groups;
        } catch (com.unboundid.ldap.sdk.LDAPException e) {
            throw new LdapException(e);
        }
    }
}
