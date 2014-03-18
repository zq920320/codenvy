package com.codenvy.migration.ldapImport.factory;

import com.codenvy.migration.ldapImport.LdapAttribute;
import com.codenvy.organization.model.AbstractOrganizationUnit;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class converts item that is object in ldap dump format to instance of some class
 *
 * @author Sergiy Leschenko
 */
public abstract class ObjectFactory<T extends AbstractOrganizationUnit> {
    private static final Logger LOG = LoggerFactory.getLogger(ObjectFactory.class);

    /** Set of names attribute that will be ignored */
    private static final Set<String> IGNORED_ATTRIBUTES = new HashSet<>(Arrays.asList(
            "entryUUID",
            "creatorsName",
            "createTimestamp",
            "entryCSN",
            "modifiersName",
            "modifyTimestamp",
            "dn",
            "structuralObjectClass",
            "objectClass")
    );

    /** Format of decoded attribute */
    private static final Pattern DECODED_PARAM = Pattern.compile("^([a-z]+): (.*)", Pattern.CASE_INSENSITIVE);

    /** Format of encoded attribute */
    private static final Pattern ENCODED_PARAM = Pattern.compile("^([a-z]+):: (.*)", Pattern.CASE_INSENSITIVE);

    /**
     * @param name
     *         of ldap attribute
     * @return true if factory can use this attribute
     */
    protected abstract boolean isSuitableAttribute(String name);

    /**
     * @param objectClass
     *         of ldap item
     * @return true if factory can parse this LDAP class
     */
    public abstract boolean isSuitableClass(String objectClass);

    public abstract T create(List<String> item) throws Exception;

    /**
     * Parses LDAP item to List Attribute
     *
     * @param item
     *         String representation of item
     * @return list of parsed attributes
     * @throws Exception
     *         when some line has invalid format
     */
    protected List<LdapAttribute> getAttributes(List<String> item) throws Exception {
        List<LdapAttribute> attributes = new ArrayList<>();

        String key;
        StringBuilder value;
        for (int i = 0; i < item.size(); ++i) {
            String line = item.get(i);

            Matcher matcher = DECODED_PARAM.matcher(line);
            boolean isEncodedParameter = false;

            if (!matcher.matches()) {
                matcher = ENCODED_PARAM.matcher(line);
                if (!matcher.matches()) {
                    throw new Exception("Line has invalid format " + line);
                }
                isEncodedParameter = true;
            }

            key = matcher.group(1);
            value = new StringBuilder(matcher.group(2));

            /* If parameter value continues on the next line then this line starts with space symbol */
            while (i + 1 < item.size() && (line = item.get(i + 1)).charAt(0) == ' ') {
                value.append(line.substring(1));
                ++i;
            }

            if (!IGNORED_ATTRIBUTES.contains(key)) {
                if (isSuitableAttribute(key)) {
                    if (isEncodedParameter) {
                        Base64 decoder = new Base64();
                        Object encoded = decoder.decode(value.toString());
                        value = new StringBuilder(new String((byte[])encoded));
                    }
                    attributes.add(new LdapAttribute(key, value.toString()));
                } else {
                    LOG.warn("Parameter is unexpected " + key + ": " + value);
                }
            }
        }

        return attributes;
    }
}
