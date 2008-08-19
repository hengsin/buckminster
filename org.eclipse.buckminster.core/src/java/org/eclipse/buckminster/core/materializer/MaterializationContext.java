/*******************************************************************************
 * Copyright (c) 2006-2007, Cloudsmith Inc.
 * The code, documentation and other materials contained herein have been
 * licensed under the Eclipse Public License - v 1.0 by the copyright holder
 * listed above, as the Initial Contributor under such license. The text of
 * such license is available at www.eclipse.org.
 ******************************************************************************/

package org.eclipse.buckminster.core.materializer;

import java.util.Map;

import org.eclipse.buckminster.core.RMContext;
import org.eclipse.buckminster.core.common.model.ExpandingProperties;
import org.eclipse.buckminster.core.cspec.IComponentIdentifier;
import org.eclipse.buckminster.core.cspec.IComponentName;
import org.eclipse.buckminster.core.cspec.model.ComponentIdentifier;
import org.eclipse.buckminster.core.cspec.model.ComponentName;
import org.eclipse.buckminster.core.ctype.IComponentType;
import org.eclipse.buckminster.core.helpers.MapUnion;
import org.eclipse.buckminster.core.metadata.model.BOMNode;
import org.eclipse.buckminster.core.metadata.model.BillOfMaterials;
import org.eclipse.buckminster.core.metadata.model.Resolution;
import org.eclipse.buckminster.core.mspec.IMaterializationNode;
import org.eclipse.buckminster.core.mspec.model.MaterializationSpec;
import org.eclipse.buckminster.core.query.model.ComponentQuery;
import org.eclipse.buckminster.core.reader.IReaderType;
import org.eclipse.buckminster.core.version.IVersion;
import org.eclipse.buckminster.runtime.BuckminsterException;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * @author Thomas Hallgren
 */
public class MaterializationContext extends RMContext
{
	private final BillOfMaterials m_bom;
	private final MaterializationSpec m_materializationSpec;
	private final MaterializationStatistics m_statistics = new MaterializationStatistics();
	private boolean m_rebootNeeded = false;

	public MaterializationContext(BillOfMaterials bom, MaterializationSpec mspec)
	{
		super(mspec.getProperties());
		m_bom = bom;
		m_materializationSpec = mspec;
		addTagInfosFromBom();
	}

	public MaterializationContext(BillOfMaterials bom, MaterializationSpec mspec, RMContext context)
	{
		super(new MapUnion<String, String>(mspec.getProperties(), context), context);
		m_bom = bom;
		m_materializationSpec = mspec;
	}

	public BillOfMaterials getBillOfMaterials()
	{
		return m_bom;
	}

	@Override
	public ComponentQuery getComponentQuery()
	{
		return m_bom.getQuery();
	}

	/**
	 * Returns the designated full path to the installed artifact for the resolution. This
	 * is a shortcut for<pre>
	 * getInstallLocation(resolution).append(getLeafArtifact(resolution))
	 * </pre>
	 * @param resolution The resolution for which we want the artifact location
	 * @return An absolute path in the local file system.
	 * @throws CoreException
	 */
	public IPath getArtifactLocation(Resolution resolution) throws CoreException
	{
		IPath installLocation = getInstallLocation(resolution);
		IPath leafArtifact = getLeafArtifact(resolution);
		if(leafArtifact == null)
			installLocation = installLocation.addTrailingSeparator();
		else
			installLocation = installLocation.append(leafArtifact);
		return installLocation;
	}

	/**
	 * Returns the install location for the resolution as specified in the {@link MaterializationSpec}
	 * or the default location if it is not specified.
	 * @param resolution The resolution for which we want the install location
	 * @return An absolute path in the local file system.
	 * @throws CoreException
	 */
	public IPath getInstallLocation(Resolution resolution) throws CoreException
	{
		IPath relativeLocation = getRelativeInstallLocation(resolution);
		if(relativeLocation != null)
		{
			IPath tmp = expand(relativeLocation);
			if(tmp.isAbsolute())
				return tmp;
		}

		IPath location = getRootInstallLocation(resolution);
		if(relativeLocation != null)
			location = location.append(relativeLocation);
		return expand(location);
	}

	public IPath getLeafArtifact(Resolution resolution) throws CoreException
	{
		IComponentIdentifier ci = resolution.getComponentIdentifier();
		MaterializationSpec mspec = getMaterializationSpec();
		IPath leaf = mspec.getLeafArtifact(ci);
		boolean isExpand = mspec.isExpand(ci);

		if(leaf != null)
		{
			// MSpec always take precedence
			//
			if(isExpand)
				leaf = leaf.addTrailingSeparator();
			return leaf;
		}

		IReaderType rd = mspec.getMaterializer(resolution).getMaterializationReaderType(resolution);
		if(isExpand)
			//
			// We only name files, not expanded folders
			//
			return null;

		leaf = rd.getLeafArtifact(resolution, this);
		if(leaf == null)
		{
			// No filename is available, let's use a name built from <componentname>_<version>
			//
			StringBuilder nameBld = new StringBuilder(ci.getName());
			IVersion version = ci.getVersion();
			if(version != null)
			{
				nameBld.append('_');
				version.toString(nameBld);
			}
			nameBld.append(".dat");
			leaf = Path.fromPortableString(nameBld.toString());
			if(leaf.segmentCount() > 1)
				leaf = leaf.removeFirstSegments(leaf.segmentCount() - 1);
		}
		return leaf;
	}

	public MaterializationStatistics getMaterializationStatistics()
	{
		return m_statistics;
	}

	public MaterializationSpec getMaterializationSpec()
	{
		return m_materializationSpec;
	}

	public int getMaxParallelJobs()
	{
		int maxParallelJobs = m_materializationSpec.getMaxParallelJobs();
		if(maxParallelJobs == -1)
			maxParallelJobs = MaterializationJob.getMaxParallelJobs();
		return maxParallelJobs;
	}

	@Override
	public Map<String, String> getProperties(ComponentName cName)
	{
		Map<String,String> p = super.getProperties(cName);
		IMaterializationNode node = m_materializationSpec.getMatchingNode(cName);
		if(node != null)
			p.putAll(node.getProperties());
		return p;
	}

	public IPath getWorkspaceLocation(Resolution resolution) throws CoreException
	{
		IPath nodeLocation = null;
		ComponentIdentifier ci = resolution.getComponentIdentifier();
		IMaterializationNode node = m_materializationSpec.getMatchingNode(ci);
		if(node != null)
		{
			nodeLocation = node.getWorkspaceLocation();
			if(nodeLocation != null)
			{
				nodeLocation = Path.fromOSString(ExpandingProperties.expand(getProperties(ci), nodeLocation.toOSString(), 0));
				IPath tmp = expand(nodeLocation);
				if(tmp.isAbsolute())
					return tmp;
			}
		}

		IPath rootLocation = m_materializationSpec.getWorkspaceLocation();
		if(rootLocation == null)
		{
			if(nodeLocation != null)
				//
				// At this point the nodeLocation must be relative so this
				// is illegal.
				//
				throw BuckminsterException.fromMessage(
					"WorkspaceLocation %s in node matching %s cannot be relative unless a main workspace location is present",
						nodeLocation, ci);

			// Default to location of current workspace
			//
			return ResourcesPlugin.getWorkspace().getRoot().getLocation();
		}

		return expand((nodeLocation == null)
			? rootLocation
			: rootLocation.append(nodeLocation));
	}

	/**
	 * If the target platform materializer installs things into the current
	 * runtime, this flag will be set to <code>true</code>.
	 * 
	 * @return <code>true</code> if a materializer altered the current runtime platform.
	 */
	public boolean isRebootNeeded()
	{
		return m_rebootNeeded;
	}

	public String getSuffixedName(Resolution resolution, String remoteName)
	throws CoreException
	{
		MaterializationSpec mspec = getMaterializationSpec();
		IComponentName cName = resolution.getComponentIdentifier();
		if(!mspec.isUnpack(cName))
			return null;

		String name = mspec.getSuffix(cName);
		if(name == null)
			name = remoteName;

		if(name == null)
		{
			IReaderType rd = resolution.getProvider().getReaderType();
			IPath leaf = rd.getLeafArtifact(resolution, this);
			if(leaf == null || leaf.segmentCount() == 0)
				throw BuckminsterException.fromMessage("Unable to determine suffix for unpack of %s", cName);
			name = leaf.segment(0);
		}
		return name;
	}

	/**
	 * Set by the target platform materializer when it installs new features into the
	 * default target platform (the one currently in use).
	 *
	 * @param flag
	 */
	public void setRebootNeeded(boolean flag)
	{
		m_rebootNeeded = flag;
	}

	private IPath getRelativeInstallLocation(Resolution resolution) throws CoreException
	{
		ComponentIdentifier ci = resolution.getComponentIdentifier();
		IMaterializationNode node = m_materializationSpec.getMatchingNode(ci);
		IPath location = null;
		if(node != null)
		{
			location = node.getInstallLocation();
			if(location != null)
				return location;
		}

		IReaderType rd = m_materializationSpec.getMaterializer(resolution).getMaterializationReaderType(resolution);
		location = rd.getInstallLocation(resolution, this);
		IComponentType cType = resolution.getComponentType();
		if(cType != null)
		{
			IPath ctypeRelative = cType.getRelativeLocation();
			if(ctypeRelative != null)
			{
				if(location == null)
					location = ctypeRelative;
				else
					location = location.append(ctypeRelative);
			}
		}
		return location;
	}

	private IPath getRootInstallLocation(Resolution resolution) throws CoreException
	{
		IPath location = m_materializationSpec.getInstallLocation();
		if(location == null)
			location = m_materializationSpec.getMaterializer(resolution).getDefaultInstallRoot(this, resolution);
		return location;
	}

	private IPath expand(IPath path)
	{
		return Path.fromOSString(ExpandingProperties.expand(this, path.toOSString(), 0));
	}

	private void addTagInfosFromBom()
	{
		addTagInfosFromNode(m_bom.getQuery().getTagInfo(), m_bom);
	}

	private void addTagInfosFromNode(String tagInfo, BOMNode node)
	{
		Resolution res = node.getResolution();
		if(res == null || IReaderType.ECLIPSE_PLATFORM.equals(res.getProvider().getReaderTypeId()))
			return;

		addTagInfo(node.getRequest(), tagInfo);
		String childTagInfo = res.getCSpec().getTagInfo(tagInfo);
		for(BOMNode child : node.getChildren())
			addTagInfosFromNode(childTagInfo, child);
	}
}
