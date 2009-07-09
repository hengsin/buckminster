/*****************************************************************************
 * Copyright (c) 2006-2007, Cloudsmith Inc.
 * The code, documentation and other materials contained herein have been
 * licensed under the Eclipse Public License - v 1.0 by the copyright holder
 * listed above, as the Initial Contributor under such license. The text of
 * such license is available at www.eclipse.org.
 *****************************************************************************/
package org.eclipse.buckminster.core.cspec.builder;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.buckminster.core.cspec.IArtifact;
import org.eclipse.buckminster.core.cspec.IAttribute;
import org.eclipse.buckminster.core.cspec.model.Artifact;
import org.eclipse.buckminster.core.cspec.model.PathAlreadyDefinedException;
import org.eclipse.core.runtime.IPath;

/**
 * @author Thomas Hallgren
 */
public class ArtifactBuilder extends TopLevelAttributeBuilder implements IArtifact
{
	private IPath m_base;

	private final HashSet<IPath> m_paths = new HashSet<IPath>();

	ArtifactBuilder(CSpecBuilder cspecBuilder)
	{
		super(cspecBuilder);
	}

	public void addPath(IPath path) throws PathAlreadyDefinedException
	{
		if(m_paths.contains(path))
			throw new PathAlreadyDefinedException(getCSpecName(), getName(), path);
		m_paths.add(path);
	}

	@Override
	public void clear()
	{
		super.clear();
		m_base = null;
		m_paths.clear();
	}

	@Override
	public Artifact createAttribute()
	{
		return new Artifact(this);
	}

	@Override
	public AttributeBuilder getAttributeBuilder(CSpecBuilder specBuilder)
	{
		return specBuilder == getCSpecBuilder()
				? this
				: new ArtifactBuilder(specBuilder);
	}

	public IPath getBase()
	{
		return m_base;
	}

	public Set<IPath> getPaths()
	{
		return m_paths;
	}

	/**
	 * @deprecated no longer used
	 */
	@Deprecated
	public String getType()
	{
		return null;
	}

	@Override
	public void initFrom(IAttribute attribute)
	{
		super.initFrom(attribute);
		IArtifact artifact = (IArtifact)attribute;
		m_base = artifact.getBase();
		m_paths.addAll(artifact.getPaths());
	}

	public void removePath(IPath path) throws MissingPathException
	{
		if(!m_paths.contains(path))
			throw new MissingPathException(getCSpecName(), getName(), path);
		m_paths.remove(path);
	}

	public void setBase(IPath base)
	{
		m_base = (base == null)
				? null
				: base.addTrailingSeparator();
	}

	/**
	 * @deprecated type is no longer used
	 */
	@Deprecated
	public void setType(String type)
	{
	}
}
