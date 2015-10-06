/*
 *  Copyright 2015 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License. 
 */
package scouter.server;

import java.io.File;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import scouter.lang.ObjectType;
import scouter.lang.counters.CounterEngine;
import scouter.lang.pack.MapPack;
import scouter.server.util.XmlUtil;
import scouter.util.FileUtil;


public class CounterManager {

	private static final String CUSTOM_FILENAME = "counters.site.xml";
	
	private static volatile CounterManager instance;
	CounterEngine engine = new CounterEngine();
	File customFile;
	
	byte[] xmlContent;
	byte[] xmlCustomContent;
	
	
	public static CounterManager getInstance() {
		if (instance == null) {
			synchronized (CounterManager.class) {
				if (instance == null) {
					instance = new CounterManager();
				}
			}
		}
		return instance;
	}
	
	private CounterManager() {
		init();
	}
	
	private void init() {
		readAndParseXml("/scouter/lang/counters/counters.xml");
		customFile = new File(Configure.CONF_DIR + CUSTOM_FILENAME);
		if (customFile.canRead()) {
			xmlCustomContent = FileUtil.readAll(customFile);
			engine.parse(xmlCustomContent);
		}
	}
	
	private void readAndParseXml(String path) {
		InputStream in  = CounterEngine.class.getResourceAsStream(path);
		try {
			xmlContent = FileUtil.readAll(in);
			engine.parse(xmlContent);
		} catch (Exception e) {
			Logger.println("Failed read " + path);
		} finally {
			FileUtil.close(in);
		}
	}
	
	public CounterEngine getCounterEngine() {
		return engine;
	}
	
	public byte[] getXmlContent() {
		return xmlContent;
	}
	
	public byte[] getXmlCustomContent() {
		return xmlCustomContent;
	}
	
	public synchronized boolean addObjectType(MapPack param) {
		String name = param.getText(CounterEngine.ATTR_NAME);
		if (engine.getObjectType(name) != null) {
			return false;
		}
		String displayName = param.getText(CounterEngine.ATTR_DISPLAY);
		String family = param.getText(CounterEngine.ATTR_FAMILY);
		String icon = param.getText(CounterEngine.ATTR_ICON);
		boolean subobject = param.getBoolean(CounterEngine.ATTR_SUBOBJECT);
		ObjectType objType = new ObjectType();
		objType.setName(name);
		objType.setDisplayName(displayName);
		objType.setIcon(icon);
		objType.setFamily(engine.getFamily(family));
		objType.setSubObject(subobject);
		boolean success = appendObjectType(objType);
		if (success) {
			xmlCustomContent = FileUtil.readAll(customFile);
			engine.addObjectType(objType);
		}
		return success;
	}
	
	public synchronized boolean editObjectType(MapPack param) {
		String name = param.getText(CounterEngine.ATTR_NAME);
		String displayName = param.getText(CounterEngine.ATTR_DISPLAY);
		String family = param.getText(CounterEngine.ATTR_FAMILY);
		String icon = param.getText(CounterEngine.ATTR_ICON);
		boolean subobject = param.getBoolean(CounterEngine.ATTR_SUBOBJECT);
		ObjectType objType = new ObjectType();
		objType.setName(name);
		objType.setDisplayName(displayName);
		objType.setIcon(icon);
		objType.setFamily(engine.getFamily(family));
		objType.setSubObject(subobject);
		boolean success = editOrAppendObjectType(objType);
		if (success) {
			xmlCustomContent = FileUtil.readAll(customFile);
			engine.addObjectType(objType);
		}
		return success;
	}
	
	private boolean appendObjectType(ObjectType objType) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = dbf.newDocumentBuilder();
			Document doc = null;
			Element parentElement = null;
			if (customFile.canRead()) {
				doc = builder.parse(customFile);
				doc.getDocumentElement().normalize();
				parentElement = (Element) doc.getElementsByTagName(CounterEngine.TAG_TYPES).item(0);
			} else {
				doc = builder.newDocument();
				parentElement = doc.createElement(CounterEngine.TAG_TYPES);
				doc.appendChild(parentElement);
			}
			Element objElement = doc.createElement(CounterEngine.TAG_OBJECT_TYPE);
			objElement.setAttribute(CounterEngine.ATTR_NAME, objType.getName());
			objElement.setAttribute(CounterEngine.ATTR_DISPLAY, objType.getDisplayName());
			objElement.setAttribute(CounterEngine.ATTR_FAMILY, objType.getFamily().getName());
			objElement.setAttribute(CounterEngine.ATTR_ICON, objType.getIcon());
			objElement.setAttribute(CounterEngine.ATTR_SUBOBJECT, objType.isSubObject() ? "true" : "false");
			parentElement.appendChild(objElement);
			XmlUtil.writeXmlFileWithIndent(doc, customFile, 2);
		} catch (Exception e) {
			Logger.println(e.getMessage());
			return false;
		}
		return true;
	}
	
	private boolean editOrAppendObjectType(ObjectType objType) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = dbf.newDocumentBuilder();
			Document doc = null;
			Element parentElement = null;
			boolean found = false;
			if (customFile.canRead()) {
				doc = builder.parse(customFile);
				doc.getDocumentElement().normalize();
				parentElement = (Element) doc.getElementsByTagName(CounterEngine.TAG_TYPES).item(0);
			} else {
				doc = builder.newDocument();
				parentElement = doc.createElement(CounterEngine.TAG_TYPES);
				doc.appendChild(parentElement);
			}
			NodeList list = doc.getElementsByTagName(CounterEngine.TAG_OBJECT_TYPE);
			if (list == null || list.getLength() < 1) {
				Element objElement = doc.createElement(CounterEngine.TAG_OBJECT_TYPE);
				objElement.setAttribute(CounterEngine.ATTR_NAME, objType.getName());
				objElement.setAttribute(CounterEngine.ATTR_DISPLAY, objType.getDisplayName());
				objElement.setAttribute(CounterEngine.ATTR_FAMILY, objType.getFamily().getName());
				objElement.setAttribute(CounterEngine.ATTR_ICON, objType.getIcon());
				objElement.setAttribute(CounterEngine.ATTR_SUBOBJECT, objType.isSubObject() ? "true" : "false");
				parentElement.appendChild(objElement);
				found = true;
			} else {
				for (int i = 0; i <  list.getLength(); i++) {
					Node node = list.item(i);
					if (node.getNodeType() == Node.ELEMENT_NODE) {
						Element objElement = (Element) node;
						String name = objElement.getAttribute(CounterEngine.ATTR_NAME);
						if (objType.getName().equals(name)) {
							objElement.setAttribute(CounterEngine.ATTR_DISPLAY, objType.getDisplayName());
							objElement.setAttribute(CounterEngine.ATTR_FAMILY, objType.getFamily().getName());
							objElement.setAttribute(CounterEngine.ATTR_ICON, objType.getIcon());
							objElement.setAttribute(CounterEngine.ATTR_SUBOBJECT, objType.isSubObject() ? "true" : "false");
							found = true;
							break;
						}
					}
				}
				if (found == false) {
					Element objElement = doc.createElement(CounterEngine.TAG_OBJECT_TYPE);
					objElement.setAttribute(CounterEngine.ATTR_NAME, objType.getName());
					objElement.setAttribute(CounterEngine.ATTR_DISPLAY, objType.getDisplayName());
					objElement.setAttribute(CounterEngine.ATTR_FAMILY, objType.getFamily().getName());
					objElement.setAttribute(CounterEngine.ATTR_ICON, objType.getIcon());
					objElement.setAttribute(CounterEngine.ATTR_SUBOBJECT, objType.isSubObject() ? "true" : "false");
					parentElement.appendChild(objElement);
					found = true;
				}
			}
			if (found) {
				XmlUtil.writeXmlFileWithIndent(doc, customFile, 2);
				return true;
			}
		} catch (Exception e) {
			Logger.println(e.getMessage());
			return false;
		}
		return false;
	}
	
	public static void main(String[] args) {
		File f = Configure.getInstance().getPropertyFile();
		System.out.println(f.getParent());
	}
}
