/*****************************************************************************
 * Copyright (c) 2006-2007, Cloudsmith Inc.
 * The code, documentation and other materials contained herein have been
 * licensed under the Eclipse Public License - v 1.0 by the copyright holder
 * listed above, as the Initial Contributor under such license. The text of
 * such license is available at www.eclipse.org.
 *****************************************************************************/
package org.eclipse.buckminster.core.cspec.builder;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.buckminster.core.CorePlugin;
import org.eclipse.buckminster.core.Messages;
import org.eclipse.buckminster.core.P2Constants;
import org.eclipse.buckminster.core.TargetPlatform;
import org.eclipse.buckminster.core.common.model.Documentation;
import org.eclipse.buckminster.core.cspec.IAttribute;
import org.eclipse.buckminster.core.cspec.ICSpecData;
import org.eclipse.buckminster.core.cspec.IComponentIdentifier;
import org.eclipse.buckminster.core.cspec.IComponentRequest;
import org.eclipse.buckminster.core.cspec.IGenerator;
import org.eclipse.buckminster.core.cspec.model.AttributeAlreadyDefinedException;
import org.eclipse.buckminster.core.cspec.model.CSpec;
import org.eclipse.buckminster.core.cspec.model.ComponentIdentifier;
import org.eclipse.buckminster.core.cspec.model.ComponentRequest;
import org.eclipse.buckminster.core.cspec.model.GeneratorAlreadyDefinedException;
import org.eclipse.buckminster.core.cspec.model.MissingAttributeException;
import org.eclipse.buckminster.core.cspec.model.MissingDependencyException;
import org.eclipse.buckminster.core.ctype.IComponentType;
import org.eclipse.buckminster.core.helpers.FilterUtils;
import org.eclipse.buckminster.osgi.filter.Filter;
import org.eclipse.buckminster.osgi.filter.FilterFactory;
import org.eclipse.buckminster.runtime.BuckminsterException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.internal.p2.metadata.RequiredCapability;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.IRequirement;
import org.eclipse.equinox.p2.metadata.Version;
import org.eclipse.equinox.p2.metadata.VersionRange;
import org.eclipse.equinox.p2.metadata.expression.IMatchExpression;
import org.eclipse.equinox.p2.metadata.query.ExpressionQuery;
import org.eclipse.equinox.p2.query.IQuery;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.InvalidSyntaxException;

/**
 * @author Thomas Hallgren
 */
@SuppressWarnings("restriction")
public class CSpecBuilder implements ICSpecData
{
	private HashMap<String, AttributeBuilder> m_attributes;

	private String m_componentType;

	private HashMap<String, ComponentRequestBuilder> m_dependencies;

	private Documentation m_documentation;

	private HashMap<IComponentIdentifier, GeneratorBuilder> m_generators;

	private String m_name;

	private URL m_projectInfo;

	private String m_shortDesc;

	private Version m_version;

	private Filter m_filter;

	public CSpecBuilder()
	{
	}

	public CSpecBuilder(IMetadataRepository mdr, IInstallableUnit iu) throws CoreException
	{
		String name = iu.getId();
		boolean isFeature = name.endsWith(P2Constants.FEATURE_GROUP);
		if(isFeature)
		{
			name = name.substring(0, name.length() - P2Constants.FEATURE_GROUP.length());
			setComponentTypeID(IComponentType.ECLIPSE_FEATURE);
		}
		else
			setComponentTypeID(IComponentType.OSGI_BUNDLE);

		setName(name);
		setVersion(iu.getVersion());

		org.osgi.framework.Filter filterExpr = iu.getFilter();
		if(filterExpr != null)
		{
			try
			{
				Filter filter = FilterFactory.newInstance(filterExpr.toString());
				filter = FilterUtils.replaceAttributeNames(filter, "osgi", TargetPlatform.TARGET_PREFIX); //$NON-NLS-1$
				setFilter(filter);
			}
			catch(InvalidSyntaxException e)
			{
				throw BuckminsterException.wrap(e);
			}
		}

		boolean hasBogusFragments = isFeature && ("org.eclipse.platform".equals(name) //$NON-NLS-1$
				|| "org.eclipse.equinox.executable".equals(name) //$NON-NLS-1$
		|| "org.eclipse.rcp".equals(name)); //$NON-NLS-1$

		for(IRequirement cap : iu.getRequiredCapabilities())
		{
			// We only bother with direct dependencies to other IU's here
			// since package imports etc. are not yet supported
			//
			IMatchExpression<IInstallableUnit> matches = cap.getMatches();
			String namespace = RequiredCapability.extractNamespace(matches);
			if(namespace == null)
				continue;

			name = RequiredCapability.extractName(matches);
			if(name == null)
				continue;
			if(name.endsWith("_root") || name.contains("_root.")) //$NON-NLS-1$ //$NON-NLS-2$
				// TODO: Handle binary feature contribution.
				continue;

			String ctype;
			if(IInstallableUnit.NAMESPACE_IU_ID.equals(namespace))
			{
				if(name.endsWith(P2Constants.FEATURE_GROUP))
				{
					name = name.substring(0, name.length() - P2Constants.FEATURE_GROUP.length());
					ctype = IComponentType.ECLIPSE_FEATURE;
				}
				else if(isFeature)
					ctype = IComponentType.OSGI_BUNDLE;
				else
					continue;
			}
			else if(IComponentType.OSGI_BUNDLE.equals(namespace))
				ctype = namespace;
			else
				// Package or something else that we don't care about here
				continue;

			filterExpr = cap.getFilter();
			String filterStr = filterExpr == null
					? null
					: filterExpr.toString();
			if(cap.getMin() == 0)
			{
				if(filterStr == null)
					filterStr = ComponentRequest.FILTER_ECLIPSE_P2_OPTIONAL;
				else
				{
					filterStr = "(&" + ComponentRequest.FILTER_ECLIPSE_P2_OPTIONAL + filterStr + ')'; //$NON-NLS-1$
				}
			}
			else if(hasBogusFragments && ctype == IComponentType.OSGI_BUNDLE && filterStr != null)
			{
				// Don't add unless this requirement can be satisfied within the same mdr
				IQuery<IInstallableUnit> query = new ExpressionQuery<IInstallableUnit>(IInstallableUnit.class, matches);
				IQueryResult<IInstallableUnit> result = mdr.query(query, null);
				if(result.isEmpty())
					continue;
			}

			ComponentRequestBuilder crb = new ComponentRequestBuilder();
			crb.setName(name);
			crb.setComponentTypeID(ctype);
			crb.setVersionRange(RequiredCapability.extractRange(matches));

			if(filterStr != null)
			{
				try
				{
					Filter filter = FilterFactory.newInstance(filterStr);
					filter = FilterUtils.replaceAttributeNames(filter, "osgi", TargetPlatform.TARGET_PREFIX); //$NON-NLS-1$
					crb.setFilter(filter);
				}
				catch(InvalidSyntaxException e)
				{
					throw BuckminsterException.wrap(e);
				}
			}
			addDependency(crb);
		}
	}

	public ActionBuilder addAction(String actionName, boolean publ, String actorName, boolean always)
			throws AttributeAlreadyDefinedException
	{
		ActionBuilder bld = createActionBuilder();
		bld.setName(actionName);
		bld.setPublic(publ);
		bld.setActorName(actorName);
		bld.setAlways(always);
		addAttribute(bld);
		return bld;
	}

	public ArtifactBuilder addArtifact(String name, boolean publ, IPath base) throws AttributeAlreadyDefinedException
	{
		ArtifactBuilder bld = createArtifactBuilder();
		bld.setName(name);
		bld.setPublic(publ);
		bld.setBase(base);
		addAttribute(bld);
		return bld;
	}

	public void addAttribute(IAttribute attribute) throws AttributeAlreadyDefinedException
	{
		String name = attribute.getName();
		if(m_attributes == null)
			m_attributes = new HashMap<String, AttributeBuilder>();
		else if(m_attributes.containsKey(name))
			throw new AttributeAlreadyDefinedException(m_name, name);
		m_attributes.put(name, attribute.getAttributeBuilder(this));
	}

	public boolean addDependency(IComponentRequest dependency) throws CoreException
	{
		String name = dependency.getName();
		String depType = dependency.getComponentTypeID();
		ComponentRequestBuilder bld;
		if(dependency instanceof ComponentRequestBuilder)
			bld = (ComponentRequestBuilder)dependency;
		else
		{
			bld = createDependencyBuilder();
			bld.initFrom(dependency);
		}

		ComponentRequestBuilder old = getDependency(name, depType);
		if(old == null)
		{
			if(m_dependencies == null)
				m_dependencies = new HashMap<String, ComponentRequestBuilder>();

			m_dependencies.put(name, bld);
			return true;
		}

		String oldType = old.getComponentTypeID();
		if(oldType != null && depType != null && !oldType.equals(depType))
		{
			// The types of the components differ. Remove the unqualified
			// entry and add the new qualified ones
			//
			m_dependencies.remove(name);
			StringBuilder nameBld = new StringBuilder(name);
			nameBld.append(CSpec.COMPONENT_NAME_TYPE_SEPARATOR);
			int len = nameBld.length();
			nameBld.append(oldType);
			m_dependencies.put(nameBld.toString(), old);
			nameBld.setLength(len);
			nameBld.append(depType);
			m_dependencies.put(nameBld.toString(), bld);
			return true;
		}

		// We cannot determine a difference in component type so the
		// ranges must be mergeable
		//
		VersionRange vd = old.getVersionRange();
		VersionRange nvd = dependency.getVersionRange();
		if(vd == null)
			vd = nvd;
		else
		{
			if(nvd != null)
			{
				VersionRange isect = vd.intersect(nvd);
				if(isect == null)
				{
					// Version ranges were not possible to merge, i.e. no intersection. We
					// log a warning about this and select the higher range.
					//
					CorePlugin.getLogger().warning(
							NLS.bind(Messages.Dependency_0_is_defined_more_then_once_in_component_1, getName(),
									old.getName()));
					if(vd.getMinimum().compareTo(nvd.getMaximum()) < 0)
						vd = nvd;
				}
				else
					vd = isect;
			}
		}

		Filter fl = old.getFilter();
		Filter nfl = dependency.getFilter();
		if(fl == null || nfl == null)
			fl = null;
		else
		{
			if(!fl.equals(nfl))
			{
				try
				{
					fl = FilterFactory.newInstance("(|" + fl + nfl + ')'); //$NON-NLS-1$
				}
				catch(InvalidSyntaxException e)
				{
					throw BuckminsterException.wrap(e);
				}
			}
		}

		if(vd == old.getVersionRange() && fl == old.getFilter())
			return false;

		if(oldType == null && depType != null)
			old.setComponentTypeID(depType);
		old.setVersionRange(vd);
		old.setFilter(fl);
		return false;
	}

	public void addGenerator(IGenerator generator) throws GeneratorAlreadyDefinedException
	{
		IComponentIdentifier ci = generator.getGeneratedIdentifier();
		if(m_generators == null)
			m_generators = new HashMap<IComponentIdentifier, GeneratorBuilder>();
		else if(m_generators.containsKey(ci))
			throw new GeneratorAlreadyDefinedException(m_name, ci);

		GeneratorBuilder bld = createGeneratorBuilder();
		bld.initFrom(generator);
		m_generators.put(ci, bld);
	}

	public GroupBuilder addGroup(String name, boolean publ) throws AttributeAlreadyDefinedException
	{
		GroupBuilder bld = createGroupBuilder();
		bld.setName(name);
		bld.setPublic(publ);
		addAttribute(bld);
		return bld;
	}

	public ActionBuilder addInternalAction(String actionName, boolean publ) throws AttributeAlreadyDefinedException
	{
		return addAction(actionName, publ, null, true);
	}

	public void clear()
	{
		m_name = null;
		m_componentType = null;
		m_version = null;
		m_filter = null;
		m_projectInfo = null;
		m_documentation = null;
		m_shortDesc = null;
		m_dependencies = null;
		m_attributes = null;
		m_generators = null;
	}

	public ActionArtifactBuilder createActionArtifactBuilder()
	{
		return new ActionArtifactBuilder(this);
	}

	public ActionBuilder createActionBuilder()
	{
		return new ActionBuilder(this);
	}

	public ArtifactBuilder createArtifactBuilder()
	{
		return new ArtifactBuilder(this);
	}

	public AttributeBuilder createAttributeBuilder()
	{
		return new AttributeBuilder(this);
	}

	public CSpec createCSpec()
	{
		return new CSpec(this);
	}

	public ComponentRequestBuilder createDependencyBuilder()
	{
		return new ComponentRequestBuilder();
	}

	public GeneratorBuilder createGeneratorBuilder()
	{
		return new GeneratorBuilder(this);
	}

	public GroupBuilder createGroupBuilder()
	{
		return new GroupBuilder(this);
	}

	public void finalWrapUp()
	{
		if(m_attributes != null && m_dependencies != null)
		{
			for(AttributeBuilder attr : m_attributes.values())
			{
				if(attr instanceof GroupBuilder)
					((GroupBuilder)attr).finalWrapUp(m_dependencies);
				else if(attr instanceof ActionBuilder)
					((ActionBuilder)attr).getPrerequisitesBuilder().finalWrapUp(m_dependencies);
			}
		}
	}

	public ActionBuilder getActionBuilder(String name)
	{
		if(m_attributes != null)
		{
			AttributeBuilder attr = m_attributes.get(name);
			if(attr instanceof ActionBuilder)
				return (ActionBuilder)attr;
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapterType)
	{
		if(CSpecBuilder.class.isAssignableFrom(adapterType))
			return this;

		if(CSpec.class.isAssignableFrom(adapterType))
			return createCSpec();

		return Platform.getAdapterManager().getAdapter(this, adapterType);
	}

	public ArtifactBuilder getArtifactBuilder(String name)
	{
		AttributeBuilder attr = m_attributes.get(name);
		return attr instanceof ArtifactBuilder
				? (ArtifactBuilder)attr
				: null;
	}

	public AttributeBuilder getAttribute(String name)
	{
		return m_attributes == null
				? null
				: m_attributes.get(name);
	}

	public Map<String, AttributeBuilder> getAttributes()
	{
		return m_attributes;
	}

	public ComponentIdentifier getComponentIdentifier()
	{
		return new ComponentIdentifier(m_name, m_componentType, m_version);
	}

	public String getComponentTypeID()
	{
		return m_componentType;
	}

	public Collection<ComponentRequestBuilder> getDependencies()
	{
		return m_dependencies == null
				? Collections.<ComponentRequestBuilder> emptyList()
				: m_dependencies.values();
	}

	public ComponentRequestBuilder getDependency(String dependencyName, String componentType)
			throws MissingDependencyException
	{
		ComponentRequestBuilder dependency = null;
		if(m_dependencies != null)
		{
			dependency = m_dependencies.get(dependencyName);
			if(dependency == null && componentType != null)
				dependency = m_dependencies.get(dependencyName + CSpec.COMPONENT_NAME_TYPE_SEPARATOR + componentType);
		}
		return dependency;
	}

	public Map<String, ComponentRequestBuilder> getDependencyMap()
	{
		return m_dependencies;
	}

	public Documentation getDocumentation()
	{
		return m_documentation;
	}

	public Filter getFilter()
	{
		return m_filter;
	}

	public Collection<GeneratorBuilder> getGeneratorList()
	{
		return m_generators == null
				? Collections.<GeneratorBuilder> emptySet()
				: m_generators.values();
	}

	public GroupBuilder getGroup(String name)
	{
		AttributeBuilder attr = m_attributes.get(name);
		return attr instanceof GroupBuilder
				? (GroupBuilder)attr
				: null;
	}

	public String getName()
	{
		return m_name;
	}

	public URL getProjectInfo()
	{
		return m_projectInfo;
	}

	public ActionBuilder getRequiredAction(String name) throws MissingAttributeException
	{
		AttributeBuilder attr = m_attributes.get(name);
		if(attr instanceof ActionBuilder)
			return (ActionBuilder)attr;
		throw new MissingAttributeException(m_name, name);
	}

	public ArtifactBuilder getRequiredArtifact(String name) throws MissingAttributeException
	{
		if(m_attributes != null)
		{
			AttributeBuilder attr = m_attributes.get(name);
			if(attr instanceof ArtifactBuilder)
				return (ArtifactBuilder)attr;
		}
		throw new MissingAttributeException(m_name, name);
	}

	public AttributeBuilder getRequiredAttribute(String name) throws MissingAttributeException
	{
		if(m_attributes != null)
		{
			AttributeBuilder attr = m_attributes.get(name);
			if(attr != null)
				return attr;
		}
		throw new MissingAttributeException(m_name, name);
	}

	public ComponentRequestBuilder getRequiredDependency(String dependencyName, String componentType)
			throws MissingDependencyException
	{
		ComponentRequestBuilder dependency = getDependency(dependencyName, componentType);
		if(dependency == null)
			throw new MissingDependencyException(m_name, dependencyName);
		return dependency;
	}

	public GroupBuilder getRequiredGroup(String name) throws MissingAttributeException
	{
		AttributeBuilder attr = m_attributes.get(name);
		if(attr instanceof GroupBuilder)
			return (GroupBuilder)attr;
		throw new MissingAttributeException(m_name, name);
	}

	public String getShortDesc()
	{
		return m_shortDesc;
	}

	public String getTagInfo(String parentInfo)
	{
		return CSpec.getTagInfo(getComponentIdentifier(), m_projectInfo, parentInfo);
	}

	public Version getVersion()
	{
		return m_version;
	}

	public void initFrom(ICSpecData cspec) throws CoreException
	{
		m_name = cspec.getName();
		m_componentType = cspec.getComponentTypeID();
		m_version = cspec.getVersion();
		m_filter = cspec.getFilter();
		m_projectInfo = cspec.getProjectInfo();
		m_documentation = cspec.getDocumentation();
		m_shortDesc = cspec.getShortDesc();

		Map<String, ? extends IAttribute> attrs = cspec.getAttributes();
		if(attrs.size() > 0)
		{
			m_attributes = new HashMap<String, AttributeBuilder>(attrs.size());
			for(IAttribute attr : attrs.values())
				m_attributes.put(attr.getName(), attr.getAttributeBuilder(this));
		}
		else
			m_attributes = null;

		Collection<? extends IComponentRequest> deps = cspec.getDependencies();
		if(deps.size() > 0)
		{
			m_dependencies = new HashMap<String, ComponentRequestBuilder>(deps.size());
			for(IComponentRequest dep : deps)
				addDependency(dep);
		}
		else
			m_dependencies = null;

		Collection<? extends IGenerator> gens = cspec.getGeneratorList();
		if(gens.size() > 0)
		{
			m_generators = new HashMap<IComponentIdentifier, GeneratorBuilder>(gens.size());
			for(IGenerator gen : gens)
			{
				GeneratorBuilder gb = createGeneratorBuilder();
				gb.initFrom(gen);
				m_generators.put(gen.getGeneratedIdentifier(), gb);
			}
		}
		else
			m_generators = null;
	}

	public void removeAttribute(String name)
	{
		if(m_attributes != null)
			m_attributes.remove(name);
	}

	public void removeDependency(String name)
	{
		if(m_dependencies != null)
			m_dependencies.remove(name);
	}

	public void removeGenerator(String name)
	{
		if(m_generators != null)
			m_generators.remove(name);
	}

	public void setComponentTypeID(String componentType)
	{
		m_componentType = componentType;
	}

	public void setDocumentation(Documentation documentation)
	{
		m_documentation = documentation;
	}

	public void setFilter(Filter filter)
	{
		m_filter = filter;
	}

	public void setName(String name)
	{
		m_name = name;
	}

	public void setProjectInfo(URL projectInfo)
	{
		m_projectInfo = projectInfo;
	}

	public void setShortDesc(String shortDesc)
	{
		m_shortDesc = shortDesc;
	}

	public void setVersion(Version version)
	{
		m_version = version;
	}
}
