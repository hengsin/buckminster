/*******************************************************************************
 * Copyright (c) 2006-2007, Cloudsmith Inc.
 * The code, documentation and other materials contained herein have been
 * licensed under the Eclipse Public License - v 1.0 by the copyright holder
 * listed above, as the Initial Contributor under such license. The text of
 * such license is available at www.eclipse.org.
 ******************************************************************************/
package org.eclipse.buckminster.pde.tasks;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.buckminster.core.CorePlugin;
import org.eclipse.buckminster.core.actor.AbstractActor;
import org.eclipse.buckminster.core.cspec.model.ComponentIdentifier;
import org.eclipse.buckminster.core.helpers.BMProperties;
import org.eclipse.buckminster.core.version.IQualifierGenerator;
import org.eclipse.buckminster.core.version.IVersion;
import org.eclipse.buckminster.core.version.VersionFactory;
import org.eclipse.buckminster.runtime.IOUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.pde.internal.build.IBuildPropertiesConstants;

/**
 * @author Thomas Hallgren
 * 
 */
@SuppressWarnings("restriction")
abstract class VersionConsolidator implements IBuildPropertiesConstants
{
	public static final String GENERATOR_PREFIX = "generator:";

	private static final String QUALIFIER_REPLACEMENT_PREFIX = "qualifier.replacement.";

	private static final String MATCH_ALL = QUALIFIER_REPLACEMENT_PREFIX + DEFAULT_MATCH_ALL;

	private static final SimpleDateFormat s_dateFormat = new SimpleDateFormat("yyyyMMddHHmm");

	private final File m_outputFile;

	private final Map<String, String> m_properties;

	private final String m_qualifier;

	VersionConsolidator(File outputFile, File propertiesFile, String qualifier) throws IOException
	{
		m_outputFile = outputFile;
		m_qualifier = qualifier;
		
		Map<String,String> globalProps = AbstractActor.getActiveContext().getProperties();

		if(propertiesFile == null)
			m_properties = globalProps;
		else
		{
			InputStream input = null;
			try
			{
				input = new BufferedInputStream(new FileInputStream(propertiesFile));
				m_properties = new BMProperties(input);
				m_properties.putAll(globalProps);
			}
			finally
			{
				IOUtils.close(input);
			}
		}
	}

	String generateQualifier(String id, String version, String qualifier, String componentType, List<ComponentIdentifier> deps)
	{
		String newVersion = null;
		String generatorId = qualifier.substring(GENERATOR_PREFIX.length());
		try
		{
			ComponentIdentifier cid = new ComponentIdentifier(id, componentType, VersionFactory.OSGiType.fromString(version));
			IQualifierGenerator generator = CorePlugin.getDefault().getQualifierGenerator(generatorId);
			IVersion qualifiedVersion = generator.generateQualifier(AbstractActor.getActiveContext(), cid, deps);
			if(qualifiedVersion != null)
				newVersion = qualifiedVersion.toString();
			if(version.equals(newVersion))
				newVersion = null;
		}
		catch(CoreException e)
		{
			CorePlugin.getLogger().warning("Unable to qualify version", e);
		}
		return newVersion;
	}

	File getOutputFile()
	{
		return m_outputFile;
	}

	Map<String, String> getProperties()
	{
		return m_properties;
	}
	String getQualifier()
	{
		return m_qualifier;
	}

	String getQualifierReplacement(String version, String id)
	{
		String newQualifier = null;
		if(m_qualifier == null || m_qualifier.equalsIgnoreCase(PROPERTY_CONTEXT))
		{
			if(m_properties.size() != 0)
			{
				// First we check to see if there is a match for a precise version
				//
				StringBuilder bld = new StringBuilder(QUALIFIER_REPLACEMENT_PREFIX);
				bld.append(id);
				bld.append(',');
				int lenWithId = bld.length();

				// Lookup using id,<version without the .qualifier suffix>
				//
				bld.append(version, 0, version.length() - PROPERTY_QUALIFIER.length() - 1);
				newQualifier = m_properties.get(bld.toString());

				if(newQualifier == null)
				{
					// If not found, then lookup for the id,0.0.0
					//
					bld.setLength(lenWithId);
					bld.append("0.0.0");
					newQualifier = m_properties.get(bld.toString());
					if(newQualifier == null)
						newQualifier = m_properties.get(MATCH_ALL);
				}
			}

			if(newQualifier == null)
			{
				synchronized(s_dateFormat)
				{
					newQualifier = s_dateFormat.format(new Date());
				}
			}
		}
		else if(m_qualifier.equalsIgnoreCase(PROPERTY_NONE))
			newQualifier = ""; //$NON-NLS-1$
		else
			newQualifier = m_qualifier;
		return newQualifier;
	}

	String replaceQualifierInVersion(String version, String id)
	{
		if(version.endsWith(PROPERTY_QUALIFIER))
			version = version.replaceFirst(PROPERTY_QUALIFIER, getQualifierReplacement(version, id));
		return version;
	}
}
