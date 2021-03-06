/*******************************************************************************
 * Copyright (c) 2006-2013, Cloudsmith Inc.
 * The code, documentation and other materials contained herein have been
 * licensed under the Eclipse Public License - v 1.0 by the copyright holder
 * listed above, as the Initial Contributor under such license. The text of
 * such license is available at www.eclipse.org.
 ******************************************************************************/

package org.eclipse.buckminster.core.version;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.buckminster.core.KeyConstants;
import org.eclipse.buckminster.core.actor.IActionContext;
import org.eclipse.buckminster.core.cspec.model.ComponentIdentifier;
import org.eclipse.buckminster.core.helpers.DateAndTimeUtils;
import org.eclipse.buckminster.core.metadata.MissingComponentException;
import org.eclipse.buckminster.core.metadata.WorkspaceInfo;
import org.eclipse.buckminster.core.reader.AbstractReaderType;
import org.eclipse.buckminster.core.reader.IReaderType;
import org.eclipse.buckminster.runtime.Buckminster;
import org.eclipse.buckminster.runtime.BuckminsterException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.Version;

/**
 * This class will generate qualifiers based on the last modification timestamp.
 * The timestamp is obtained using the same @ IReaderType} that was used when
 * the component was first materialized
 * 
 * @author Thomas Hallgren
 */
public class TimestampQualifierGenerator extends AbstractQualifierGenerator {
	public static final String FORMAT_PROPERTY = "generator.lastModified.format"; //$NON-NLS-1$

	public static final String DEFAULT_FORMAT = "'v'yyyyMMddHHmm"; //$NON-NLS-1$

	public static final String[] commonFormats = new String[] { DEFAULT_FORMAT, "'v'yyyyMMdd-HHmm", "'v'yyyyMMdd", //$NON-NLS-1$ //$NON-NLS-2$
			"'I'yyyyMMddHHmm", "'I'yyyyMMdd-HHmm", "'I'yyyyMMdd" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	public static final DateFormat[] commonFormatters;

	// Milliseconds corresponding to approximately 10 years
	//
	private static final long SANITY_THRESHOLD = (10L * 365L + 5L) * 24L * 60L * 60L * 1000L;

	static {
		int idx = commonFormats.length;
		commonFormatters = new DateFormat[idx];
		while (--idx >= 0) {
			DateFormat dm = new SimpleDateFormat(commonFormats[idx]);
			dm.setTimeZone(DateAndTimeUtils.UTC);
			dm.setLenient(false);
			commonFormatters[idx] = dm;
		}
	}

	public static DateFormat getDateFormat(Map<String, ? extends Object> props) {
		String format = (String) props.get(FORMAT_PROPERTY);
		if (format == null)
			format = DEFAULT_FORMAT;

		DateFormat mf = new SimpleDateFormat(format);
		mf.setTimeZone(DateAndTimeUtils.UTC);
		mf.setLenient(false);
		return mf;
	}

	private static Date getLastModification(ComponentIdentifier cid, IActionContext context) throws CoreException {
		IPath location = WorkspaceInfo.getComponentLocation(cid);
		IProject project = WorkspaceInfo.getProject(cid);
		if (project == null) {
			Buckminster.getLogger().debug("getLastModification: Failed determine project for component %s", cid); //$NON-NLS-1$
			return null;
		}
		IReaderType readerType = AbstractReaderType.getTypeForResource(project);
		if (readerType == null) {
			Buckminster.getLogger().debug("getLastModification: Failed determine reader type for component %s", cid); //$NON-NLS-1$
			return null;
		}
		try {
			return readerType.getLastModification(location.toFile(), context.getCancellationMonitor());
		} catch (RuntimeException e) {
			throw BuckminsterException.wrap(e);
		}
	}

	private static Date parseSaneDate(DateFormat mf, String str) throws ParseException {
		long now = System.currentTimeMillis();
		long sanePast = now - SANITY_THRESHOLD;
		Date dt = mf.parse(str);
		long tm = dt.getTime();
		if (tm > now || tm < sanePast)
			throw new ParseException("Bogus", 0); //$NON-NLS-1$
		return dt;
	}

	@Override
	public Version generateQualifier(IActionContext context, ComponentIdentifier cid, List<ComponentIdentifier> dependencies) throws CoreException {
		final Version currentVersion = cid.getVersion();
		if (currentVersion == null)
			return null;

		try {
			Date lastMod = getLastModification(cid, context);
			if (lastMod == null)
				return currentVersion;

			Map<String, ? extends Object> props = context.getProperties();
			DateFormat mf = getDateFormat(props);

			for (ComponentIdentifier dependency : dependencies) {
				Version depVer = dependency.getVersion();
				if (depVer == null)
					continue;

				String qualifier = VersionHelper.getQualifier(depVer);
				if (qualifier == null)
					continue;

				Date depLastMod = null;
				try {
					depLastMod = parseSaneDate(mf, qualifier);
				} catch (ParseException e) {
					// Try the common formats. Use the first one that succeeds
					//
					synchronized (commonFormatters) {
						for (int idx = 0; idx < commonFormatters.length; ++idx) {
							try {
								depLastMod = parseSaneDate(commonFormatters[idx], qualifier);
								break;
							} catch (ParseException e1) {
							}
						}
					}
				}
				if (depLastMod == null) {
					try {
						// Replace the qualifier and attempt to find the real
						// source in the workspace. If
						// found, we use the SCM timestamp for that source.
						//
						depVer = VersionHelper.replaceQualifier(depVer, "qualifier"); //$NON-NLS-1$
						depLastMod = getLastModification(new ComponentIdentifier(dependency.getName(), dependency.getComponentTypeID(), depVer),
								context);
					} catch (CoreException e) {
					}
				}

				if (depLastMod != null && depLastMod.compareTo(lastMod) > 0)
					lastMod = depLastMod;
			}

			String newQual = mf.format(lastMod);
			newQual = VersionHelper.getQualifier(currentVersion).replace("qualifier", newQual); //$NON-NLS-1$
			Version newVersion = VersionHelper.replaceQualifier(currentVersion, newQual);
			IInstallableUnit prevIU = obtainFromReferenceRepo(cid, null);
			if (prevIU == null)
				return newVersion;

			// Exactly the same version has been generated before
			String buildId = prevIU.getProperty(KeyConstants.BUILD_ID);
			if (buildId == null || buildId.equals(props.get("build.id"))) //$NON-NLS-1$
				return newVersion;

			// Component contains a generated build id that differs from the
			// current one. We need todays date.
			newQual = mf.format(BuildTimestampQualifierGenerator.getBuildTimestamp(context));
			newQual = VersionHelper.getQualifier(currentVersion).replace("qualifier", newQual); //$NON-NLS-1$
			return VersionHelper.replaceQualifier(currentVersion, newQual);
		} catch (MissingComponentException e) {
			return currentVersion;
		}
	}
}
