package fi.csc.data;

import javax.naming.directory.Attribute;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.Hashtable;

public class LDAP {
    final static Hashtable<String, String> environment = new Hashtable<String, String>();

    public LDAP(String ldapkey) {
        environment.put(Context.SECURITY_CREDENTIALS,ldapkey);
    }


        String emailquery(String tunnus) {
            try {
                DirContext dcContext = new InitialDirContext(environment);
                String filter = "(&(objectClass=inetOrgPerson)(cn="+tunnus+"))";
                String[] attrIDs = {"mail"};
                SearchControls searchControls = new SearchControls();
                searchControls.setReturningAttributes(attrIDs);
                searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
                NamingEnumeration<SearchResult> searchResults = dcContext
                        .search("ou=academic,ou=external,ou=users,ou=idm,dc=csc,dc=fi", filter, searchControls);
                SearchResult sr = searchResults.next();
                NamingEnumeration<Attribute> nea = (NamingEnumeration<Attribute>) sr.getAttributes().getAll();
                Attribute a = nea.next();
                return (String) a.get();
            } catch (NamingException e) {
                throw new RuntimeException(e);
            }
        }

    static {
        environment.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
        environment.put(Context.PROVIDER_URL,"ldaps://ldapcet3a.csc.fi");
        environment.put(Context.SECURITY_AUTHENTICATION,"simple");
        environment.put(Context.SECURITY_PRINCIPAL,"uid=data-copier-api,ou=Custom,ou=Special Users,dc=csc,dc=fi");
        }
}
