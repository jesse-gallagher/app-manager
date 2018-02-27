package com.raidomatic.xml;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

import com.ibm.commons.xml.DOMUtil;
import com.ibm.commons.xml.Format;

public class XMLDocument extends XMLNode {
	private static final long serialVersionUID = -8106159267601656260L;

	public XMLDocument() { }
	public XMLDocument(Node node) { super(node); }
	public XMLDocument(String xml) throws Exception {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

		this.node = builder.parse(xml);
	}

	public void loadURL(String urlString) throws Exception {
		URL url = new URL(urlString);
		URLConnection conn = url.openConnection();

		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

		this.node = builder.parse((InputStream)conn.getContent());
	}

	public void loadInputStream(InputStream is) throws Exception {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

		this.node = builder.parse(is);
	}

	public void loadString(String s) throws Exception {
		loadInputStream(new ByteArrayInputStream(s.getBytes()));
	}


	@Override
	public String getXml() throws IOException {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DOMUtil.serialize(bos, ((Document)this.node).getDocumentElement(), Format.defaultFormat);
			return new String(bos.toByteArray(), "UTF-8");
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}