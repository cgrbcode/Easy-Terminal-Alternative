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
		String check = this.isLegalPassword(newPassword);
		if (!check.equals("pass")){
            return check;
        }
        LDAPConnector con = new LDAPConnector();
		LDAPConnection connection = con.getConn();
		PasswordModifyExtendedRequest passwordModifyRequest = new PasswordModifyExtendedRequest("uid=" + user + "," + con.getAuthdn(), oldPassword, newPassword);
		PasswordModifyExtendedResult passwordModifyResult;
		try {		
			final BindRequest request = new SimpleBindRequest("uid=" + user + ","+con.getAuthdn(), oldPassword);
			BindResult result = connection.bind(request);
			if (result.getResultCode() != ResultCode.SUCCESS){
				return "An error has occurred: " + result.getResultCode();
			}
			passwordModifyResult = (PasswordModifyExtendedResult) connection.processExtendedOperation(passwordModifyRequest);
			if (passwordModifyResult.getResultCode() == ResultCode.SUCCESS) {
				System.out.println("The password change was successful.");
				con.disconnect();
				return "Your password has been changed successfully.";
			} else {
				System.err.println("An error occurred while attempting to process " + "the password modify extended request.");
				System.err.println(passwordModifyResult.getResultCode());
				return "An error has occurred: " + passwordModifyResult.getResultCode();
			}
		} catch (LDAPException e) {
			e.printStackTrace();
			return "Invalid old password. If the problem persist contact an administrator.";
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
    /**
     * Checks a password to see if it matches up with the enforced password policy.
     *
     * This method should me modified or overridden (probably moved to Authentication Service) if you are installing a custom module.
     *
     * @param password password to be checked
     * @return String Returns "pass" if it passed the policy, returns the problem otherwise.
     */
    private String isLegalPassword(String password){
        boolean contains_num = false, contains_lowercase = false , contains_uppercase = false, contains_special = false;
        if (password.length() < 6){
            return "The password must have at least 6 characters";
        }
        if (password.matches("^(?=.*[0-9]).{6,}$")){            
            contains_num = true;
        }
        if (password.matches("^(?=.*[a-z]).{6,}$")){
            contains_lowercase = true;
        }
        if (password.matches("^(?=.*[A-Z]).{6,}$")){
            contains_uppercase = true;
        }
        if (password.matches("^(?=.*[!@#$%^&+=]).{6,}$")){
            contains_special = true;
        }
        if( ! (contains_num || contains_special) ){
            if ( !(contains_uppercase && contains_lowercase)){
                return "The password must have both upper and lowecase letters, or non-letters.";
            }
        }
        return "pass";
    }

}
