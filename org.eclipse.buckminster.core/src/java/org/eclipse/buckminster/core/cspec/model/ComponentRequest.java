/*****************************************************************************
 * Copyright (c) 2006-2007, Cloudsmith Inc.
 * The code, documentation and other materials contained herein have been
 * licensed under the Eclipse Public License - v 1.0 by the copyright holder
 * listed above, as the Initial Contributor under such license. The text of
 * such license is available at www.eclipse.org.
 *****************************************************************************/
package org.eclipse.buckminster.core.cspec.model;

import java.util.Map;

import org.eclipse.buckminster.core.KeyConstants;
import org.eclipse.buckminster.core.internal.version.OSGiVersionType;
import org.eclipse.buckminster.core.version.IVersionDesignator;
import org.eclipse.buckminster.core.version.VersionFactory;
import org.eclipse.buckminster.runtime.Trivial;
import org.eclipse.buckminster.sax.Utils;
import org.eclipse.core.runtime.CoreException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * A ComponentRequest is part of a requirement. All referenced components must be available in a
 * workspace for a requirment to be fulfilled. A component can be further qualified using target
 * references in cases when only a part of the component is needed. The ComponentRequest uses a
 * singleton pattern and is optimized for use as key in a Map or Set.
 * @author thhal
 */
public class ComponentRequest extends ComponentName
{
	@SuppressWarnings("hiding")
	public static final String TAG = "component";

	static public final String ATTR_VERSION_DESIGNATOR = "versionDesignator";

	static public final String ATTR_VERSION_TYPE = "versionType";

	public static ComponentRequest fromProperties(Map<String, String> properties) throws CoreException
	{
		IVersionDesignator vd = null;
		String vdStr = properties.get(KeyConstants.VERSION_DESIGNATOR);
		if(vdStr != null)
		{
			String vdType = properties.get(KeyConstants.VERSION_TYPE);
			if(vdType == null)
				vdType = OSGiVersionType.ID;
			vd = VersionFactory.createDesignator(vdType, vdStr);
		}
		return new ComponentRequest(properties.get(KeyConstants.COMPONENT_NAME),
			properties.get(KeyConstants.CATEGORY_NAME), vd);
	}

	private final IVersionDesignator m_versionDesignator;

	public ComponentRequest(String name, String category, IVersionDesignator versionDesignator)
	{
		super(name, category);
		m_versionDesignator = versionDesignator;
	}

	public ComponentRequest(String name, String category, String versionDesignatorStr, String versionTypeId)
	throws CoreException
	{
		super(name, category);
		IVersionDesignator versionDesignator = null;
		if(versionDesignatorStr != null)
			versionDesignator = VersionFactory.createDesignator(versionTypeId, versionDesignatorStr);
		m_versionDesignator = versionDesignator;
	}

	public boolean designates(ComponentIdentifier id)
	{
		return this.getName().equals(id.getName())
			&& (this.getCategory() == null || this.getCategory().equals(id.getCategory()))
			&& (m_versionDesignator == null || m_versionDesignator.designates(id.getVersion()));
	}

	/**
	 * Returns true if this component reference is equal to obj with respect to name,
	 * versionSelector, and match rule.
	 */
	@Override
	public boolean equals(Object o)
	{
		if(o == this)
			return true;

		return super.equals(o)
			&& Trivial.equalsAllowNull(this.m_versionDesignator, ((ComponentRequest)o).m_versionDesignator);
	}

	@Override
	public String getDefaultTag()
	{
		return TAG;
	}

	@Override
	public Map<String, String> getProperties()
	{
		Map<String, String> p = super.getProperties();
		if(m_versionDesignator != null)
		{
			p.put(KeyConstants.VERSION_DESIGNATOR, m_versionDesignator.toString());
			p.put(KeyConstants.VERSION_TYPE, m_versionDesignator.getVersion().getType().getId());
		}
		return p;
	}

	public IVersionDesignator getVersionDesignator()
	{
		return m_versionDesignator;
	}

	public String getViewName()
	{
		String name = this.getName();
		String category = this.getCategory();
		if(category == null)
			return name;

		return name + ':' + category;
	}

	/**
	 * Returns the hashCode for this component request.
	 */
	@Override
	public int hashCode()
	{
		int hash = super.hashCode();
		return 31 * hash + (m_versionDesignator == null ? 0 : m_versionDesignator.hashCode());
	}

	public ComponentRequest mergeDesignator(ComponentRequest that) throws ComponentRequestConflictException
	{
		if(!this.getName().equals(that.getName()))
			throw new ComponentRequestConflictException(this, that);

		String thisCat = this.getCategory();
		String thatCat = that.getCategory();
		if(thisCat == null)
			thisCat = thatCat;
		else if(thatCat != null && !thisCat.equals(thatCat))
			throw new ComponentRequestConflictException(this, that);

		IVersionDesignator thisVD = this.getVersionDesignator();
		IVersionDesignator thatVD = that.getVersionDesignator();
		if(thisVD == null)
			return thatVD == null ? this : that;

		if(thatVD == null)
			return this;

		IVersionDesignator mergedVD = thisVD.merge(thatVD);
		if(mergedVD == thisVD)
			return this;

		if(mergedVD == null)
			throw new ComponentRequestConflictException(this, that);

		return new ComponentRequest(this.getName(), thisCat, mergedVD);
	}
	
	@Override
	public ComponentName toPureComponentName()
	{
		return new ComponentName(this);
	}

	@Override
	public void toString(StringBuilder bld)
	{
		super.toString(bld);
		if(m_versionDesignator != null)
		{
			bld.append('/');
			bld.append(m_versionDesignator);
			bld.append('#');
			bld.append(m_versionDesignator.getVersion().getType().getId());
		}
	}

	@Override
	protected void addAttributes(AttributesImpl attrs)
	{
		super.addAttributes(attrs);
		if(m_versionDesignator != null)
		{
			Utils.addAttribute(attrs, ATTR_VERSION_DESIGNATOR, m_versionDesignator.toString());
			Utils.addAttribute(attrs, ATTR_VERSION_TYPE, m_versionDesignator.getVersion().getType().getId());
		}
	}
}
