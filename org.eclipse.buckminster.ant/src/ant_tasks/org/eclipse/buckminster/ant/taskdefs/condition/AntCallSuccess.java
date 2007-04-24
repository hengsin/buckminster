/**************************************************************************
 * Copyright (c) 2006-2007, Cloudsmith Inc.
 * The code, documentation and other materials contained herein have been
 * licensed under the Eclipse Public License - v 1.0 by the copyright holder
 * listed above, as the Initial Contributor under such license. The text of
 * such license is available at www.eclipse.org.
 ***************************************************************************/
package org.eclipse.buckminster.ant.taskdefs.condition;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.CallTarget;
import org.apache.tools.ant.taskdefs.condition.Condition;

/**
 * @author Thomas Hallgren
 */
public class AntCallSuccess extends CallTarget implements Condition
{
	public boolean eval() throws BuildException
	{
		Project p = getProject();
		boolean wasKeepGoing = p.isKeepGoingMode();
		p.setKeepGoingMode(true);

		try
		{
			execute();
			return true;
		}
		catch(BuildException e)
		{
			return false;
		}
		finally
		{
			p.setKeepGoingMode(wasKeepGoing);
		}
	}
}
