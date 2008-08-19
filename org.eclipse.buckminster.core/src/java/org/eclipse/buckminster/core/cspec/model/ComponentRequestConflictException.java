/*****************************************************************************
 * Copyright (c) 2006-2008, Cloudsmith Inc.
 * The code, documentation and other materials contained herein have been
 * licensed under the Eclipse Public License - v 1.0 by the copyright holder
 * listed above, as the Initial Contributor under such license. The text of
 * such license is available at www.eclipse.org.
 *****************************************************************************/
package org.eclipse.buckminster.core.cspec.model;

import org.eclipse.buckminster.core.cspec.IComponentRequest;
import org.eclipse.buckminster.core.helpers.LocalizedException;

/**
 * @author Thomas Hallgren
 */
public class ComponentRequestConflictException extends LocalizedException
{
	private static final long serialVersionUID = -1279777286044718638L;

	public ComponentRequestConflictException(IComponentRequest rq1, IComponentRequest rq2)
	{
		super("Component request %s is inconflict with request %s", rq1, rq2);
	}
}
