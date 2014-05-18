package com.example.rainmessage.helper;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class SinaWeatherXmlParser {
	InputStream inStream;
	Element root;

	public InputStream getInStream() {
		return inStream;
	}

	public void setInStream(InputStream inStream) {
		this.inStream = inStream;
	}

	public Element getRoot() {
		return root;
	}

	public void setRoot(Element root) {
		this.root = root;
	}

	public SinaWeatherXmlParser() {
	}

	public SinaWeatherXmlParser(InputStream inStream) {
		if (inStream != null) {
			this.inStream = inStream;
			DocumentBuilderFactory domfac = DocumentBuilderFactory
					.newInstance();
			try {
				DocumentBuilder domBuilder = domfac.newDocumentBuilder();
				Document doc = domBuilder.parse(inStream);
				root = doc.getDocumentElement();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public SinaWeatherXmlParser(String path) {
		InputStream inStream = null;
		try {
			inStream = new FileInputStream(path);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		if (inStream != null) {
			this.inStream = inStream;
			DocumentBuilderFactory domfac = DocumentBuilderFactory
					.newInstance();
			try {
				DocumentBuilder domBuilder = domfac.newDocumentBuilder();
				Document doc = domBuilder.parse(inStream);
				root = doc.getDocumentElement();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public SinaWeatherXmlParser(URL url) {
		InputStream inStream = null;
		try {
			inStream = url.openStream();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		if (inStream != null) {
			this.inStream = inStream;
			DocumentBuilderFactory domfac = DocumentBuilderFactory
					.newInstance();
			try {
				DocumentBuilder domBuilder = domfac.newDocumentBuilder();
				Document doc = domBuilder.parse(inStream);
				root = doc.getDocumentElement();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * @param nodes
	 * @return �����ڵ���ֵ�Էֺŷָ�
	 */
	public Map<String, String> getValue(String[] nodes) {
		if (inStream == null || root==null) {
			return null;
		}
		Map<String, String> map = new HashMap<String, String>();
		// ��ʼ��ÿ���ڵ��ֵΪnull
		for (int i = 0; i < nodes.length; i++) {
			map.put(nodes[i], null);
		}

		// �����һ�ڵ�
		NodeList topNodes = root.getChildNodes();
		if (topNodes != null) {
			for (int i = 0; i < topNodes.getLength(); i++) {
				Node book = topNodes.item(i);
				if (book.getNodeType() == Node.ELEMENT_NODE) {
					for (int j = 0; j < nodes.length; j++) {
						for (Node node = book.getFirstChild(); node != null; node = node
								.getNextSibling()) {
							if (node.getNodeType() == Node.ELEMENT_NODE) {
								if (node.getNodeName().equals(nodes[j])) {
									//String val=node.getFirstChild().getNodeValue();
									String val = node.getTextContent();
									System.out.println(nodes[j] + ":" + val);
									// ���ԭ���Ѿ���ֵ���Էֺŷָ�
									String temp = map.get(nodes[j]);
									if (temp != null && !temp.equals("")) {
										temp = temp + ";" + val;
									} else {
										temp = val;
									}
									map.put(nodes[j], temp);
								}
							}
						}
					}
				}
			}
		}
		return map;
	}
	
	public static boolean isRain(String mCity) throws NoWeatherResultException {
		String city=mCity;
		mCity=mCity.trim();
		String encodedCity="";
		try {
			 encodedCity=URLEncoder.encode(city, "GB18030");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String link = "http://php.weather.sina.com.cn/xml.php?city="+encodedCity+"&password=DJOYnieT8234jlsK&day=1";
		URL url;
		//String path = "test.xml";
		try {
			String result1 = null;
			String result2 = null;
			for(int i=0;i<10;i++){
			url = new URL(link);
			//System.out.println(url);
			// InputStream inStream= url.openStream();
			// InputStream inStream=new FileInputStream(new File("test.xml"));
			SinaWeatherXmlParser parser = new SinaWeatherXmlParser(url);
			String st1="status1";
			String st2="status2";
			String[] nodes = {st1,st2};
			Map<String, String> map = parser.getValue(nodes);
			result1=map.get(st1);
			result2=map.get(st2);
			Thread.sleep(1000 * 60);
			if(result1!=null||result2!=null){
				break;
			}
			}
			if(result1==null||result2==null){
				throw new NoWeatherResultException();
			}
			if(contains(result1,'雨')||contains(result2,'雨')){
				return true;
			}
				
			
			//System.out.println(result1);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;

	}
	
	private static boolean contains(String whole, char key) {
		if(whole==null) return false;
		char[] ca=whole.toCharArray();
		for(char c:ca){
			if(c==key){
				return true;
			}
		}
		return false;
	}

}
