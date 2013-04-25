package cgrb.eta.server.services;
import com.unboundid.ldap.sdk.BindRequest;
import com.unboundid.ldap.sdk.BindResult;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;
import com.unboundid.ldap.sdk.SimpleBindRequest;
import com.unboundid.ldap.sdk.extensions.PasswordModifyExtendedRequest;
import com.unboundid.ldap.sdk.extensions.PasswordModifyExtendedResult;

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
		LDAPConnector con = new LDAPConnector();
		LDAPConnection connection = con.getConn();

		PasswordModifyExtendedRequest passwordModifyRequest = new PasswordModifyExtendedRequest("uid=" + user + "," + con.getAuthdn(), oldPassword, newPassword);
		PasswordModifyExtendedResult passwordModifyResult;
		try {
			passwordModifyResult = (PasswordModifyExtendedResult) connection.processExtendedOperation(passwordModifyRequest);
		if (passwordModifyResult.getResultCode() == ResultCode.SUCCESS) {
			System.out.println("The password change was successful.");
			return "Success changing the password!!";
		} else {
			System.err.println("An error occurred while attempting to process " + "the password modify extended request.");
			System.err.println(passwordModifyResult.getResultCode());
			return "An error has occurred: " + passwordModifyResult.getResultCode();
		}
		} catch (LDAPException e) {
			e.printStackTrace();
			return "Something went wrong trying to read the response.";
		}

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
