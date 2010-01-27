package org.varnerlab.universaleditor.gui;

import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;

public class SBMLNamespaceContext implements NamespaceContext {

	public String getNamespaceURI(String prefix) {
		String strNamespace=null;
		
		if (prefix.equalsIgnoreCase("sbml"))
		{
			strNamespace="http://www.sbml.org/sbml/level2";
		}
		else if (prefix.equalsIgnoreCase("html"))
		{
			strNamespace="http://www.w3.org/1999/xhtml";
		}
		
		return(strNamespace);
	}

	public String getPrefix(String namespaceURI) {
		// TODO Auto-generated method stub
		return null;
	}

	public Iterator getPrefixes(String namespaceURI) {
		// TODO Auto-generated method stub
		return null;
	}

}
