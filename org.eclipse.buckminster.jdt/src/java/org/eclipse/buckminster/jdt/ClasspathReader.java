/*******************************************************************************
 * Copyright (c) 2004, 2006
 * Thomas Hallgren, Kenneth Olwing, Mitch Sonies
 * Pontus Rydin, Nils Unden, Peer Torngren
 * The code, documentation and other materials contained herein have been
 * licensed under the Eclipse Public License - v 1.0 by the individual
 * copyright holders listed above, as Initial Contributors under such license.
 * The text of such license is available at www.eclipse.org.
 *******************************************************************************/

package org.eclipse.buckminster.jdt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.buckminster.core.helpers.FileUtils;
import org.eclipse.buckminster.core.reader.ICatalogReader;
import org.eclipse.buckminster.core.reader.IComponentReader;
import org.eclipse.buckminster.core.reader.IFileReader;
import org.eclipse.buckminster.core.reader.IStreamConsumer;
import org.eclipse.buckminster.runtime.BuckminsterException;
import org.eclipse.buckminster.runtime.MonitorUtils;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;

/**
 * A IStreamConsumer responsible for reading and parsing a
 * <code>.classpath</code> file.
 * 
 * @author Thomas Hallgren
 */
@SuppressWarnings("restriction")
public class ClasspathReader extends JavaProject implements IStreamConsumer<IClasspathEntry[]> {
	public static IClasspathEntry[] getClasspath(IComponentReader reader, IProgressMonitor monitor) throws CoreException {
		ClasspathReader rdr = new ClasspathReader();
		try {
			return (reader instanceof ICatalogReader) ? ((ICatalogReader) reader).readFile(CLASSPATH_FILENAME, rdr, monitor) : ((IFileReader) reader)
					.readFile(rdr, monitor);
		} catch (IOException e) {
			throw BuckminsterException.wrap(e);
		}
	}

	public ClasspathReader() {
		super(ResourcesPlugin.getWorkspace().getRoot().getProject(" "), JavaModelManager.getJavaModelManager().getJavaModel()); //$NON-NLS-1$
	}

	@Override
	public IClasspathEntry[] consumeStream(IComponentReader reader, String streamName, InputStream stream, IProgressMonitor monitor)
			throws CoreException, IOException {
		monitor.beginTask(null, 150);
		monitor.subTask(Messages.parsing_classpath);
		try {
			ByteArrayOutputStream builder = new ByteArrayOutputStream();
			FileUtils.copyFile(stream, builder, MonitorUtils.subMonitor(monitor, 100));
			return decodeClasspath(new String(builder.toByteArray()), null)[0];
		} finally {
			monitor.done();
		}
	}
}
