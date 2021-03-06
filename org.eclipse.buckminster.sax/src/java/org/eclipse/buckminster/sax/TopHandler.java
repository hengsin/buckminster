/*****************************************************************************
 * Copyright (c) 2006-2013, Cloudsmith Inc.
 * The code, documentation and other materials contained herein have been
 * licensed under the Eclipse Public License - v 1.0 by the copyright holder
 * listed above, as the Initial Contributor under such license. The text of
 * such license is available at www.eclipse.org.
 *****************************************************************************/
package org.eclipse.buckminster.sax;

import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * Instances of this class represents the top handler, i.e. the handler that
 * deals with the XML document itself. This handler will receive the first call
 * to {@link #startElement(String, String, String, Attributes)}and then typicall
 * push a {@link ChildHandler}for that root element.
 * 
 * @author Thomas Hallgren
 */
public abstract class TopHandler extends AbstractHandler {
	/**
	 * The SAXParserWrapper class is useful for parser implementations that want
	 * to expose themselves as a {@link ContentHandler}. The ContentHandler
	 * interface of the <code>TopHandler</code> is already taken and deciated to
	 * a handler that handles the top element only. An instance of
	 * <code>SAXParserWrapper</code> will dispatch all events to the current
	 * <code>ContentHandler</code> of the internal {@link XMLReader} and thus
	 * handle the full document.
	 * 
	 * @author thhal
	 */
	protected class SAXParserWrapper implements ContentHandler {
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			reader.getContentHandler().characters(ch, start, length);
		}

		@Override
		public void endDocument() throws SAXException {
			reader.getContentHandler().endDocument();
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			reader.getContentHandler().endElement(uri, localName, qName);
		}

		@Override
		public void endPrefixMapping(String prefix) throws SAXException {
			reader.getContentHandler().endPrefixMapping(prefix);
		}

		@Override
		public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
			reader.getContentHandler().ignorableWhitespace(ch, start, length);
		}

		@Override
		public void processingInstruction(String target, String data) throws SAXException {
			reader.getContentHandler().processingInstruction(target, data);
		}

		@Override
		public void setDocumentLocator(Locator locator) {
			reader.getContentHandler().setDocumentLocator(locator);
		}

		@Override
		public void skippedEntity(String name) throws SAXException {
			reader.getContentHandler().skippedEntity(name);
		}

		@Override
		public void startDocument() throws SAXException {
			reader.getContentHandler().startDocument();
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
			reader.getContentHandler().startElement(uri, localName, qName, atts);
		}

		@Override
		public void startPrefixMapping(String prefix, String uri) throws SAXException {
			reader.getContentHandler().startPrefixMapping(prefix, uri);
		}
	}

	private Locator documentLocator;

	private XMLReader reader;

	private final Stack<ContentHandler> handlerStack = new Stack<ContentHandler>();

	/**
	 * Create a <code>TopHandler</code> and assing a <code>XMLReader</code>
	 * parent. The created <code>TopHandler</code> will become the content
	 * handler of the parent.
	 * 
	 * @param parent
	 *            The XMLReader (the actual parser most likely).
	 */
	protected TopHandler(XMLReader parent) {
		reader = parent;
		parent.setContentHandler(this);
	}

	/**
	 * Pop the current handler.
	 * 
	 * @param uri
	 *            The Namespace URI, ignored.
	 * @param localName
	 *            The local name, ignored.
	 * @param qName
	 *            The qualified name, ignored.
	 */
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		this.popHandler();
	}

	/**
	 * Obtains the current handler from the reader and returns it.
	 * 
	 * @return The handler that is current
	 */
	public final AbstractHandler getCurrentHandler() {
		return (AbstractHandler) reader.getContentHandler();
	}

	/**
	 * Returns the encoding of the current document. This method must only be
	 * after the call to {@link #startDocument()} has completed and not after
	 * {@link #endDocument()} has completed.
	 * 
	 * @return The document encoding for current document.
	 */
	public String getEncoding() {
		return Utils.getEncoding(documentLocator);
	}

	/**
	 * Returns the string &quot;root&quot;.
	 */
	@Override
	public String getTAG() {
		return "root"; //$NON-NLS-1$
	}

	@Override
	public final TopHandler getTopHandler() {
		return this;
	}

	/**
	 * Pop the filter that was last pushed.
	 */
	public void popFilter() {
		if (reader instanceof XMLFilterImpl) {
			XMLFilterImpl filter = (XMLFilterImpl) reader;
			reader = filter.getParent();
			reader.setContentHandler(filter.getContentHandler());
			filter.setParent(null);
			filter.setContentHandler(null);
		}
	}

	/**
	 * Push a filter that will sit between the XMLReader and this handler.
	 * 
	 * @param filter
	 *            The filter.
	 */
	public void pushFilter(XMLFilterImpl filter) {
		filter.setContentHandler(reader.getContentHandler());
		reader.setContentHandler(filter);
		filter.setParent(reader);
		reader = filter;
	}

	/**
	 * Called from parsers that support the <code>Locator</code> interface.
	 */
	@Override
	public final void setDocumentLocator(Locator locator) {
		documentLocator = locator;
	}

	/**
	 * Instruct this parser to use a specific EntityResolver.
	 */
	public final void setEntityResolver(EntityResolver resolver) {
		reader.setEntityResolver(resolver);
	}

	/**
	 * Instruct this parser to use a specific ErrorHandler.
	 */
	public final void setErrorHandler(ErrorHandler errorHandler) {
		reader.setErrorHandler(errorHandler);
	}

	/**
	 * Instruct this parser to use namespaces.
	 */
	public final void setNamespaceAware(boolean flag) {
		try {
			reader.setFeature("http://xml.org/sax/features/namespaces", flag); //$NON-NLS-1$
		} catch (SAXException e) {
			// All XMLReaders are required to support the namespace
			// feature, so this should be regarded as fatal.
			//
			throw new RuntimeException(e);
		}
	}

	/**
	 * This method must be implemented by a subclass. The expected behavior is
	 * to push a {@link ChildHandler}that maps to the root element of the XML
	 * document.
	 * 
	 * @param uri
	 *            The Namespace URI, ignored.
	 * @param localName
	 *            The local name, ignored.
	 * @param qName
	 *            The qualified name, passed on to the thrown exception.
	 * @param attributes
	 *            The attributes attached to the element, ignored
	 * @throws UnrecognizedElementException
	 *             unless overridden.
	 */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		throw new UnrecognizedElementException("root", qName, this.getDocumentLocator()); //$NON-NLS-1$
	}

	/**
	 * Returns the document <code>Locator</code> currently associated with this
	 * handler, or <code>null</code> if no locator has been set.
	 * 
	 * @return The document <code>Locator</code> or <code>null</code>.
	 */
	@Override
	protected final Locator getDocumentLocator() {
		return documentLocator;
	}

	/**
	 * Returns the XMLReader currently associated with this instance. This is
	 * either the reader that was passed to the constructor or a filter that was
	 * added using {@link #pushFilter(XMLFilterImpl)}.
	 * 
	 * @return the XMLReader currently associated with this instance.
	 */
	protected final XMLReader getXMLReader() {
		return reader;
	}

	/**
	 * Pop the last pushed handler form the stack and make it the
	 * <code>ContentHandler</code> of the current <code>XMLReader</code>.
	 */
	protected final void popHandler() {
		setHandler(handlerStack.pop());
	}

	/**
	 * Calls the {@link #handleAttributes(Attributes)}method of the supplied
	 * handler. The handler is then made the <code>ContentHandler</code> of the
	 * current <code>XMLReader</code> and the previous handler is stacked.
	 * 
	 * @param handler
	 *            The new handler
	 * @param attrs
	 *            Attributes to pass to the new handler.
	 * @throws SAXException
	 */
	protected final void pushHandler(ChildHandler handler, Attributes attrs) throws SAXException {
		handlerStack.push(reader.getContentHandler());
		setHandler(handler);
		handler.handleAttributes(attrs);
	}

	private void setHandler(ContentHandler handler) {
		reader.setContentHandler(handler);
		if (handler instanceof LexicalHandler) {
			try {
				reader.setProperty("http://xml.org/sax/properties/lexical-handler", handler); //$NON-NLS-1$
			} catch (SAXException e) {
				// Ignore. Not all parsers can cope with lexical handlers.
			}
		}
	}
}
