package cgrb.eta.server.services;

import org.apache.commons.lang.NotImplementedException;

import com.unboundid.ldap.sdk.BindRequest;
import com.unboundid.ldap.sdk.BindResult;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;
import com.unboundid.ldap.sdk.SimpleBindRequest;

public class LDAPAuthenticator extends AuthenticationService {

	@Override
	public boolean checkCredentials(String user, String password) {
		try {
			LDAPConnector con = new LDAPConnector();
			LDAPConnection connection = con.getConn();
			final BindRequest request = new SimpleBindRequest("uid=" + user + ","+con.getAuthdn(), password);
			BindResult result = connection.bind(request);
			if (result.getResultCode() == ResultCode.SUCCESS) {
				con.disconnect();
			}
			return true;
		} catch (LDAPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public String changePassword(String user, String oldPassword, String newPassword) {
		throw new NotImplementedException();

	}

	@Override
	public String getUserFullName(String username) {
		// name,Lab/homeroom,phonenumber,department
		try {
			LDAPConnector con = new LDAPConnector();
			LDAPConnection conn = con.getConn();
			SearchResult searchResults = conn.search(con.getBasedn(), SearchScope.SUB, "(uid=" + username + ")");

			String gecos = null;
			if (searchResults.getEntryCount() > 0) {
				SearchResultEntry entry = searchResults.getSearchEntries().get(0);
				gecos = entry.getAttributeValue("gecos");
			}
			System.out.println("Gecos: " + gecos);
			String[] parsed = gecos.split(",");
			return parsed[0];
		} catch (LDAPException e) {
			e.printStackTrace();
		}
		return "";

	}

}
