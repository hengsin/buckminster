/*****************************************************************************
 * Copyright (c) 2006-2007, Cloudsmith Inc.
 * The code, documentation and other materials contained herein have been
 * licensed under the Eclipse Public License - v 1.0 by the copyright holder
 * listed above, as the Initial Contributor under such license. The text of
 * such license is available at www.eclipse.org.
 *****************************************************************************/

package org.eclipse.buckminster.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.buckminster.core.common.model.ExpandingProperties;
import org.eclipse.buckminster.core.common.model.IProperties;
import org.eclipse.buckminster.core.cspec.QualifiedDependency;
import org.eclipse.buckminster.core.cspec.model.Action;
import org.eclipse.buckminster.core.cspec.model.Attribute;
import org.eclipse.buckminster.core.cspec.model.ComponentName;
import org.eclipse.buckminster.core.cspec.model.ComponentRequest;
import org.eclipse.buckminster.core.metadata.model.Resolution;
import org.eclipse.buckminster.core.query.model.ComponentQuery;
import org.eclipse.buckminster.core.resolver.NodeQuery;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;

/**
 * The <i>Resolution and Materialization</i> context. Maintains information with
 * a lifecycle that lasts throughout the resolution and materialization process.
 * 
 * @author Thomas Hallgren
 */
public abstract class RMContext extends ExpandingProperties
{
	private boolean m_continueOnError;
	private MultiStatus m_status;
	private HashMap<Object,Object> m_userCache;

	public RMContext(Map<String,String> properties)
	{
		super(properties);
	}

	/**
	 * This is where the exceptions that occur during processing will end up if the
	 * {@link #isContinueOnError()} returns <code>true</code>.
	 * 
	 * @param resolveStatus
	 *            A status that indicates an error during processing.
	 */
	public synchronized void addException(IStatus resolveStatus)
	{
		if(m_status == null)
		{
			m_status = new MultiStatus(
				CorePlugin.getID(),
				IStatus.OK,
				resolveStatus instanceof MultiStatus
					? ((MultiStatus)resolveStatus).getChildren()
					: new IStatus[] { resolveStatus },
				"Errors during resolve", null);
		}
		else
			m_status.merge(resolveStatus);
	}

	/**
	 * Clears the status so that next call to {@link #getStatus()}
	 * returns {@link IStatus#OK_STATUS}.
	 */
	public synchronized void clearStatus()
	{
		m_status = null;
	}

	public String getBindingName(Resolution resolution, Map<String,String> props) throws CoreException
	{
		ComponentRequest request = resolution.getRequest();
		String name = null;

		Attribute bindEntryPoint = resolution.getCSpec().getBindEntryPoint();
		if(bindEntryPoint instanceof Action)
		{
			if(props == null)
				props = getProperties(request);
			name = ((Action)bindEntryPoint).getBindingName(props);
		}

		if(name == null)
			name = request.getName();
		return name;
	}

	public abstract ComponentQuery getComponentQuery() throws CoreException;

	public NodeQuery getNodeQuery(ComponentRequest request) throws CoreException
	{
		return getNodeQuery(new QualifiedDependency(request, getComponentQuery().getAttributes(request)));
	}

	public NodeQuery getNodeQuery(QualifiedDependency qualifiedDependency)
	{
		return new NodeQuery(this, qualifiedDependency);
	}

	public Map<String, String> getProperties(ComponentName cName)
	{
		// fill this up as you go..
		//
		IProperties p = new ExpandingProperties(this);
		p.putAll(cName.getProperties());
		return p;
	}

	public NodeQuery getRootNodeQuery() throws CoreException
	{
		return getNodeQuery(getComponentQuery().getRootRequest());
	}

	/**
	 * Returns the status that reflects the outcome of the process.
	 * If the status is
	 * {@link org.eclipse.core.runtime.Status#OK_STATUS OK_STATUS} everything
	 * went OK.
	 * 
	 * @return The status of the process
	 */
	public synchronized IStatus getStatus()
	{
		return m_status == null ? Status.OK_STATUS : m_status;
	}

	public synchronized Map<Object,Object> getUserCache()
	{
		if(m_userCache == null)
			m_userCache = new HashMap<Object, Object>();
		return m_userCache;
	}

	public synchronized boolean isContinueOnError()
	{
		return m_continueOnError;
	}

	public void setContinueOnError(boolean flag)
	{
		m_continueOnError = flag;
	}
}
