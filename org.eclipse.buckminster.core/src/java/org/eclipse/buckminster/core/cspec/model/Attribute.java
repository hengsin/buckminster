/*****************************************************************************
 * Copyright (c) 2006-2007, Cloudsmith Inc.
 * The code, documentation and other materials contained herein have been
 * licensed under the Eclipse Public License - v 1.0 by the copyright holder
 * listed above, as the Initial Contributor under such license. The text of
 * such license is available at www.eclipse.org.
 *****************************************************************************/
package org.eclipse.buckminster.core.cspec.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.buckminster.core.KeyConstants;
import org.eclipse.buckminster.core.common.model.Documentation;
import org.eclipse.buckminster.core.common.model.ExpandingProperties;
import org.eclipse.buckminster.core.common.model.SAXEmitter;
import org.eclipse.buckminster.core.cspec.PathGroup;
import org.eclipse.buckminster.core.cspec.builder.AttributeBuilder;
import org.eclipse.buckminster.core.metadata.model.IModelCache;
import org.eclipse.buckminster.core.metadata.model.UUIDKeyed;
import org.eclipse.buckminster.runtime.BuckminsterException;
import org.eclipse.buckminster.sax.ISaxableElement;
import org.eclipse.buckminster.sax.Utils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * @author Thomas Hallgren
 */
public abstract class Attribute extends NamedElement implements Cloneable
{
	public final static String PROPERTY_PREFIX = "buckminster.";
	public final static String INSTALLER_HINT_PREFIX = PROPERTY_PREFIX + "install.";

	public static final String ELEM_INSTALLER_HINTS = "installerHints";

	public static final String PUBLIC_TAG = "public";

	public static final String PRIVATE_TAG = "private";

	private final boolean m_public;

	private final Map<String, String> m_installerHints;
	
	private final Documentation m_documentation;

	private CSpec m_cspec;

	Attribute(AttributeBuilder builder)
	{
		super(builder.getName());
		m_public = builder.isPublic();
		m_installerHints = UUIDKeyed.createUnmodifiableProperties(builder.getInstallerHints());
		m_documentation = builder.getDocumentation();
	}

	Attribute(String name, boolean publ, Map<String, String> installerHints, Documentation documentation)
	{
		super(name);
		m_public = publ;
		m_installerHints = UUIDKeyed.createUnmodifiableProperties(installerHints);
		m_documentation = documentation;
	}

	public void addDynamicProperties(Map<String, String> properties)
	throws CoreException
	{
		String actionOutput;
		CSpec cspec = getCSpec();
		IPath buckminsterTempRoot = Path.fromOSString(
				System.getProperty("java.io.tmpdir")).append("buckminster").append(cspec.getName());

		String outputRoot = properties.get(KeyConstants.ACTION_OUTPUT_ROOT);
		if(outputRoot != null)
		{
			// Output root must be qualified with component name to avoid
			// conflicts
			//
			actionOutput = Path.fromOSString(outputRoot).append(cspec.getName()).toPortableString();
		}
		else
			actionOutput = buckminsterTempRoot.append("build").toPortableString();

		properties.put(KeyConstants.ACTION_OUTPUT, actionOutput);
		properties.put(KeyConstants.ACTION_TEMP, buckminsterTempRoot.append("temp").toPortableString());
		properties.put(KeyConstants.ACTION_HOME, cspec.getComponentLocation().toOSString());
		properties.putAll(cspec.getComponentIdentifier().getProperties());
	}

	/**
	 * Create a copy of this Attribute with the owner set to <code>null</code>.
	 * @return A copy that has no cspec owner assigned.
	 */
	public Attribute copy()
	{
		Attribute copy;
		try
		{
			copy = (Attribute)clone();
		}
		catch(CloneNotSupportedException e)
		{
			throw new RuntimeException(e);
		}
		copy.m_cspec = null;
		return copy;
	}

	public final CSpec getCSpec()
	{
		assert m_cspec != null;
		return m_cspec;
	}

	public void getDeepInstallerHints(IModelCache ctx, Map<String, String> hints, Stack<IAttributeFilter> filters) throws CoreException
	{
		Map<String, String> myHints = getInstallerHints();
		if(myHints.size() > 0)
		{
			StringBuilder bld = new StringBuilder(100);
			bld.append(INSTALLER_HINT_PREFIX);
			int pfLen = INSTALLER_HINT_PREFIX.length();
			for(Map.Entry<String, String> hint : myHints.entrySet())
			{
				bld.setLength(pfLen);
				bld.append(hint.getKey());
				bld.append('.');
				bld.append(getName());
				hints.put(bld.toString(), hint.getValue());
			}
		}

		CSpec cspec = getCSpec();
		for(Prerequisite child : getPrerequisites(filters))
		{
			Attribute refAttr = child.getReferencedAttribute(cspec, ctx);
			if(child.isFilter())
			{
				if(filters == null)
					filters = new Stack<IAttributeFilter>();
				filters.push(child);
				refAttr.getDeepInstallerHints(ctx, hints, filters);
				filters.pop();
			}
			else
				refAttr.getDeepInstallerHints(ctx, hints, filters);
		}
	}

	public String getDefaultTag()
	{
		return isPublic() ? PUBLIC_TAG : PRIVATE_TAG;
	}

	public Documentation getDocumentation()
	{
		return m_documentation;
	}

	public long getFirstModified(IModelCache ctx, int expectedFileCount, int[] fileCount) throws CoreException
	{
		PathGroup[] pqs = getPathGroups(ctx, null);
		int idx = pqs.length;
		if(idx == 0)
			return 0L;

		if(idx > 1 && expectedFileCount > 0)
			//
			// We don't know how to distribute the count
			//
			expectedFileCount = -1;

		long oldest = Long.MAX_VALUE;
		while(--idx >= 0)
		{
			long pgModTime = pqs[idx].getFirstModified(expectedFileCount, fileCount);
			if(pgModTime < oldest)
			{
				oldest = pgModTime;
				if(oldest == 0)
					break;
			}
		}
		return oldest;
	}

	public final Map<String, String> getInstallerHints()
	{
		return m_installerHints;
	}

	public void appendRelativeFiles(IModelCache ctx, Map<String,Long> fileNames) throws CoreException
	{
		PathGroup[] pqs = getPathGroups(ctx, null);
		int idx = pqs.length;
		while(--idx >= 0)
			pqs[idx].appendRelativeFiles(fileNames);
	}

	public long getLastModified(IModelCache ctx, long threshold, int[] fileCount) throws CoreException
	{
		PathGroup[] pqs = getPathGroups(ctx, null);
		int count = 0;
		int idx = pqs.length;
		int[] countBin = new int[1];
		long newest = 0L;
		while(--idx >= 0)
		{
			countBin[0] = 0;
			long pgModTime = pqs[idx].getLastModified(threshold, countBin);
			count += countBin[0];
			if(pgModTime > newest)
			{
				newest = pgModTime;
				if(newest > threshold)
					break;
			}
		}
		fileCount[0] = count;
		return newest;
	}

	public final PathGroup[] getPathGroups(IModelCache ctx, Stack<IAttributeFilter> filters) throws CoreException
	{
		PathGroup[] pga;
		if(filters == null || filters.isEmpty())
		{
			Map<String,PathGroup[]> cache = ctx.getPathGroupsCache();
			String qName = getQualifiedName();
			pga = cache.get(qName);
			if(pga == null)
			{
				ExpandingProperties local = new ExpandingProperties(ctx.getProperties());
				addDynamicProperties(local);
				pga = internalGetPathGroups(ctx, local, filters);
				cache.put(qName, pga);
			}
		}
		else
		{
			// Can't use the cache
			//
			ExpandingProperties local = new ExpandingProperties(ctx.getProperties());
			addDynamicProperties(local);
			pga = internalGetPathGroups(ctx, local, filters);
		}
		return pga;
	}

	public List<Prerequisite> getPrerequisites()
	{
		return getPrerequisites(null);
	}

	public List<Prerequisite> getPrerequisites(Stack<IAttributeFilter> filters)
	{
		// Only targets have artifact group prerequisites
		//
		return Collections.emptyList();
	}

	public String getQualifiedName()
	{
		return getCSpec().getName() + '#' + getName();
	}

	public IPath getUniquePath(IPath root, IModelCache modelCtx) throws CoreException
	{
		IPath uniquePath = null;
		PathGroup[] groups = getPathGroups(modelCtx, null);
		if(groups.length == 1)
		{
			PathGroup group = groups[0];
			IPath[] paths = group.getPaths();
			if(paths.length == 1)
			{
				IPath base = group.getBase();
				if(base == null || !base.isAbsolute())
				{
					if(root == null)
						root = getCSpec().getComponentLocation();
					if(base == null)
						base = root;
					else if(!base.isAbsolute())
						base = root.append(base);
				}
				uniquePath = base.append(paths[0]);
			}
		}
		if(uniquePath == null)
			throw BuckminsterException.fromMessage("Unable to determine a unique product path for " + this);
		return uniquePath;
	}

	public boolean isEnabled(IModelCache ctx)
	{
		return true;
	}

	public boolean isProducedByActions(IModelCache cache) throws CoreException
	{
		return false;
	}

	public boolean isPublic()
	{
		return m_public;
	}

	@Override
	public final String toString()
	{
		StringBuilder bld = new StringBuilder();
		toString(bld);
		return bld.toString();
	}

	public void toString(StringBuilder bld)
	{
		getCSpec().getComponentIdentifier().toString(bld);
		bld.append('#');
		bld.append(getName());
	}

	@Override
	protected void emitElements(ContentHandler handler, String namespace, String prefix) throws SAXException
	{
		if(m_documentation != null)
			m_documentation.toSax(handler, namespace, prefix, m_documentation.getDefaultTag());

		if(!m_installerHints.isEmpty())
		{
			String qName = Utils.makeQualifiedName(prefix, ELEM_INSTALLER_HINTS);
			handler.startElement(namespace, ELEM_INSTALLER_HINTS, qName, ISaxableElement.EMPTY_ATTRIBUTES);
			SAXEmitter.emitProperties(handler, m_installerHints, namespace, prefix, true, false);
			handler.endElement(namespace, ELEM_INSTALLER_HINTS, qName);
		}
	}

	protected abstract PathGroup[] internalGetPathGroups(IModelCache ctx, Map<String, String> local, Stack<IAttributeFilter> filters) throws CoreException;

	/**
	 * It would be wonderful if we could have everything final. Double referernces does however
	 * create a hen and egg problem. This is the hen telling the egg that it is its mother.
	 * @param cspec The owner cspec
	 */
	void setCSPec(CSpec cspec)
	{
		assert m_cspec == null;
		m_cspec = cspec;
	}
}
