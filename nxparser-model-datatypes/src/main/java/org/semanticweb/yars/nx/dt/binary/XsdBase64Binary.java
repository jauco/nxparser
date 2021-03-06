package org.semanticweb.yars.nx.dt.binary;

import java.util.regex.Pattern;

import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.dt.Datatype;
import org.semanticweb.yars.nx.dt.DatatypeParseException;
import org.semanticweb.yars.nx.namespace.XSD;
/**
 * Represents the xsd:base64Binary datatype
 * @author aidhog
 *
 */
		
public class XsdBase64Binary extends Datatype<String> {
	public static final Resource DT = XSD.BASE64BINARY;
	private String _h;
	
	// cf. http://www.w3.org/TR/2004/PER-xmlschema-2-20040318/#base64Binary
	public static final String REGEX = "((([A-Za-z0-9+/] ?){4})*(([A-Za-z0-9+/] ?){3}[A-Za-z0-9+/]|([A-Za-z0-9+/] ?){2}[AEIMQUYcgkosw048] ?=|[A-Za-z0-9+/] ?[AQgw] ?= ?=))?";
	
	public XsdBase64Binary(String s) throws DatatypeParseException{
		if (!Pattern.matches(REGEX, s))
			throw new DatatypeParseException("Lexical value does not correspond to regex "+REGEX+".",s,DT,2);
		_h = s;
	}

	public String getValue() {
		return _h;
	}

	public String getCanonicalRepresentation() {
		return _h;
	}
}
