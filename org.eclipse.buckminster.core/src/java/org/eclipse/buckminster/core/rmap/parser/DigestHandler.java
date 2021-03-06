package org.eclipse.buckminster.core.rmap.parser;

import org.eclipse.buckminster.core.common.parser.FormatHandler;
import org.eclipse.buckminster.core.rmap.model.Provider;
import org.eclipse.buckminster.sax.AbstractHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class DigestHandler extends FormatHandler {
	public static final String TAG = Provider.TAG_DIGEST;

	private String algorithm;

	public DigestHandler(AbstractHandler parent) {
		super(parent);
	}

	@Override
	public void handleAttributes(Attributes attrs) throws SAXException {
		super.handleAttributes(attrs);
		algorithm = getStringValue(attrs, Provider.ATTR_ALGORITHM);
	}

	String getAlgorithm() {
		return algorithm;
	}
}
