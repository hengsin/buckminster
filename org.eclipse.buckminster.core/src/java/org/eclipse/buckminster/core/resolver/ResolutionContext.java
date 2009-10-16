/*******************************************************************************
 * Copyright (c) 2006-2007, Cloudsmith Inc.
 * The code, documentation and other materials contained herein have been
 * licensed under the Eclipse Public License - v 1.0 by the copyright holder
 * listed above, as the Initial Contributor under such license. The text of
 * such license is available at www.eclipse.org.
 ******************************************************************************/

package org.eclipse.buckminster.core.resolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.buckminster.core.CorePlugin;
import org.eclipse.buckminster.core.RMContext;
import org.eclipse.buckminster.core.cspec.IComponentRequest;
import org.eclipse.buckminster.core.cspec.IGenerator;
import org.eclipse.buckminster.core.cspec.model.CSpec;
import org.eclipse.buckminster.core.cspec.model.ComponentName;
import org.eclipse.buckminster.core.cspec.model.ComponentRequest;
import org.eclipse.buckminster.core.helpers.UnmodifiableMapUnion;
import org.eclipse.buckminster.core.metadata.model.GeneratorNode;
import org.eclipse.buckminster.core.mspec.model.MaterializationSpec;
import org.eclipse.buckminster.core.query.IAdvisorNode;
import org.eclipse.buckminster.core.query.model.ComponentQuery;
import org.eclipse.buckminster.runtime.Logger;
import org.eclipse.buckminster.sax.Utils;
import org.eclipse.core.runtime.IStatus;

/**
 * @author Thomas Hallgren
 */
public class ResolutionContext extends RMContext implements IResolverBackchannel
{
	private final ComponentQuery m_componentQuery;

	private HashMap<String, GeneratorNode> m_generators;

	private final ResolutionContext m_parentContext;

	private final HashMap<ComponentRequest, List<ResolverDecision>> m_decisionLog = new HashMap<ComponentRequest, List<ResolverDecision>>();

	public ResolutionContext(ComponentQuery componentQuery)
	{
		this(componentQuery, null);
	}

	public ResolutionContext(ComponentQuery componentQuery, ResolutionContext parentContext)
	{
		super(parentContext == null
				? componentQuery.getGlobalProperties()
				: new UnmodifiableMapUnion<String, Object>(componentQuery.getGlobalProperties(), parentContext));
		m_componentQuery = componentQuery;
		m_parentContext = parentContext;
	}

	public ResolutionContext(MaterializationSpec mspec, ComponentQuery componentQuery)
	{
		super(new UnmodifiableMapUnion<String, Object>(componentQuery.getGlobalProperties(), mspec.getProperties()));
		m_componentQuery = componentQuery;
		m_parentContext = null;
	}

	@Override
	public synchronized void addRequestStatus(IComponentRequest request, IStatus resolveStatus)
	{
		if(m_parentContext != null)
			m_parentContext.addRequestStatus(request, resolveStatus);
		else
			super.addRequestStatus(request, resolveStatus);
	}

	@Override
	public synchronized void clearStatus()
	{
		if(m_parentContext != null)
			m_parentContext.clearStatus();
		else
			super.clearStatus();
	}

	@Override
	public synchronized Map<String, String> getBindingProperties()
	{
		return (m_parentContext != null)
				? m_parentContext.getBindingProperties()
				: super.getBindingProperties();
	}

	@Override
	public ComponentQuery getComponentQuery()
	{
		return m_componentQuery;
	}

	public synchronized List<ResolverDecision> getDecisionLog(IComponentRequest request)
	{
		if(m_parentContext != null)
			return m_parentContext.getDecisionLog(request);
		return Utils.createUnmodifiableList(m_decisionLog.get(request));
	}

	public GeneratorNode getGeneratorNode(String name)
	{
		if(m_generators != null)
		{
			GeneratorNode node = m_generators.get(name);
			if(node != null)
				return node;
		}
		return (m_parentContext == null)
				? null
				: m_parentContext.getGeneratorNode(name);
	}

	@Override
	public Map<String, ? extends Object> getProperties(ComponentName cName)
	{
		IAdvisorNode parentNode = null;
		IAdvisorNode node = null;
		Map<String, ? extends Object> p = super.getProperties(cName);
		if(m_parentContext != null)
			parentNode = m_parentContext.getComponentQuery().getMatchingNode(cName, this);

		node = getComponentQuery().getMatchingNode(cName, this);
		if(parentNode == null && node == null)
			return p;

		if(parentNode != null)
			p = new UnmodifiableMapUnion<String, Object>(parentNode.getProperties(), p);

		if(node != null && node != parentNode)
			p = new UnmodifiableMapUnion<String, Object>(node.getProperties(), p);

		return p;
	}

	@Override
	public synchronized IStatus getStatus()
	{
		return (m_parentContext != null)
				? m_parentContext.getStatus()
				: super.getStatus();
	}

	@Override
	public synchronized Map<UUID, Object> getUserCache()
	{
		return (m_parentContext != null)
				? m_parentContext.getUserCache()
				: super.getUserCache();
	}

	@Override
	public synchronized boolean isContinueOnError()
	{
		return (m_parentContext != null)
				? m_parentContext.isContinueOnError()
				: super.isContinueOnError();
	}

	public synchronized ResolverDecision logDecision(ComponentRequest request, ResolverDecisionType decisionType,
			Object... args)
	{
		if(m_parentContext != null)
			return m_parentContext.logDecision(request, decisionType, args);

		List<ResolverDecision> decisions = m_decisionLog.get(request);
		if(decisions == null)
		{
			decisions = new ArrayList<ResolverDecision>();
			m_decisionLog.put(request, decisions);
		}

		ResolverDecision decision = new ResolverDecision(request, decisionType, args);
		decisions.add(decision);
		Logger logger = CorePlugin.getLogger();
		if(logger.isDebugEnabled())
			logger.debug("%s: %s", request, decision.toString()); //$NON-NLS-1$
		return decision;
	}

	public ResolverDecision logDecision(ResolverDecisionType decisionType, Object... args)
	{
		return logDecision(getComponentQuery().getExpandedRootRequest(this), decisionType, args);
	}

	@Override
	public void setContinueOnError(boolean flag)
	{
		if(m_parentContext != null)
			m_parentContext.setContinueOnError(flag);
		else
			super.setContinueOnError(flag);
	}

	public void setGenerators(CSpec cspec, Collection<? extends IGenerator> generators)
	{
		for(IGenerator generator : generators)
		{
			if(m_generators == null)
				m_generators = new HashMap<String, GeneratorNode>();
			m_generators.put(generator.getGenerates(), new GeneratorNode(cspec, generator));
		}
	}
}
