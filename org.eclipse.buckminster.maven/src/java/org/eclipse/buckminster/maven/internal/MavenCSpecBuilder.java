/*****************************************************************************
 * Copyright (c) 2006-2007, Cloudsmith Inc.
 * The code, documentation and other materials contained herein have been
 * licensed under the Eclipse Public License - v 1.0 by the copyright holder
 * listed above, as the Initial Contributor under such license. The text of
 * such license is available at www.eclipse.org.
 *****************************************************************************/
package org.eclipse.buckminster.maven.internal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.buckminster.core.common.model.ExpandingProperties;
import org.eclipse.buckminster.core.cspec.AbstractResolutionBuilder;
import org.eclipse.buckminster.core.cspec.builder.CSpecBuilder;
import org.eclipse.buckminster.core.cspec.builder.GroupBuilder;
import org.eclipse.buckminster.core.ctype.AbstractComponentType;
import org.eclipse.buckminster.core.ctype.MissingCSpecSourceException;
import org.eclipse.buckminster.core.metadata.model.BOMNode;
import org.eclipse.buckminster.core.reader.ICatalogReader;
import org.eclipse.buckminster.core.reader.IComponentReader;
import org.eclipse.buckminster.core.reader.IFileReader;
import org.eclipse.buckminster.core.reader.IStreamConsumer;
import org.eclipse.buckminster.core.version.ProviderMatch;
import org.eclipse.buckminster.runtime.BuckminsterException;
import org.eclipse.buckminster.runtime.MonitorUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Thomas Hallgren
 */
class MavenCSpecBuilder extends AbstractResolutionBuilder implements IStreamConsumer<Document>
{
	public BOMNode build(IComponentReader[] readerHandle, boolean forResolutionAidOnly, IProgressMonitor monitor)
	throws CoreException
	{
		IComponentReader reader = readerHandle[0];
		ProviderMatch ri = reader.getProviderMatch();
		monitor.beginTask(null, 3000);
		monitor.subTask("Generating cspec from Maven artifact");
		try
		{
			IPath pomPath = null;
			Document pomDoc;
			IProgressMonitor subMon = MonitorUtils.subMonitor(monitor, 2000);
			if(reader instanceof MavenReader)
			{
				// We are reading from a maven repository. In that case, we will
				// allow a missing pom file.
				//
				IPath[] pomPathRet = new IPath[1];
				pomDoc = ((MavenReader)reader).getPOMDocument(pomPathRet, subMon);
				pomPath = pomPathRet[0];
			}
			else
			{
				// Some other reader is used. This reader is either reading a source
				// directory (catalog reader) or a pom/project file directly. In any
				// case, we consider a missing file an exceptional condition.
				//
				try
				{
					if(reader instanceof ICatalogReader)
					{
						try
						{
							pomDoc = ((ICatalogReader)reader).readFile("pom.xml", this, subMon);
						}
						catch(FileNotFoundException e)
						{
							pomDoc = ((ICatalogReader)reader).readFile("project.xml", this, subMon);
						}
					}
					else
						pomDoc = ((IFileReader)reader).readFile(this, subMon);
				}
				catch(FileNotFoundException e2)
				{
					throw new MissingCSpecSourceException(reader.getProviderMatch());
				}
			}

			CSpecBuilder cspecBld = ri.createCSpec();
			GroupBuilder archives = AbstractComponentType.addSelfAsJarArtifactGroups(cspecBld);
			if(pomDoc != null)
			{
				ExpandingProperties properties = new ExpandingProperties();
				MavenComponentType.addDependencies(reader, pomDoc, pomPath, cspecBld, archives, properties);
			}

			applyExtensions(cspecBld, forResolutionAidOnly, reader, MonitorUtils.subMonitor(monitor, 1000));
			return createNode(reader, cspecBld, null);
		}
		catch(IOException e)
		{
			throw BuckminsterException.wrap(e);
		}
		finally
		{
			monitor.done();
		}
	}

	public Document consumeStream(IComponentReader reader, String streamName, InputStream stream, IProgressMonitor monitor)
	throws CoreException, IOException
	{
		monitor.beginTask(streamName, 1);
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource source = new InputSource(stream);
			source.setSystemId(streamName);
			return builder.parse(source);
		}
		catch(SAXException e)
		{
			throw BuckminsterException.wrap(e);
		}
		catch(ParserConfigurationException e)
		{
			throw BuckminsterException.wrap(e);
		}
		finally
		{
			MonitorUtils.worked(monitor, 1);
			monitor.done();
		}
	}
}