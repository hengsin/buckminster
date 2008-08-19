/*******************************************************************************
 * Copyright (c) 2006-2008, Cloudsmith Inc.
 * The code, documentation and other materials contained herein have been
 * licensed under the Eclipse Public License - v 1.0 by the copyright holder
 * listed above, as the Initial Contributor under such license. The text of
 * such license is available at www.eclipse.org.
 ******************************************************************************/

package org.eclipse.buckminster.core.materializer;

import org.eclipse.buckminster.core.cspec.IComponentIdentifier;
import org.eclipse.buckminster.core.helpers.LocalizedException;
import org.eclipse.core.runtime.IPath;

/**
 * @author Thomas Hallgren
 */
public class FileFolderMismatchException extends LocalizedException
{
	private static final long serialVersionUID = -8203237901604381801L;

	public FileFolderMismatchException(IComponentIdentifier ci, IPath path)
	{
		super("Unable to reuse location %s for component %s. A file was expected but a folder was found or vice versa",
				path, ci);
	}
}
