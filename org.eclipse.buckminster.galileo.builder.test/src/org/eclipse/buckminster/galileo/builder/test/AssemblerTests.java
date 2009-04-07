package org.eclipse.buckminster.galileo.builder.test;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.buckminster.galileo.builder.Builder;
import org.eclipse.buckminster.runtime.Buckminster;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.service.packageadmin.PackageAdmin;

public class AssemblerTests extends TestCase
{
	static private final String EXEMPLARY_SETUP = "org.eclipse.equinox.p2.exemplarysetup"; //$NON-NLS-1$

	public static Test suite() throws Exception
	{
		File tpLocation = new File("/home/thhal/tmp/eclipse");
		Buckminster bucky = Buckminster.getDefault();
		PackageAdmin packageAdmin = bucky.getService(PackageAdmin.class);
		try
		{
			if(!startEarly(packageAdmin, EXEMPLARY_SETUP))
			{
				Buckminster.getLogger().error(NLS.bind("Missing bundle {0}", EXEMPLARY_SETUP));
				return null;
			}
		}
		finally
		{
			bucky.ungetService(packageAdmin);
		}

		File baseLocation = new File("/home/thhal/tmp/galileo.test/tmp");
		Builder b = new Builder(new File(
				"/home/thhal/workspaces/galileo-build/org.eclipse.galileo.build/out/build/galileo/galileo.build"),
				baseLocation);

		TestSuite suite = new TestSuite("Tests for org.eclipse.buckminster.galileo.assembler"); //$NON-NLS-1$
		File targetRepoLocation = new File("/home/thhal/tmp/galileo.test/repo");
		suite.addTest(new AssemblerTests("testCompositeGenerator", b, tpLocation, targetRepoLocation));
		suite.addTest(new AssemblerTests("testPlatformRepoGenerator", b, tpLocation, targetRepoLocation));
		suite.addTest(new AssemblerTests("testExtraRepoGenerator", b, tpLocation, targetRepoLocation));
		suite.addTest(new AssemblerTests("testRepositoryVerifier", b, tpLocation, targetRepoLocation));
		suite.addTest(new AssemblerTests("testMirroring", b, tpLocation, targetRepoLocation));
		return suite;
	}

	private static synchronized Bundle getBundle(PackageAdmin packageAdmin, String symbolicName)
	{
		Bundle[] bundles = packageAdmin.getBundles(symbolicName, null);
		if(bundles == null)
			return null;
		// Return the first bundle that is not installed or uninstalled
		for(int i = 0; i < bundles.length; i++)
		{
			if((bundles[i].getState() & (Bundle.INSTALLED | Bundle.UNINSTALLED)) == 0)
			{
				return bundles[i];
			}
		}
		return null;
	}

	private static boolean startEarly(PackageAdmin packageAdmin, String bundleName) throws BundleException
	{
		Bundle bundle = getBundle(packageAdmin, bundleName);
		if(bundle == null)
			return false;
		bundle.start(Bundle.START_TRANSIENT);
		return true;
	}

	private final Builder m_builder;

	private final File m_targetPlatformLocation;

	private final File m_targetRepoLocation;

	public AssemblerTests(String testName, Builder builder, File targetPlatformLocation, File targetRepoLocation)
	{
		super(testName);
		m_builder = builder;
		m_targetPlatformLocation = targetPlatformLocation;
		m_targetRepoLocation = targetRepoLocation;
	}

	public void testCompositeGenerator() throws Exception
	{
		m_builder.runCompositeGenerator(new NullProgressMonitor());
	}

	public void testExtraRepoGenerator() throws Exception
	{
		m_builder.runExtraRepoGenerator(new NullProgressMonitor());
	}

	public void testMirroring() throws Exception
	{
		m_builder.runMirroring(new NullProgressMonitor(), m_targetRepoLocation);
	}

	public void testPlatformRepoGenerator() throws Exception
	{
		m_builder.runPlatformRepoGenerator(new NullProgressMonitor(), m_targetPlatformLocation);
	}

	public void testRepositoryVerifier() throws Exception
	{
		m_builder.runRepositoryVerifier(new NullProgressMonitor());
	}
}
