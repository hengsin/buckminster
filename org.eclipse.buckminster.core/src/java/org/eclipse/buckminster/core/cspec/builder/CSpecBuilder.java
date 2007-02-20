/*****************************************************************************
 * Copyright (c) 2006-2007, Cloudsmith Inc.
 * The code, documentation and other materials contained herein have been
 * licensed under the Eclipse Public License - v 1.0 by the copyright holder
 * listed above, as the Initial Contributor under such license. The text of
 * such license is available at www.eclipse.org.
 *****************************************************************************/
package org.eclipse.buckminster.core.cspec.builder;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.buckminster.core.common.model.Documentation;
import org.eclipse.buckminster.core.cspec.model.Action;
import org.eclipse.buckminster.core.cspec.model.ActionArtifact;
import org.eclipse.buckminster.core.cspec.model.Artifact;
import org.eclipse.buckminster.core.cspec.model.Attribute;
import org.eclipse.buckminster.core.cspec.model.AttributeAlreadyDefinedException;
import org.eclipse.buckminster.core.cspec.model.CSpec;
import org.eclipse.buckminster.core.cspec.model.ComponentRequest;
import org.eclipse.buckminster.core.cspec.model.DependencyAlreadyDefinedException;
import org.eclipse.buckminster.core.cspec.model.Generator;
import org.eclipse.buckminster.core.cspec.model.GeneratorAlreadyDefinedException;
import org.eclipse.buckminster.core.cspec.model.MissingAttributeException;
import org.eclipse.buckminster.core.cspec.model.MissingDependencyException;
import org.eclipse.buckminster.core.version.IVersion;
import org.eclipse.buckminster.core.version.VersionFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * @author Thomas Hallgren
 */
public class CSpecBuilder
{
	private HashMap<String,AttributeBuilder> m_attributes;
	private String m_category;
	private HashMap<String,DependencyBuilder> m_dependencies;
	private Documentation m_documentation;
	private HashMap<String,GeneratorBuilder> m_generators;
	private String m_name;
	private String m_shortDesc;
	private IVersion m_version;

	public ActionBuilder addAction(String actionName, boolean publ, String actorName, boolean always) throws AttributeAlreadyDefinedException
	{
		ActionBuilder bld = this.createActionBuilder();
		bld.setName(actionName);
		bld.setPublic(publ);
		bld.setActorName(actorName);
		bld.setAlways(always);
		this.addAttribute(bld);
		return bld;
	}

	public ArtifactBuilder addArtifact(String name, boolean publ, String type, IPath base) throws AttributeAlreadyDefinedException
	{
		ArtifactBuilder bld = this.createArtifactBuilder();
		bld.setName(name);
		bld.setPublic(publ);
		bld.setType(type);
		bld.setBase(base);
		this.addAttribute(bld);
		return bld;
	}

	public void addAttribute(AttributeBuilder attribute) throws AttributeAlreadyDefinedException
	{
		String name = attribute.getName();
		if(m_attributes == null)
			m_attributes = new HashMap<String, AttributeBuilder>();
		else
		if(m_attributes.containsKey(name))
			throw new AttributeAlreadyDefinedException(m_name, name);
		m_attributes.put(name, attribute);
	}

	public void addDependency(ComponentRequest request) throws DependencyAlreadyDefinedException
	{
		DependencyBuilder bld = this.createDependencyBuilder();
		bld.initFrom(request);
		this.addDependency(bld);
	}

	public void addDependency(DependencyBuilder dependency) throws DependencyAlreadyDefinedException
	{
		String name = dependency.getName();
		if(m_dependencies == null)
			m_dependencies = new HashMap<String,DependencyBuilder>();
		else
		if(m_dependencies.containsKey(name))
			throw new DependencyAlreadyDefinedException(m_name, name);
		m_dependencies.put(name, dependency);
	}

	public void addGenerator(GeneratorBuilder generator) throws GeneratorAlreadyDefinedException
	{
		String name = generator.getName();
		if(m_generators == null)
			m_generators = new HashMap<String,GeneratorBuilder>();
		else
		if(m_generators.containsKey(name))
			throw new GeneratorAlreadyDefinedException(m_name, name);
		m_generators.put(name, generator);
	}

	public GroupBuilder addGroup(String name, boolean publ) throws AttributeAlreadyDefinedException
	{
		GroupBuilder bld = this.createGroupBuilder();
		bld.setName(name);
		bld.setPublic(publ);
		this.addAttribute(bld);
		return bld;
	}

	public ActionBuilder addInternalAction(String actionName, boolean publ) throws AttributeAlreadyDefinedException
	{
		return this.addAction(actionName, publ, null, true);
	}

	public void clear()
	{
		m_name = null;
		m_category = null;
		m_version = null;
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

	public CSpec createCSpec() throws CoreException
	{
		return new CSpec(m_name, m_category, m_version, m_documentation, m_shortDesc, m_dependencies, m_generators, m_attributes);
	}

	public DependencyBuilder createDependencyBuilder()
	{
		return new DependencyBuilder(this);
	}

	public GeneratorBuilder createGeneratorBuilder()
	{
		return new GeneratorBuilder(this);
	}

	public GroupBuilder createGroupBuilder()
	{
		return new GroupBuilder(this);
	}

	public ActionBuilder getAction(String name)
	{
		AttributeBuilder attr = getAttribute(name);
		return attr instanceof ActionBuilder ? (ActionBuilder)attr : null;
	}

	public ArtifactBuilder getArtifact(String name)
	{
		AttributeBuilder attr = getAttribute(name);
		return attr instanceof ArtifactBuilder ? (ArtifactBuilder)attr : null;
	}

	public AttributeBuilder getAttribute(String name)
	{
		return m_attributes == null ? null : m_attributes.get(name);
	}

	public String getCategory()
	{
		return m_category;
	}

	public Map<String,DependencyBuilder> getDependencies()
	{
		return m_dependencies;
	}

	public DependencyBuilder getDependency(String dependencyName)
	{
		return m_dependencies == null ? null : m_dependencies.get(dependencyName);
	}

	public Documentation getDocumentation()
	{
		return m_documentation;
	}

	public GroupBuilder getGroup(String name)
	{
		AttributeBuilder attr = getAttribute(name);
		return attr instanceof GroupBuilder ? (GroupBuilder)attr : null;
	}

	public String getName()
	{
		return m_name;
	}

	public ActionBuilder getRequiredAction(String name) throws MissingAttributeException
	{
		AttributeBuilder attr = getAttribute(name);
		if(attr instanceof ActionBuilder)
			return (ActionBuilder)attr;
		throw new MissingAttributeException(m_name, name);
	}

	public ArtifactBuilder getRequiredArtifact(String name) throws MissingAttributeException
	{
		AttributeBuilder attr = getAttribute(name);
		if(attr instanceof ArtifactBuilder)
			return (ArtifactBuilder)attr;
		throw new MissingAttributeException(m_name, name);
	}

	public AttributeBuilder getRequiredAttribute(String name) throws MissingAttributeException
	{
		AttributeBuilder attr = getAttribute(name);
		if(attr == null)
			throw new MissingAttributeException(m_name, name);
		return attr;
	}

	public DependencyBuilder getRequiredDependency(String name) throws MissingDependencyException
	{
		DependencyBuilder dep = getDependency(name);
		if(dep == null)
			throw new MissingDependencyException(m_name, name);
		return dep;
	}

	public GroupBuilder getRequiredGroup(String name) throws MissingAttributeException
	{
		AttributeBuilder attr = getAttribute(name);
		if(attr instanceof GroupBuilder)
			return (GroupBuilder)attr;
		throw new MissingAttributeException(m_name, name);
	}

	public String getShortDesc()
	{
		return m_shortDesc;
	}

	public IVersion getVersion()
	{
		return m_version;
	}

	public void initFrom(CSpec cspec)
	{
		m_name = cspec.getName();
		m_category = cspec.getCategory();
		m_version = cspec.getVersion();
		m_documentation = cspec.getDocumentation();
		m_shortDesc = cspec.getShortDesc();

		Map<String,Attribute> attrs = cspec.getAttributes();
		if(attrs.size() > 0)
		{
			m_attributes = new HashMap<String, AttributeBuilder>(attrs.size());
			for(Attribute attr : attrs.values())
			{
				AttributeBuilder ab;
				if(attr instanceof ActionArtifact)
					ab = this.createActionArtifactBuilder();
				else if(attr instanceof Artifact)
					ab = this.createArtifactBuilder();
				else if(attr instanceof Action)
					ab = this.createActionBuilder();
				else
					ab = this.createGroupBuilder();
				ab.initFrom(attr);
				m_attributes.put(attr.getName(), ab);
			}
		}
		else
			m_attributes = null;

		Map<String,ComponentRequest> deps = cspec.getDependencies();
		if(deps.size() > 0)
		{
			m_dependencies = new HashMap<String, DependencyBuilder>(deps.size());
			for(ComponentRequest dep : deps.values())
			{
				DependencyBuilder db = this.createDependencyBuilder();
				db.initFrom(dep);
				m_dependencies.put(dep.getName(), db);
			}
		}
		else
			m_dependencies = null;

		Map<String,Generator> gens = cspec.getGenerators();
		if(gens.size() > 0)
		{
			m_generators = new HashMap<String, GeneratorBuilder>(gens.size());
			for(Generator gen : gens.values())
			{
				GeneratorBuilder gb = createGeneratorBuilder();
				gb.initFrom(gen);
				m_generators.put(gen.getName(), gb);
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

	public void setCategory(String category)
	{
		m_category = category;
	}

	public void setDocumentation(Documentation documentation)
	{
		m_documentation = documentation;
	}

	public void setName(String name)
	{
		m_name = name;
	}

	public void setShortDesc(String shortDesc)
	{
		m_shortDesc = shortDesc;
	}

	public void setVersion(IVersion version)
	{
		m_version = version;
	}

	public void setVersion(String versionString, String versionTypeId) throws CoreException
	{
		if(versionString == null)
		{
			m_version = null;
			return;
		}

		versionString = versionString.trim();
		if(versionString.length() == 0)
		{
			m_version = null;
			return;
		}

		if(versionTypeId != null)
		{
			versionTypeId = versionTypeId.trim();
			if(versionTypeId.length() == 0)
				versionTypeId = null;
		}
		m_version = VersionFactory.createVersion(versionTypeId, versionString);
	}
}

