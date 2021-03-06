/*****************************************************************************
 * Copyright (c) 2006-2013, Cloudsmith Inc.
 * The code, documentation and other materials contained herein have been
 * licensed under the Eclipse Public License - v 1.0 by the copyright holder
 * listed above, as the Initial Contributor under such license. The text of
 * such license is available at www.eclipse.org.
 *****************************************************************************/
package org.eclipse.buckminster.core;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.eclipse.buckminster.core.cspec.model.ComponentIdentifier;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Thomas Hallgren
 */
public interface ITargetPlatform {
	/**
	 * Returns the target system architecture
	 */
	String getArch();

	/**
	 * Returns a list of all components (features, plugins, and fragments) that
	 * are known to the target platform.
	 */
	List<ComponentIdentifier> getComponents() throws CoreException;

	/**
	 * Returns the location of the directory container of the default target
	 * platform. The default target platform is the running instance augmented
	 * with an extra directory that Buckminster uses for p2 provisioning. It
	 * will be created if it does not exist.
	 * 
	 * @param asActive
	 *            Set the default target platform active.
	 * @return The location of the directory container maintained by the default
	 *         target platform.
	 * @throws CoreException
	 *             if something goes wrong when defining the default target
	 *             platform
	 */
	File getDefaultPlatformLocation(boolean asActive) throws CoreException;

	/**
	 * Returns the target platform's main location
	 */
	File getLocation();

	/**
	 * Returns the target locale
	 */
	String getNL();

	/**
	 * Returns the target operating system
	 */
	String getOS();

	/**
	 * Returns the target windowing system.
	 */
	String getWS();

	/**
	 * Perform any refresh actions needed after the content of the given
	 * locations has been altered.
	 * 
	 * @param locations
	 *            A set of files denoting absolute paths in the local file
	 *            system
	 */
	void locationsChanged(Set<File> locations);

	/**
	 * Refresh the active target platform.
	 */
	void refresh();
}
