package org.eclipse.buckminster.galileo.builder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.tools.ant.Project;
import org.eclipse.amalgam.releng.build.Build;
import org.eclipse.amalgam.releng.build.BuildPackage;
import org.eclipse.amalgam.releng.build.Contact;
import org.eclipse.amalgam.releng.build.Contribution;
import org.eclipse.amalgam.releng.build.Promotion;
import org.eclipse.amalgam.releng.build.Repository;
import org.eclipse.buckminster.runtime.Buckminster;
import org.eclipse.buckminster.runtime.BuckminsterException;
import org.eclipse.buckminster.runtime.IOUtils;
import org.eclipse.buckminster.runtime.Logger;
import org.eclipse.buckminster.runtime.MonitorUtils;
import org.eclipse.buckminster.runtime.MultiTeeOutputStream;
import org.eclipse.buckminster.runtime.NullOutputStream;
import org.eclipse.buckminster.runtime.URLUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.equinox.internal.p2.core.helpers.FileUtils;
import org.eclipse.equinox.internal.provisional.p2.core.Version;
import org.eclipse.equinox.internal.provisional.p2.engine.IProfile;
import org.eclipse.equinox.internal.provisional.p2.engine.IProfileRegistry;
import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;
import org.eclipse.m2m.internal.qvt.oml.common.launch.TargetUriData;
import org.eclipse.m2m.internal.qvt.oml.emf.util.ModelContent;
import org.eclipse.m2m.internal.qvt.oml.runtime.launch.QvtLaunchConfigurationDelegateBase;
import org.eclipse.m2m.internal.qvt.oml.runtime.project.DeployedQvtModule;
import org.eclipse.m2m.internal.qvt.oml.runtime.project.QvtInterpretedTransformation;
import org.eclipse.m2m.internal.qvt.oml.runtime.project.QvtTransformation;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.service.packageadmin.PackageAdmin;

@SuppressWarnings("restriction")
public class Builder implements IApplication {
	public static final String NAMESPACE_OSGI_BUNDLE = "osgi.bundle"; //$NON-NLS-1$

	public static final String PROFILE_ID = "GalileoTest"; //$NON-NLS-1$

	public static final String ALL_CONTRIBUTED_CONTENT_FEATURE = "all.contributed.content.feature.group"; //$NON-NLS-1$

	public static final Version ALL_CONTRIBUTED_CONTENT_VERSION = new Version(1, 0, 0);

	public static final String GALILEO_FEATURE = "org.eclipse.galileo"; //$NON-NLS-1$

	public static final String LINE_SEPARATOR = System.getProperty("line.separator"); //$NON-NLS-1$

	private static final String BUNDLE_ECF_FS_PROVIDER = "org.eclipse.ecf.provider.filetransfer"; //$NON-NLS-1$

	private static final String BUNDLE_EXEMPLARY_SETUP = "org.eclipse.equinox.p2.exemplarysetup"; //$NON-NLS-1$

	private static final String BUNDLE_UPDATESITE = "org.eclipse.equinox.p2.updatesite"; //$NON-NLS-1$

	private static final String CORE_BUNDLE = "org.eclipse.equinox.p2.core"; //$NON-NLS-1$

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd"); //$NON-NLS-1$

	private static final String PROP_P2_DATA_AREA = "eclipse.p2.data.area";

	private static final String PROP_P2_PROFILE = "eclipse.p2.profile";

	private static final Project PROPERTY_REPLACER = new Project();

	private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HHmm"); //$NON-NLS-1$

	private static final String TP_CONTRIBUTION_LABEL = "Eclipse"; //$NON-NLS-1$

	static {
		TimeZone utc = TimeZone.getTimeZone("UTC"); //$NON-NLS-1$
		PROPERTY_REPLACER.initProperties();
		DATE_FORMAT.setTimeZone(utc);
		TIME_FORMAT.setTimeZone(utc);
	}

	private static InternetAddress contactToAddress(Contact contact) throws UnsupportedEncodingException {
		InternetAddress addr = new InternetAddress();
		addr.setPersonal(contact.getName());
		addr.setAddress(contact.getEmail());
		return addr;
	}

	/**
	 * Creates a repository location without the trailing slash that will be
	 * added if the standard {@link java.io.File#toURI()} is used.
	 * 
	 * @param repoLocation
	 *            The location. Must be an absolute path.
	 * @return The created URI.
	 * @throws CoreException
	 *             if the argument is not an absolute path
	 */
	public static final URI createURI(File repoLocation) throws CoreException {
		if (repoLocation != null) {
			IPath path = Path.fromOSString(repoLocation.getPath());
			if (path.isAbsolute())
				try {
					String pathStr = path.removeTrailingSeparator().toPortableString();
					if (!pathStr.startsWith("/"))
						// Path starts with a drive letter
						pathStr = "/" + pathStr;
					return new URI("file", null, pathStr, null);
				} catch (URISyntaxException e) {
					throw BuckminsterException.wrap(e);
				}
		}
		throw BuckminsterException.fromMessage("File %s is not an absolute path", repoLocation);
	}

	private static synchronized Bundle getBundle(PackageAdmin packageAdmin, String symbolicName) {
		Bundle[] bundles = packageAdmin.getBundles(symbolicName, null);
		if (bundles == null)
			return null;

		// Return the first bundle that is not installed or uninstalled
		for (int i = 0; i < bundles.length; i++) {
			Bundle bundle = bundles[i];
			if ((bundle.getState() & (Bundle.INSTALLED | Bundle.UNINSTALLED)) == 0)
				return bundle;
		}
		return null;
	}

	public static String getExceptionMessages(Throwable e) {
		StringBuilder bld = new StringBuilder();
		getExceptionMessages(e, bld);
		return bld.toString();
	}

	private static void getExceptionMessages(Throwable e, StringBuilder bld) {
		bld.append(e.getClass().getName());
		bld.append(": ");
		if (e.getMessage() != null)
			bld.append(e.getMessage());

		if (e instanceof CoreException)
			e = ((CoreException) e).getStatus().getException();
		else {
			Throwable t = e.getCause();
			e = (t == e) ? null : t;
		}
		if (e != null) {
			bld.append("\nCaused by: ");
			getExceptionMessages(e, bld);
		}
	}

	private static boolean startEarly(PackageAdmin packageAdmin, String bundleName) throws BundleException {
		Bundle bundle = getBundle(packageAdmin, bundleName);
		if (bundle == null)
			return false;
		bundle.start(Bundle.START_TRANSIENT);
		return true;
	}

	private static boolean stopBundle(PackageAdmin packageAdmin, String bundleName) throws BundleException {
		Bundle bundle = getBundle(packageAdmin, bundleName);
		if (bundle == null || bundle.getState() != Bundle.ACTIVE)
			return false;
		bundle.stop(Bundle.STOP_TRANSIENT);
		return true;
	}

	private Build build;

	private String buildID;

	private File buildModelLocation;

	private File buildRoot;

	private URI categoriesRepo;

	private int logLevel = Logger.INFO;

	private OutputStream logOutput;

	private String mockEmailCC;

	private String mockEmailTo;

	private boolean production;

	private URI targetPlatformRepo;

	private Set<IInstallableUnit> unitsToInstall;

	private boolean update;

	private boolean verifyOnly;

	public static final String SIMPLE_METADATA_TYPE = org.eclipse.equinox.internal.p2.metadata.repository.Activator.ID + ".simpleRepository"; //$NON-NLS-1$

	public static final String SIMPLE_ARTIFACTS_TYPE = org.eclipse.equinox.internal.p2.artifact.repository.Activator.ID + ".simpleRepository"; //$NON-NLS-1$

	public static final String COMPOSITE_METADATA_TYPE = org.eclipse.equinox.internal.p2.metadata.repository.Activator.ID + ".compositeRepository"; //$NON-NLS-1$

	public static final String COMPOSITE_ARTIFACTS_TYPE = org.eclipse.equinox.internal.p2.artifact.repository.Activator.ID + ".compositeRepository"; //$NON-NLS-1$

	static final String FEATURE_GROUP_SUFFIX = ".feature.group"; //$NON-NLS-1$

	public static final String PLATFORM_REPO_NAME = "Platform Repository"; //$NON-NLS-1$

	public static final String PLATFORM_REPO_FOLDER = "platform"; //$NON-NLS-1$

	public static final String CATEGORY_REPO_FOLDER = "categories"; //$NON-NLS-1$

	public static final String COMPOSITE_REPO_FOLDER = "composite"; //$NON-NLS-1$

	public static final String MIRROR_REPO_FOLDER = "mirror"; //$NON-NLS-1$

	// A list of messages to be printed to the log file once we know which file
	// that is.
	private List<String> deferredLogMessages = new ArrayList<String>();

	private String smtpHost;

	private int smtpPort;

	private String smtpUser;

	private String smtpPassword;

	public Build getBuild() {
		return build;
	}

	public String getBuildID() {
		return buildID;
	}

	public File getBuildModelLocation() {
		return buildModelLocation;
	}

	public File getBuildRoot() {
		return buildRoot;
	}

	public URI getCategoriesRepo() {
		return categoriesRepo;
	}

	public URI getGlobalRepoURI() throws CoreException {
		return createURI(new File(buildRoot, COMPOSITE_REPO_FOLDER));
	}

	public URI getMirrorsURI() throws CoreException {
		Promotion promotion = build.getPromotion();
		if (promotion == null)
			throw BuckminsterException.fromMessage("Missing required element <promition>");

		URI mirrorsURI = URI.create(PROPERTY_REPLACER.replaceProperties(promotion.getBaseURL()));
		String downloadDirectory = PROPERTY_REPLACER.replaceProperties(promotion.getDownloadDirectory());
		if (downloadDirectory != null) {
			try {
				if (mirrorsURI.getPath().endsWith("/download.php")) //$NON-NLS-1$
				{
					String query = mirrorsURI.getQuery();
					Map<String, String> params = (query == null) ? new HashMap<String, String>() : URLUtils.queryAsParameters(query);
					params.put("file", downloadDirectory); //$NON-NLS-1$
					if (!params.containsKey("protocol")) //$NON-NLS-1$
						params.put("protocol", "http"); //$NON-NLS-1$//$NON-NLS-2$
					if (!params.containsKey("format")) //$NON-NLS-1$
						params.put("format", "xml"); //$NON-NLS-1$//$NON-NLS-2$
					mirrorsURI = new URI(mirrorsURI.getScheme(), mirrorsURI.getAuthority(), mirrorsURI.getPath(), URLUtils
							.encodeFromQueryPairs(params), mirrorsURI.getFragment());
				} else
					mirrorsURI = new URI(mirrorsURI.getScheme(), mirrorsURI.getHost(), mirrorsURI.getPath() + '/' + downloadDirectory, mirrorsURI
							.getFragment());
			} catch (URISyntaxException e) {
				throw BuckminsterException.wrap(e);
			}
		}
		return mirrorsURI;
	}

	public URI getTargetPlatformRepo() throws CoreException {
		if (targetPlatformRepo != null)
			return targetPlatformRepo;

		for (Contribution contrib : build.getContributions())
			if (TP_CONTRIBUTION_LABEL.equals(contrib.getLabel())) {
				List<Repository> repos = contrib.getRepositories();
				if (repos.size() == 1) {
					targetPlatformRepo = URI.create(repos.get(0).getLocation());
					break;
				}
			}

		if (targetPlatformRepo == null)
			throw BuckminsterException
					.fromMessage(
							"The build requires that a contribution named '%s' and appoints one repository. This is where the build extracts the target platform", //$NON-NLS-1$
							TP_CONTRIBUTION_LABEL);

		return targetPlatformRepo;
	}

	public Set<IInstallableUnit> getUnitsToInstall() {
		return unitsToInstall;
	}

	public boolean isProduction() {
		return production;
	}

	public boolean isUpdate() {
		return update;
	}

	public boolean isVerifyOnly() {
		return verifyOnly;
	}

	private InternetAddress mockCCRecipient() throws UnsupportedEncodingException {
		InternetAddress mock = null;
		if (mockEmailCC != null) {
			mock = new InternetAddress();
			mock.setAddress(mockEmailCC);
		}
		return mock;
	}

	private List<InternetAddress> mockRecipients() throws UnsupportedEncodingException {
		if (mockEmailTo != null) {
			InternetAddress mock = new InternetAddress();
			mock.setAddress(mockEmailTo);
			return Collections.singletonList(mock);
		}
		return Collections.emptyList();
	}

	private void parseCommandLineArgs(String[] args) {
		int top = args.length;
		for (int idx = 0; idx < top; ++idx) {
			String arg = args[idx];
			if ("-verifyOnly".equalsIgnoreCase(arg)) {
				setVerifyOnly(true);
				continue;
			}
			if ("-updateOnly".equalsIgnoreCase(arg)) {
				setUpdate(true);
				continue;
			}
			if ("-production".equalsIgnoreCase(arg)) {
				setProduction(true);
				continue;
			}
			if ("-mockEmailTo".equalsIgnoreCase(arg)) {
				if (++idx >= top)
					throw new IllegalArgumentException("-mockEmailTo requires an argument");
				setMockEmailTo(args[idx]);
				continue;
			}
			if ("-smtpHost".equalsIgnoreCase(arg)) {
				if (++idx >= top)
					throw new IllegalArgumentException("-smtpHost requires an argument");
				setSmtpHost(args[idx]);
				continue;
			}
			if ("-smtpPort".equalsIgnoreCase(arg)) {
				if (++idx >= top)
					throw new IllegalArgumentException("-smtpPort requires an argument");
				int portNumber = 0;
				try {
					portNumber = Integer.parseInt(args[idx]);
				} catch (NumberFormatException e) {
				}
				if (portNumber <= 0)
					throw new IllegalArgumentException("-smtpPort must be a positive integer");
				setSmtpPort(portNumber);
				continue;
			}
			if ("-smtpUser".equalsIgnoreCase(arg)) {
				if (++idx >= top)
					throw new IllegalArgumentException("-smtpUser requires an argument");
				setSmtpUser(args[idx]);
				continue;
			}
			if ("-smtpPassword".equalsIgnoreCase(arg)) {
				if (++idx >= top)
					throw new IllegalArgumentException("-smtpPassword requires an argument");
				setSmtpPassword(args[idx]);
				continue;
			}
			if ("-mockEmailCC".equalsIgnoreCase(arg)) {
				if (++idx >= top)
					throw new IllegalArgumentException("-mockEmailCC requires an argument");
				setMockEmailCC(args[idx]);
				continue;
			}
			if ("-logLevel".equalsIgnoreCase(arg)) {
				if (++idx >= top)
					throw new IllegalArgumentException("-logLevel requires an argument");
				String levelStr = args[idx];
				int level;
				if ("debug".equalsIgnoreCase(levelStr))
					level = Logger.DEBUG;
				else if ("info".equalsIgnoreCase(levelStr))
					level = Logger.INFO;
				else if ("warning".equalsIgnoreCase(levelStr))
					level = Logger.WARNING;
				else if ("error".equalsIgnoreCase(levelStr))
					level = Logger.WARNING;
				else
					throw new IllegalArgumentException(String.format("%s is not a valid logLevel", levelStr));

				setLogLevel(level);
				continue;
			}
			if ("-buildModel".equalsIgnoreCase(arg)) {
				if (++idx >= top)
					throw new IllegalArgumentException("-buildModel requires an argument");
				File buildModel = new File(args[idx]);
				if (!buildModel.canRead())
					throw new IllegalArgumentException(String.format("Unable to read %s", buildModel));
				setBuildModelLocation(buildModel);
				continue;
			}
			if ("-buildRoot".equalsIgnoreCase(arg)) {
				if (++idx >= top)
					throw new IllegalArgumentException("-buildRoot requires an argument");
				setBuildRoot(new File(args[idx]));
				continue;
			}
			if ("-buildId".equalsIgnoreCase(arg)) {
				if (++idx >= top)
					throw new IllegalArgumentException("-buildId requires an argument");
				setBuildID(args[idx]);
				continue;
			}
			if ("-targetPlatformRepository".equalsIgnoreCase(arg)) {
				if (++idx >= top)
					throw new IllegalArgumentException("-targetPlatformRepository requires an argument");
				setTargetPlatformRepo(URI.create(args[idx]));
				continue;
			}
			String msg = String.format("Unknown option %s", arg);
			Buckminster.getLogger().error(msg);
			throw new IllegalArgumentException(msg);
		}
	}

	/**
	 * Run the build
	 * 
	 * @param monitor
	 */
	public Object run(IProgressMonitor monitor) {
		MonitorUtils.begin(monitor, verifyOnly ? 100 : 1100);

		try {
			if (buildModelLocation == null)
				throw BuckminsterException.fromMessage("No buildmodel has been set");

			if (buildID == null) {
				Date now = new Date();
				buildID = "build-" + DATE_FORMAT.format(now) + TIME_FORMAT.format(now);
			}

			if (smtpHost == null)
				smtpHost = "localhost";

			if (smtpPort <= 0)
				smtpPort = 25;

			runTransformation();
			Buckminster bucky = Buckminster.getDefault();
			PackageAdmin packageAdmin = bucky.getService(PackageAdmin.class);
			try {
				stopBundle(packageAdmin, BUNDLE_EXEMPLARY_SETUP);
				stopBundle(packageAdmin, CORE_BUNDLE);

				String p2DataArea = new File(buildRoot, "p2").toString();
				System.setProperty(PROP_P2_DATA_AREA, p2DataArea);
				System.setProperty(PROP_P2_PROFILE, PROFILE_ID);

				if (!startEarly(packageAdmin, BUNDLE_ECF_FS_PROVIDER))
					throw BuckminsterException.fromMessage("Missing bundle %s", BUNDLE_ECF_FS_PROVIDER);
				if (!startEarly(packageAdmin, CORE_BUNDLE))
					throw BuckminsterException.fromMessage("Missing bundle %s", CORE_BUNDLE);
				if (!startEarly(packageAdmin, BUNDLE_EXEMPLARY_SETUP))
					throw BuckminsterException.fromMessage("Missing bundle %s", BUNDLE_EXEMPLARY_SETUP);
				if (!startEarly(packageAdmin, BUNDLE_UPDATESITE))
					throw BuckminsterException.fromMessage("Missing bundle %s", BUNDLE_UPDATESITE);

				IProfile profile = null;
				IProfileRegistry profileRegistry = bucky.getService(IProfileRegistry.class);
				if (update)
					profile = profileRegistry.getProfile(PROFILE_ID);

				if (profile == null) {
					String instArea = buildRoot.toString();
					Map<String, String> props = new HashMap<String, String>();
					props.put(IProfile.PROP_FLAVOR, "tooling"); //$NON-NLS-1$
					props.put(IProfile.PROP_NAME, build.getLabel());
					props.put(IProfile.PROP_DESCRIPTION, String.format("Default profile during %s build", build.getLabel()));
					props.put(IProfile.PROP_CACHE, instArea); //$NON-NLS-1$
					props.put(IProfile.PROP_INSTALL_FOLDER, instArea);
					profile = profileRegistry.addProfile(PROFILE_ID, props);
				}
				bucky.ungetService(profileRegistry);
			} catch (BundleException e) {
				throw BuckminsterException.wrap(e);
			} finally {
				bucky.ungetService(packageAdmin);
			}

			runCompositeGenerator(MonitorUtils.subMonitor(monitor, 70));
			runCategoriesRepoGenerator(MonitorUtils.subMonitor(monitor, 10));
			runRepositoryVerifier(MonitorUtils.subMonitor(monitor, 20));
			if (!verifyOnly)
				runMirroring(MonitorUtils.subMonitor(monitor, 1000));
		} catch (Throwable e) {
			Buckminster.getLogger().error(e, "Build failed! Exception was %s", getExceptionMessages(e));
			if (e instanceof Error)
				throw (Error) e;
			return Integer.valueOf(1);
		} finally {
			monitor.done();
		}
		return IApplication.EXIT_OK;
	}

	private void runCategoriesRepoGenerator(IProgressMonitor monitor) throws CoreException {
		CategoryRepoGenerator extraGenerator = new CategoryRepoGenerator(this);
		extraGenerator.run(monitor);
	}

	private void runCompositeGenerator(IProgressMonitor monitor) throws CoreException {
		CompositeRepoGenerator repoGenerator = new CompositeRepoGenerator(this);
		repoGenerator.run(monitor);
	}

	private void runMirroring(IProgressMonitor monitor) throws CoreException {
		MirrorGenerator mirrorGenerator = new MirrorGenerator(this);
		mirrorGenerator.run(monitor);
	}

	private void runRepositoryVerifier(IProgressMonitor monitor) throws CoreException {
		RepositoryVerifier ipt = new RepositoryVerifier(this);
		ipt.run(monitor);
	}

	/**
	 * Runs the transformation and loads the model into memory
	 * 
	 * @throws CoreException
	 *             If something goes wrong with during the process
	 */
	private void runTransformation() throws CoreException {
		File generatedBuildModel = null;
		try {
			// Transform the model, i.e. collect all contributions and create
			// one single build model file
			Date today = new Date();
			Map<String, Object> configuration = new HashMap<String, Object>();
			configuration.put("date", DATE_FORMAT.format(today)); //$NON-NLS-1$
			configuration.put("time", TIME_FORMAT.format(today)); //$NON-NLS-1$
			QvtTransformation transf = new QvtInterpretedTransformation(new DeployedQvtModule('/' + Activator.PLUGIN_ID + "/build.qvto")); //$NON-NLS-1$
			List<ModelContent> inObjects = Collections.singletonList(transf.loadInput(org.eclipse.emf.common.util.URI
					.createFileURI(buildModelLocation.getAbsolutePath())));
			generatedBuildModel = File.createTempFile("buildModel_", ".tmp"); //$NON-NLS-1$//$NON-NLS-2$

			List<TargetUriData> targetData = Collections.singletonList(new TargetUriData(createURI(generatedBuildModel).toString()));
			QvtLaunchConfigurationDelegateBase.doLaunch(transf, inObjects, targetData, configuration, null);

			// Load the Java model into memory
			ResourceSet resourceSet = new ResourceSetImpl();
			resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(Resource.Factory.Registry.DEFAULT_EXTENSION,
					new XMIResourceFactoryImpl());
			BuildPackage.eINSTANCE.eClass();
			org.eclipse.emf.common.util.URI fileURI = org.eclipse.emf.common.util.URI.createFileURI(generatedBuildModel.getAbsolutePath());
			Resource resource = resourceSet.getResource(fileURI, true);
			EList<EObject> content = resource.getContents();
			if (content.size() != 1)
				throw BuckminsterException.fromMessage("ECore Resource did not contain one resource. It had %d", Integer.valueOf(content.size()));

			build = (Build) content.get(0);
			if (buildRoot == null)
				buildRoot = new File(PROPERTY_REPLACER.replaceProperties(build.getBuildRoot()));

			if (buildRoot.exists()) {
				if (!update) {
					FileUtils.deleteAll(buildRoot);
					if (buildRoot.exists())
						throw BuckminsterException.fromMessage("Failed to delete folder %s", buildRoot);
				}
			}
			buildRoot.mkdirs();
			if (!buildRoot.exists())
				throw BuckminsterException.fromMessage("Failed to create folder %s", buildRoot);

			logOutput = new FileOutputStream(new File(buildRoot, buildID + ".log.txt"));

			// Print deferred messages (logged before we knew what file to use)
			PrintStream tmp = new PrintStream(logOutput);
			for (String msg : deferredLogMessages)
				tmp.println(msg);
			tmp.flush();

			MultiTeeOutputStream outMux = new MultiTeeOutputStream(new OutputStream[] { logOutput, System.out });
			MultiTeeOutputStream errMux = new MultiTeeOutputStream(new OutputStream[] { logOutput, System.err });
			Logger.setOutStream(new PrintStream(outMux));
			Logger.setErrStream(new PrintStream(errMux));
			Logger.setConsoleLevelThreshold(logLevel);
			Logger.setEclipseLoggerLevelThreshold(Logger.SILENT);
		} catch (Exception e) {
			throw BuckminsterException.wrap(e);
		} finally {
			if (generatedBuildModel != null)
				generatedBuildModel.delete();
		}
	}

	public void sendEmail(Contribution contrib, List<String> errors) {
		boolean useMock = (mockEmailTo != null);
		if (!(production || useMock) && build.isSendmail())
			return;

		Logger log = Buckminster.getLogger();
		try {
			InternetAddress buildMaster = contactToAddress(build.getBuildmaster());
			List<InternetAddress> toList = new ArrayList<InternetAddress>();
			for (Contact contact : contrib.getContacts())
				toList.add(contactToAddress(contact));

			StringBuilder msgBld = new StringBuilder();
			msgBld.append("The following error");
			if (errors.size() > 1)
				msgBld.append('s');
			msgBld.append(" occured when building ");
			msgBld.append(build.getLabel());
			msgBld.append(":\n\n");
			for (String error : errors) {
				msgBld.append(error);
				msgBld.append("\n\n");
			}
			msgBld.append("Check the log file for more information: ");
			msgBld.append(build.getBuilderURL());
			msgBld.append(buildID);
			msgBld.append(".log.txt\n");
			if (useMock) {
				msgBld.append("\nThis is a mock mail. Real recipients would have been:\n");
				for (InternetAddress to : toList) {
					msgBld.append("  ");
					msgBld.append(to);
					msgBld.append('\n');
				}
			}
			String msgContent = msgBld.toString();
			String subject = String.format("%s build failed", build.getLabel());

			msgBld.setLength(0);
			msgBld.append("Sending email to: ");
			for (InternetAddress to : toList) {
				msgBld.append(to);
				msgBld.append(',');
			}
			msgBld.append(buildMaster);
			if(useMock)
			{
				msgBld.append(" *** Using mock: ");
				if(mockEmailTo != null)
				{
					msgBld.append(mockEmailTo);
					if(mockEmailCC != null)
					{
						msgBld.append(',');
						msgBld.append(mockEmailTo);
					}
				}
				else
					msgBld.append(mockEmailCC);
				msgBld.append(" ***");
			}
			log.info(msgBld.toString());
			log.info("Subject: %s", subject);
			log.info("Message content: %s", msgContent);

			Properties props = new Properties();
			Session session = Session.getDefaultInstance(props);
			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(buildMaster);

			if (useMock) {
				List<InternetAddress> recipients = mockRecipients();
				msg.setRecipients(RecipientType.TO, recipients.toArray(new InternetAddress[recipients.size()]));
				InternetAddress ccRecipient = mockCCRecipient();
				if (ccRecipient != null)
					msg.setRecipient(RecipientType.CC, ccRecipient);
			} else {
				msg.setRecipients(RecipientType.TO, toList.toArray(new InternetAddress[toList.size()]));
				msg.setRecipient(RecipientType.CC, buildMaster);
			}

			msg.setText(msgContent);
			msg.setSubject(subject);

			// For some odd reason, the Geronimo SMTPTransport class chooses to
			// output
			// lots of completely meaningless output to System.out and there's
			// absolutely
			// no way to prevent that from happening.
			PrintStream sysOut = System.out;
			sysOut.flush();
			System.setOut(new PrintStream(new NullOutputStream()));
			try {
				Transport transport = session.getTransport("smtp");
				transport.connect(smtpHost, smtpPort, smtpUser, smtpPassword);
				transport.sendMessage(msg, msg.getAllRecipients());
				transport.close();
			} finally {
				System.setOut(sysOut);
			}

		} catch (MessagingException e) {
			log.error(e, "Failed to send email: %s", e.getMessage());
		} catch (UnsupportedEncodingException e) {
			log.error(e, "Failed to send email: %s", e.getMessage());
		}
	}

	public void setBuildID(String buildId) {
		this.buildID = buildId;
	}

	public void setBuildModelLocation(File buildModelLocation) {
		this.buildModelLocation = buildModelLocation;
	}

	public void setBuildRoot(File buildRoot) {
		this.buildRoot = buildRoot;
	}

	public void setCategoriesRepo(URI categoriesRepo) {
		this.categoriesRepo = categoriesRepo;
	}

	public void setLogLevel(int level) {
		logLevel = level;
	}

	public void setMockEmailCC(String mockEmailCc) {
		this.mockEmailCC = mockEmailCc;
	}

	public void setMockEmailTo(String mockEmailTo) {
		this.mockEmailTo = mockEmailTo;
	}

	public void setProduction(boolean production) {
		this.production = production;
	}

	public void setSmtpHost(String smtpHost) {
		this.smtpHost = smtpHost;
	}

	public void setSmtpPassword(String smtpPassword) {
		this.smtpPassword = smtpPassword;
	}

	public void setSmtpPort(int smtpPort) {
		this.smtpPort = smtpPort;
	}

	public void setSmtpUser(String smtpUser) {
		this.smtpUser = smtpUser;
	}

	public void setTargetPlatformRepo(URI targetPlatformRepo) {
		this.targetPlatformRepo = targetPlatformRepo;
	}

	public void setUnitsToInstall(Set<IInstallableUnit> unitsToInstall) {
		this.unitsToInstall = unitsToInstall;
	}

	public void setUpdate(boolean update) {
		this.update = update;
	}

	public void setVerifyOnly(boolean verifyOnly) {
		this.verifyOnly = verifyOnly;
	}

	public Object start(IApplicationContext context) throws Exception {

		String[] args = (String[]) context.getArguments().get("application.args");
		Logger log = Buckminster.getLogger();
		StringBuilder msgBld = new StringBuilder();
		msgBld.append("Running with arguments: ");
		for (String arg : args) {
			msgBld.append("  '");
			msgBld.append(arg);
			msgBld.append('\'');
			msgBld.append(LINE_SEPARATOR);
		}
		String msg = msgBld.toString();
		deferredLogMessages.add(msg);
		try {
			parseCommandLineArgs(args); //$NON-NLS-1$
			log.debug(msg);
		} catch (Exception e) {
			// We use error level when the arguments are corrupt since the user
			// didn't
			// have a chance to set the debug level
			log.info(msg);
			return Integer.valueOf(1);
		}
		return run(new NullProgressMonitor());
	}

	public void stop() {
		if (logOutput != null) {
			Logger.setOutStream(System.out);
			Logger.setErrStream(System.err);
			IOUtils.close(logOutput);
			logOutput = null;
		}
	}
}
