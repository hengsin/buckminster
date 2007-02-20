/*******************************************************************************
 * Copyright (c) 2004, 2005
 * Thomas Hallgren, Kenneth Olwing, Mitch Sonies
 * Pontus Rydin, Nils Unden, Peer Torngren
 * The code, documentation and other materials contained herein have been
 * licensed under the Eclipse Public License - v 1.0 by the individual
 * copyright holders listed above, as Initial Contributors under such license.
 * The text of such license is available at www.eclipse.org.
 *******************************************************************************/

package org.eclipse.buckminster.core.rmap.parser;

import java.util.ArrayList;

import org.eclipse.buckminster.core.parser.ExtensionAwareHandler;
import org.eclipse.buckminster.core.rmap.model.BidirectionalTransformer;
import org.eclipse.buckminster.core.rmap.model.VersionConverterDesc;
import org.eclipse.buckminster.sax.AbstractHandler;
import org.eclipse.buckminster.sax.ChildHandler;
import org.eclipse.buckminster.sax.ChildPoppedListener;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


/**
 * @author Thomas Hallgren
 */
public class VersionConverterHandler extends ExtensionAwareHandler implements ChildPoppedListener
{
	static final String TAG = VersionConverterDesc.TAG;
	private String m_type;
	private final BidirectionalTransformerHandler m_transformerHandler = new BidirectionalTransformerHandler(this);
	private final ArrayList<BidirectionalTransformer> m_transformers = new ArrayList<BidirectionalTransformer>();

	public VersionConverterHandler(AbstractHandler parent)
	{
		super(parent);
	}

	@Override
	public void handleAttributes(Attributes attrs)
	throws SAXException
	{
		m_type = this.getStringValue(attrs, VersionConverterDesc.ATTR_TYPE);
		m_transformers.clear();
	}

	@Override
	public ChildHandler createHandler(String uri, String localName, Attributes attrs)
	throws SAXException
	{
		ChildHandler ch;
		if(BidirectionalTransformerHandler.TAG.equals(localName))
			ch = m_transformerHandler;
		else
			ch = super.createHandler(uri, localName, attrs);
		return ch;
	}

	public VersionConverterDesc getVersionConverter()
	{
		return new VersionConverterDesc(m_type, m_transformers.toArray(new BidirectionalTransformer[m_transformers.size()]));
	}

	public void childPopped(ChildHandler child) throws SAXException
	{
		if(child instanceof BidirectionalTransformerHandler)
			m_transformers.add(((BidirectionalTransformerHandler)child).getTransformer());
	}
}

