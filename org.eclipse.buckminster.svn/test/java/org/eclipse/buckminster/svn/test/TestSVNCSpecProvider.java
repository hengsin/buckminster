/*******************************************************************************
 * Copyright (c) 2004, 2006
 * Thomas Hallgren, Kenneth Olwing, Mitch Sonies
 * Pontus Rydin, Nils Unden, Peer Torngren
 * The code, documentation and other materials contained herein have been
 * licensed under the Eclipse Public License - v 1.0 by the individual
 * copyright holders listed above, as Initial Contributors under such license.
 * The text of such license is available at www.eclipse.org.
 *******************************************************************************/

package org.eclipse.buckminster.svn.test;

import java.net.URL;
import java.util.regex.Pattern;

import org.eclipse.buckminster.core.CorePlugin;
import org.eclipse.buckminster.core.KeyConstants;
import org.eclipse.buckminster.core.common.model.Format;
import org.eclipse.buckminster.core.cspec.model.ComponentRequest;
import org.eclipse.buckminster.core.metadata.model.BillOfMaterials;
import org.eclipse.buckminster.core.query.builder.AdvisorNodeBuilder;
import org.eclipse.buckminster.core.query.builder.ComponentQueryBuilder;
import org.eclipse.buckminster.core.query.model.AdvisorNode;
import org.eclipse.buckminster.core.query.model.ComponentQuery;
import org.eclipse.buckminster.core.reader.IComponentReader;
import org.eclipse.buckminster.core.reader.IReaderType;
import org.eclipse.buckminster.core.reader.ProjectDescReader;
import org.eclipse.buckminster.core.resolver.IResolver;
import org.eclipse.buckminster.core.resolver.MainResolver;
import org.eclipse.buckminster.core.resolver.ResolutionContext;
import org.eclipse.buckminster.core.rmap.model.Provider;
import org.eclipse.buckminster.core.test.AbstractTestCase;
import org.eclipse.buckminster.core.version.IVersionDesignator;
import org.eclipse.buckminster.core.version.VersionFactory;
import org.eclipse.buckminster.core.version.VersionMatch;
import org.eclipse.buckminster.core.version.VersionSelector;
import org.eclipse.buckminster.sax.Utils;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

public class TestSVNCSpecProvider extends AbstractTestCase
{
	@Override
	protected URL getRMAP()
	{
		return getClass().getResource("test.rmap");
	}

	@Override
	public void setUp() throws Exception
	{
		super.setUp();
	}

	public void testResolutionPriority() throws Exception
	{
		IVersionDesignator designator = VersionFactory.createDesignator(VersionFactory.OSGiType, "[0.1.0,0.2.0)") ;
		ComponentRequest request = new ComponentRequest("org.eclipse.buckminster.cmdline", KeyConstants.PLUGIN_CATEGORY, designator);

		ComponentQueryBuilder queryBld = new ComponentQueryBuilder();
		queryBld.setRootRequest(request);
		queryBld.setResourceMapURL(getRMAP());

		AdvisorNodeBuilder node = new AdvisorNodeBuilder();
		node.setNamePattern(Pattern.compile("buckminster"));
		node.setUseInstalled(false);

		// Look first at main, then at branch 3.2.x and finally at Europa_GA
		//
		node.setBranchTagPath(VersionSelector.fromPath("main,3.2.x,/Europa_GA"));
		node.setResolutionPrio(new int[] { AdvisorNode.PRIO_BRANCHTAG_PATH_INDEX, AdvisorNode.PRIO_VERSION_DESIGNATOR, AdvisorNode.PRIO_SPACE_PATH_INDEX });
		queryBld.addAdvisorNode(node);

		IResolver resolver = new MainResolver(new ResolutionContext(queryBld.createComponentQuery()));
		BillOfMaterials bom = resolver.resolve(new NullProgressMonitor());
		assertTrue("Resolve failed", bom.isFullyResolved());
		assertNull(bom.getResolution().getVersionMatch().getBranchOrTag());
		Utils.serialize(bom.getResolution(), System.out);
	}

	public void testBestVersionOnPath() throws Exception
	{
		IVersionDesignator designator = VersionFactory.createDesignator(VersionFactory.OSGiType, "[0.1.0,0.2.0)") ;
		ComponentRequest request = new ComponentRequest("org.eclipse.buckminster.cmdline", KeyConstants.PLUGIN_CATEGORY, designator);

		ComponentQueryBuilder queryBld = new ComponentQueryBuilder();
		queryBld.setRootRequest(request);
		queryBld.setResourceMapURL(getRMAP());

		AdvisorNodeBuilder node = new AdvisorNodeBuilder();
		node.setNamePattern(Pattern.compile("buckminster"));
		node.setUseInstalled(false);

		// Look at branch 3.2.x, tag Europa_GA, and finally in the trunk (main)
		//
		node.setBranchTagPath(VersionSelector.fromPath("3.2.x,/Europa_GA,main"));
		queryBld.addAdvisorNode(node);

		IResolver resolver = new MainResolver(new ResolutionContext(queryBld.createComponentQuery()));
		BillOfMaterials bom = resolver.resolve(new NullProgressMonitor());
		assertTrue("Resolve failed", bom.isFullyResolved());
		assertTrue(bom.getResolution().getVersionMatch().getBranchOrTag().toString().equals("3.2.x"));
		Utils.serialize(bom.getResolution(), System.out);
	}

	public void testRevisionOnBranchProvider() throws Exception
	{
		IVersionDesignator designator = VersionFactory.createExplicitDesignator(VersionFactory.OSGiType, "1.2.2") ;
		ComponentRequest request = new ComponentRequest("org.tigris.subversion.subclipse.core", KeyConstants.PLUGIN_CATEGORY, designator);

		ComponentQueryBuilder queryBld = new ComponentQueryBuilder();
		queryBld.setRootRequest(request);
		queryBld.setResourceMapURL(getRMAP());

		// Test an advisor node that will force us to look at the 1.2.x branch
		// and at a revision of that branch where we will find version 1.2.2. It
		// just so happens that that will be revision
		//
		AdvisorNodeBuilder node = new AdvisorNodeBuilder();
		node.setNamePattern(Pattern.compile("subversion"));
		node.setUseInstalled(false);
		node.setBranchTagPath(VersionSelector.fromPath("1.2.x"));
		node.setRevision(3191);
		queryBld.addAdvisorNode(node);

		IResolver resolver = new MainResolver(new ResolutionContext(queryBld.createComponentQuery()));
		BillOfMaterials bom = resolver.resolve(new NullProgressMonitor());
		assertTrue("Resolve failed", bom.isFullyResolved());
		Utils.serialize(bom.getResolution(), System.out);
	}

	public void testCSpecProvider()
	throws Exception
	{
		CorePlugin plugin = CorePlugin.getDefault();
		if(plugin == null)
			throw new Exception("This test must be run as a \"JUnit Plug-in Test\"");

		ComponentQueryBuilder queryBld = new ComponentQueryBuilder();
		queryBld.setRootRequest(new ComponentRequest("org.eclipse.buckminster.svn", KeyConstants.PLUGIN_CATEGORY, null));
		queryBld.setResourceMapURL(TestSVNCSpecProvider.class.getResource("test.rmap"));
		ComponentQuery query = queryBld.createComponentQuery();
		IResolver resolver = new MainResolver(new ResolutionContext(query));

		Format vh = new Format("http://subclipse.tigris.org/svn/subclipse/trunk/subclipse/core");
		Provider provider = new Provider("svn", "eclipse.project", null, null, vh, null, true, true, null);
		IReaderType readerType = provider.getReaderType();
		IProgressMonitor nullMon = new NullProgressMonitor();
		IComponentReader reader = readerType.getReader(provider, resolver.getContext().getRootNodeQuery(), VersionMatch.DEFAULT, nullMon);

		IProjectDescription projDesc = ProjectDescReader.getProjectDescription(reader, nullMon);

		System.out.format("Found project named: %s%n", projDesc.getName());
		reader.close();

		String[] natures = projDesc.getNatureIds();
		for(String nature : natures)
			System.out.println(nature);
	}
}
