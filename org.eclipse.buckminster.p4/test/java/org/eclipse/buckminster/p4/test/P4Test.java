/*******************************************************************************
 * Copyright (c) 2004, 2006
 * Thomas Hallgren, Kenneth Olwing, Mitch Sonies
 * Pontus Rydin, Nils Unden, Peer Torngren
 * The code, documentation and other materials contained herein have been
 * licensed under the Eclipse Public License - v 1.0 by the individual
 * copyright holders listed above, as Initial Contributors under such license.
 * The text of such license is available at www.eclipse.org.
 *******************************************************************************/

package org.eclipse.buckminster.p4.test;

import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.buckminster.core.RMContext;
import org.eclipse.buckminster.p4.internal.ClientSpec;
import org.eclipse.buckminster.p4.internal.Connection;
import org.eclipse.buckminster.p4.internal.ConnectionInfo;
import org.eclipse.buckminster.p4.internal.DepotFile;
import org.eclipse.buckminster.p4.internal.DepotFolder;
import org.eclipse.buckminster.p4.internal.FileSpec;
import org.eclipse.buckminster.p4.internal.Label;
import org.eclipse.buckminster.p4.preferences.Client;
import org.eclipse.buckminster.p4.preferences.P4Preferences;
import org.eclipse.buckminster.p4.preferences.Server;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;


public class P4Test extends TestCase
{
	Connection m_connection;
	Map<String, String> m_env;

	@Override
	protected void setUp() throws Exception
	{
		Map<String,String> scope = RMContext.getGlobalPropertyAdditions();
		P4Preferences prefs = P4Preferences.getInstance();
		Server server = prefs.getDefaultServer();
		if(server == null)
			server = prefs.configureDefaultServer(scope, false);

		Client client = server.getDefaultClient();
		m_connection = new Connection(scope, client, server.getName());
		super.setUp();
	}

	public void testInfo()
	throws Exception
	{
		ConnectionInfo info = m_connection.getConnectionInfo();
		System.out.println(info.toString());
	}
	
	public void testClientSpec()
	throws Exception
	{
		ClientSpec client = m_connection.getClientSpec();
		for(ClientSpec.ViewEntry entry : client.getView())
			System.out.println(entry.toString());
		
	}

	public void testDepots()
	throws Exception
	{
		DepotFolder[] depots  = m_connection.getDepots();
		for(DepotFolder depot : depots)
		{
			System.out.println(depot.toString());
			DepotFolder[] folders = depot.getFolders(false);
			for(DepotFolder folder : folders)
			{
				System.out.print("    ");
				System.out.println(folder.toString());
			}
		}
	}

	public void testFolders()
	throws Exception
	{
		DepotFolder[] folders  = m_connection.getFolders(new Path("//public").append("*"), FileSpec.HEAD);
		for(DepotFolder folder : folders)
			System.out.println(folder.toString());
	}

	public void testLabels()
	throws Exception
	{
		Label[] labels  = m_connection.getLabels(new Path("//public/jam").append("..."));
		for(Label label : labels)
			System.out.println(label.getLabel());
		
		Label label = m_connection.getLabel("jam2-2-4");
		assertNotNull(label);
		System.out.println(label.getLabel());
		assertNull(m_connection.getLabel("jam-fubar"));
	}

	public void testDepotFile()
	throws Exception
	{
		String client = m_connection.getClientSpec().getClient();
		IPath filePath = new Path("//" +  client + "/public/index.html");
		DepotFile file = m_connection.getFile(new FileSpec(filePath, FileSpec.HEAD));
		assertNotNull(file);
		System.out.println(file.getDepotPath());
		System.out.println(file.getClientPath());
	}

	public void testLastChange()
	throws Exception
	{
		IPath path = new Path("//public/perforce/webkeeper");
		long number = m_connection.getLastChangeNumber(path, null);
		assertTrue(number > 0);
	}
}

