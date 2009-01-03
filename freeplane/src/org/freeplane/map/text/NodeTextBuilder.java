/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.map.text;

import java.io.IOException;

import org.freeplane.core.io.IAttributeHandler;
import org.freeplane.core.io.IAttributeWriter;
import org.freeplane.core.io.IElementContentHandler;
import org.freeplane.core.io.IElementWriter;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.core.io.IXMLElement;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.io.XMLElement;
import org.freeplane.core.map.NodeBuilder;
import org.freeplane.core.map.NodeModel;
import org.freeplane.core.util.HtmlTools;

public class NodeTextBuilder implements IElementContentHandler, IElementWriter, IAttributeWriter {
	public static final String XML_NODE_TEXT = "TEXT";
	public static final String XML_NODE_XHTML_CONTENT_TAG = "richcontent";
	public static final String XML_NODE_XHTML_TYPE_NODE = "NODE";
	public static final String XML_NODE_XHTML_TYPE_NOTE = "NOTE";
	public static final String XML_NODE_XHTML_TYPE_TAG = "TYPE";
	private boolean isTextNode;

	public Object createElement(final Object parent, final String tag, final IXMLElement attributes) {
		if (attributes == null) {
			return null;
		}
		final Object typeAttribute = attributes.getAttribute(
		    NodeTextBuilder.XML_NODE_XHTML_TYPE_TAG, null);
		if (typeAttribute != null
		        && !NodeTextBuilder.XML_NODE_XHTML_TYPE_NODE.equals(typeAttribute)) {
			return null;
		}
		return parent;
	}

	public void endElement(final Object parent, final String tag, final Object node,
	                       final IXMLElement attributes, final String content) {
		assert tag.equals("richcontent");
		final String xmlText = content;
		((NodeModel) node).setXmlText(xmlText);
	}

	private void registerAttributeHandlers(final ReadManager reader) {
		reader.addAttributeHandler(NodeBuilder.XML_NODE, NodeTextBuilder.XML_NODE_TEXT,
		    new IAttributeHandler() {
			    public void setAttribute(final Object userObject, final String value) {
				    final NodeModel node = ((NodeModel) userObject);
				    node.setText(value);
			    }
		    });
	}

	/**
	 * @param writeManager 
	 */
	public void registerBy(final ReadManager reader, WriteManager writeManager) {
		registerAttributeHandlers(reader);
		reader.addElementHandler("richcontent", this);
		writeManager.addElementWriter(NodeBuilder.XML_NODE, this);
		writeManager.addAttributeWriter(NodeBuilder.XML_NODE, this);
	}

	public void writeContent(ITreeWriter writer, Object element, String tag) throws IOException {
		if (!isTextNode) {
			final XMLElement htmlElement = new XMLElement();
			htmlElement.setName(NodeTextBuilder.XML_NODE_XHTML_CONTENT_TAG);
			htmlElement.setAttribute(NodeTextBuilder.XML_NODE_XHTML_TYPE_TAG,
			    NodeTextBuilder.XML_NODE_XHTML_TYPE_NODE);
			final NodeModel node = (NodeModel) element;
			final String content = node.getXmlText().replace('\0', ' ');
			writer.addElement(content, htmlElement);
		}
    }

	public void writeAttributes(ITreeWriter writer, Object userObject, String tag) {
		NodeModel node = (NodeModel) userObject;
		final String text = node.toString().replace('\0', ' ');
		isTextNode = !HtmlTools.isHtmlNode(text);
		if (isTextNode) {
			writer.addAttribute(NodeTextBuilder.XML_NODE_TEXT, text);
		}
    }
}
