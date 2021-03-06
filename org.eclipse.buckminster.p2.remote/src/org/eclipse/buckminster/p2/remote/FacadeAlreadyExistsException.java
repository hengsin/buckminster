/*******************************************************************************
 * Copyright (c) 2006-2008, Cloudsmith Inc.
 * The code, documentation and other materials contained herein have been
 * licensed under the Eclipse Public License - v 1.0 by the copyright holder
 * listed above, as the Initial Contributor under such license. The text of
 * such license is available at www.eclipse.org.
 ******************************************************************************/

package org.eclipse.buckminster.p2.remote;

import java.net.URI;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.internal.provisional.p2.core.ProvisionException;
import org.eclipse.osgi.util.NLS;

/**
 * @author Thomas Hallgren
 */
public class FacadeAlreadyExistsException extends ProvisionException
{
	private static final long serialVersionUID = -7365628974444801501L;

	public FacadeAlreadyExistsException(IStatus status)
	{
		super(status);
	}

	public FacadeAlreadyExistsException(URI serverId, String facadeName)
	{
		super(new Status(IStatus.ERROR, Activator.ID, NLS.bind(Messages.facadeAlreadyExists, facadeName,
			serverId), null));
	}
}
