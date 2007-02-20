/*******************************************************************************
 * Copyright (c) 2004, 2005
 * Thomas Hallgren, Kenneth Olwing, Mitch Sonies
 * Pontus Rydin, Nils Unden, Peer Torngren
 * The code, documentation and other materials contained herein have been
 * licensed under the Eclipse Public License - v 1.0 by the individual
 * copyright holders listed above, as Initial Contributors under such license.
 * The text of such license is available at www.eclipse.org.
 *******************************************************************************/

package org.eclipse.buckminster.p4.preferences;

import java.util.ArrayList;

import org.eclipse.buckminster.core.XMLConstants;
import org.eclipse.buckminster.sax.ISaxable;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * @author Thomas Hallgren
 */
public class Server extends NodeWrapper implements ISaxable
{
	public static final String BM_SERVER_NS = XMLConstants.BM_PREFIX + "P4Server-1.0";
	public static final String BM_SERVER_PREFIX = "p4s";
	public static final String BM_SERVER_RESOURCE = "/p4server-1.0.xsd";
	public static final String FILE_EXTENSION = ".p4srv";
	public static final String TAG = "server";
	public static final String ATTR_NAME = "name";
	public static final String ATTR_PASSWORD = "password";
	public static final String ATTR_USER = "user";
	public static final String ATTR_DEFAULT_CLIENT = "defaultClient";

	Server(Preferences preferences)
	{
		super(preferences);
	}

	public Client addClient(String name) throws BackingStoreException
	{
		Preferences prefs = this.getPreferences();
		boolean first = (prefs.childrenNames().length == 0);
		if(!first && prefs.nodeExists(name))
			throw new BackingStoreException("Client already exists");

		Client client = new Client(this, prefs.node(name));
		if(first)
			this.setDefaultClient(name);

		return client;
	}

	public Server createCopy(String newName) throws BackingStoreException
	{
		Server copy = P4Preferences.getInstance().addServer(newName);
		deepCopy(this.getPreferences(), copy.getPreferences());
		return copy;
	}

	public Client getClient(String name) throws BackingStoreException
	{
		Preferences prefs = this.getPreferences();
		return prefs.nodeExists(name) ? new Client(this, prefs.node(name)) : null;
	}

	public String getPassword()
	{
		return this.getPreferences().get(ATTR_PASSWORD, null);
	}

	public String getUser()
	{
		return this.getPreferences().get(ATTR_USER, null);
	}

	public String getDefaultClientName()
	{
		return this.getPreferences().get(ATTR_DEFAULT_CLIENT, null);
	}

	public String[] getClientNames() throws BackingStoreException
	{
		return this.getPreferences().childrenNames();
	}

	public Client[] getClients() throws BackingStoreException
	{
		Preferences prefs = this.getPreferences();
		ArrayList<Client> clients = new ArrayList<Client>();
		for (String child : prefs.childrenNames())
		{
			try
			{
				clients.add(new Client(this, prefs.node(child)));
			}
			catch (IllegalStateException e)
			{
				// Someone removed this node during iteration
				continue;
			}
		}
		return clients.toArray(new Client[clients.size()]);
	}

	public Client getDefaultClient() throws BackingStoreException
	{
		String defaultName = this.getDefaultClientName();
		if(defaultName != null)
		{
			Client defaultClient = this.getClient(defaultName);
			if(defaultClient != null)
				return defaultClient;
		}
		throw new BackingStoreException("No default client exists");
	}

	public String getDefaultTag()
	{
		return TAG;
	}

	public void setDefaultClient(String clientName)
	{
		this.putString(ATTR_DEFAULT_CLIENT, clientName);
	}

	public boolean isDefaultServer()
	{
		return this.getName().equals(P4Preferences.getInstance().getDefaultServerName());
	}

	@Override
	public void remove() throws BackingStoreException
	{
		if(this.isDefaultServer())
			P4Preferences.getInstance().setOtherDefaultServer(this.getName());
		super.remove();
	}

	public void setAsDefault()
	{
		P4Preferences.getInstance().setDefaultServer(this.getName());
	}

	public void setOtherDefaultClient(String clientName) throws BackingStoreException
	{
		Preferences prefs = this.getPreferences();
		for (String childName : prefs.childrenNames())
		{
			if(!childName.equals(clientName))
			{
				this.setDefaultClient(childName);
				break;
			}
		}
	}

	public void setPassword(String password)
	{
		this.putString(ATTR_PASSWORD, password);
	}

	public void setUser(String user)
	{
		this.putString(ATTR_USER, user);
	}

	@Override
	protected void addAttributes(AttributesImpl attrs) throws SAXException
	{
		addAttribute(attrs, ATTR_NAME, this.getName());
		addAttribute(attrs, ATTR_DEFAULT_CLIENT, this.getDefaultClientName());
		addAttribute(attrs, ATTR_USER, this.getUser());
		addAttribute(attrs, ATTR_PASSWORD, this.getPassword());
	}

	@Override
	protected void emitElements(ContentHandler receiver, String namespace, String prefix) throws SAXException
	{
		try
		{
			for(Client client : this.getClients())
				client.toSax(receiver, namespace, prefix, client.getDefaultTag());
		}
		catch(BackingStoreException e)
		{
			throw new SAXException(e);
		}
	}

	public void toSax(ContentHandler receiver) throws SAXException
	{
		receiver.startDocument();
		receiver.startPrefixMapping(BM_SERVER_PREFIX, BM_SERVER_NS);
		this.toSax(receiver, BM_SERVER_NS, BM_SERVER_PREFIX, this.getDefaultTag());
		receiver.endPrefixMapping(BM_SERVER_PREFIX);
		receiver.endDocument();
	}
}
