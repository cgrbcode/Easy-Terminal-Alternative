package cgrb.eta.server.services;

import java.security.GeneralSecurityException;

import cgrb.eta.server.settings.Setting;
import cgrb.eta.server.settings.Settings;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustStoreTrustManager;

/**
 * LDAPConnector maintains and connects to our ldap server according to the parameters in the settings.
 * 
 * LDAPConnector reads and contributes to our settings file, and then attempts to connect to the ldap server. Disconnect should be called
 * on cleanup. All of the getters and setters that refer to connection configuration will be written to our settings file (upon set).
 * 
 * @field	conn	This is the field that will actually hold the connection. It will be used by any method attempting to query the server.
 * @field	basedn	The base Distinguished Name of the server as according to the settings.
 * @field authdn	The context of the database which contains the directory access information.
 * @field hostname	The fully qualified hostname of the LDAP server.
 * @field port	The port at which the server will be accessed.
 * @field connected	A boolean that represents if our system is connected to the LDAP server.
 * 
 * 
 * @author hillst
 *
 */
public class LDAPConnector {
	private LDAPConnection conn;
	private String basedn;
	private String authdn;
	private Settings settings = Settings.getInstance();
	private String hostname;
	private int port;
	private TrustStoreTrustManager keystore;
	private boolean connected;

	/**
	 * Our default and only constructor loads all relevant configuration settings and automatically tries to connect to the server.
	 */
	public LDAPConnector() {
		String keystorestr = settings.getSetting("keystorepass").getStringValue();
		char[] keystorepw = keystorestr.toCharArray();
		String keystorelocation = settings.getSetting("keystoreloc").getStringValue();
		String keystoreformat = settings.getSetting("keystoreformat").getStringValue();
		hostname = settings.getSetting("ldaphost").getStringValue();
		port = settings.getSetting("ldapport").getIntValue();
		basedn = settings.getSetting("basedn").getStringValue();
		authdn = settings.getSetting("authdn").getStringValue();
		//System.out.println("basedn: " + basedn);
		//System.out.println("authdn: " + authdn);
		keystore = new TrustStoreTrustManager(keystorelocation, keystorepw, keystoreformat, true);
		
		try {
			this.connect();
			this.connected = true;
		} catch (LDAPException e) {
			//If this get's caught it is VERY important that it is mentioned in the logs.
			e.printStackTrace();
		} catch (GeneralSecurityException e) {
			// thrown when there is a bad certificate usually, we probably don't want to keep trying if this is the case
			e.printStackTrace();
		}
	}
	/**
	 * Connect() will try to connect us to the database server. Useful if we attempt to connect from other processes, or if we need to disconnect/reconnect for some reason.
	 * 
	 * @return	If the connect was successfully made or not.
	 * @throws LDAPException	Throws this exception typically if we are unable to access the host (either port or hostname problem).
	 * @throws GeneralSecurityException	This is thrown if there is an invalid certificate. DO NOT ignore this exception.
	 */
	public boolean connect() throws LDAPException, GeneralSecurityException {
		// throws error lol
		this.disconnect();
		try {
			SSLUtil ssl = new SSLUtil(this.keystore);
			this.conn = new LDAPConnection(ssl.createSSLSocketFactory());
			this.conn.connect(this.hostname, this.port);
			this.connected = true;
			return true;
		} catch (LDAPException e) {
			// TODO insure this goes to catalina.out
			throw e;
		} catch (GeneralSecurityException e) {
			// TODO insure this goes to catalina.out
			throw e;
		}
	}

	public boolean disconnect() {
		if (this.conn == null){
			return true;
		}
		conn.close();
		this.connected = false;
		return true;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
		settings.putSetting("hostname", new Setting(hostname));
	}

	public int getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = new Integer(port);
		settings.putSetting("port", new Setting(port));
	}

	public LDAPConnection getConn() {
		return conn;
	}

	public String getBasedn() {
		return basedn;
	}

	// TODO ensure that his saves
	public void setBasedn(String basedn) {
		this.basedn = basedn;
		settings.putSetting("basedn", new Setting(basedn));
	}

	public String getAuthdn() {
		return authdn;
	}

	public void setAuthdn(String authdn) {
		this.authdn = authdn;
		settings.putSetting("authdn", new Setting(authdn));
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}
	
}
