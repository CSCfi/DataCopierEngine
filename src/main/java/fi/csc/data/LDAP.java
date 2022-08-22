package fi.csc.data;

import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.api.ldap.model.cursor.SearchCursor;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.Response;
import org.apache.directory.api.ldap.model.message.SearchRequest;
import org.apache.directory.api.ldap.model.message.SearchRequestImpl;
import org.apache.directory.api.ldap.model.message.SearchResultEntry;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.Hashtable;


public class LDAP {

    final static String ATRIBUUTTI = "mail";
    String ldapkey;

    public LDAP(String ldapkey) {
        this.ldapkey = ldapkey;
    }
    String emailquery(String tunnus) {
        try {
            LdapConnection connection = new LdapNetworkConnection( "ldapcet3a.csc.fi", 636, true );
            connection.bind( "uid=data-copier-api,ou=Custom,ou=Special Users,dc=csc,dc=fi", ldapkey);
            String filter = "(&(objectClass=inetOrgPerson)(cn="+tunnus+"))";
            SearchRequest req = new SearchRequestImpl();
            req.setScope( SearchScope.SUBTREE );
            req.addAttributes( ATRIBUUTTI );//"mail"
            req.setTimeLimit( 0 );
            req.setBase( new Dn( "ou=academic,ou=external,ou=users,ou=idm,dc=csc,dc=fi" ) );
            req.setFilter(filter);
            SearchCursor searchCursor = connection.search( req );
            while ( searchCursor.next() ) {
                Response response = searchCursor.get();

                // process the SearchResultEntry
                if (response instanceof SearchResultEntry) {
                    Entry resultEntry = ((SearchResultEntry) response).getEntry();
                    if (resultEntry.containsAttribute(ATRIBUUTTI))
                        return String.valueOf(resultEntry.get(ATRIBUUTTI));
                }
            }
        } catch (CursorException e) {
            throw new RuntimeException(e);
        } catch (LdapException e) {
            throw new RuntimeException(e);
        }
        return "LDAP-QUERY-FAILED";
    }


}
