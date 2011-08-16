// (c) 2004 Andreas Harth

package org.semanticweb.yars.nx;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

import org.semanticweb.yars.nx.util.NxUtil;

/**
 * An RDF resource (http://...).
 * 
 * @author Andreas Harth
 * @author Tobias Kaefer
 */
public class Resource implements Node, Serializable {
	// the value of the resource (now includes < >)
	protected String _data;

	// called a lot, so instead will save value
	// not serialised mainly for backwards compatibility
	// EDIT: not needed, will be cached in _data string!!!
	// protected transient int _hashcode = 0;

	// version number for serialisation
	public static final long serialVersionUID = 1l;

	/**
	 * Constructor.
	 */
	public Resource() {
		_data = null;
	}

	/**
	 * Constructor. Assumes no angle brackets around the uri like in Nx.
	 */
	public Resource(String uri) {
		this(uri, false);
	}

	/**
	 * Constructor.
	 * 
	 * @param isN3
	 *            Set this to true if angle brackets are already around URI as
	 *            required by N3.
	 */
	public Resource(String uri, boolean isN3) {
		if (!isN3) {
			if (uri.charAt(0) != '<')
				_data = "<" + uri + ">";
			else
				_data = uri;
		} else
			_data = uri;
	}

	public String getHost() throws URISyntaxException {
		URI u = new URI(toString());
		return u.getHost();
	}

	/**
	 * Needed for storing in hashtables.
	 */
	public int hashCode() {
		return toN3().hashCode();
	}

	/**
	 * Get string representation.
	 * 
	 */
	public String toString() {
		// not called so often, now a bit slower
		return NxUtil.unescape(toN3().substring(1, toN3().length() - 1));
	}

	/**
	 * Get URI in N3 notation.
	 * 
	 */
	public String toN3() {
		// called a LOT, so now 10x faster
		return _data;
	}

	/**
	 * Compare.
	 * 
	 */
	public int compareTo(Object o) {
		if (o == this)
			return 0;

		if (o instanceof Resource) {
			Resource r = (Resource) o;
			return toN3().compareTo(r.toN3());
		} else if (o instanceof Literal) {
			return Integer.MAX_VALUE;
		} else if (o instanceof BNode) {
			return Integer.MIN_VALUE / 3;
		} else if (o instanceof Unbound) {
			return Integer.MIN_VALUE / 2;
		} else if (o instanceof Variable) {
			return Integer.MIN_VALUE;
		}

		throw new ClassCastException("parameter is not of type Resource but "
				+ o.getClass().getName());
	}

	/**
	 * Equality check.
	 * 
	 */
	public boolean equals(Object o) {
		if (o == this)
			return true;
		else if (o instanceof Resource) {
			Resource r = (Resource) o;
			return (r.toN3().equals(toN3()));
		} else
			return false;
	}

	/**
	 * Override readObject for backwards compatability and storing hashcode
	 */
	private void readObject(ObjectInputStream ois)
			throws ClassNotFoundException, IOException {
		ois.defaultReadObject();
		if (!_data.startsWith("<") || !_data.endsWith(">"))
			_data = "<" + _data + ">";
	}

	/**
	 * Compares strings backwards... why? Cos it should be faster for URLs...
	 * 
	 * @param a
	 *            A string
	 * @param b
	 *            A string
	 * @return a = b ?
	 */
	public static boolean equals(String a, String b) {
		if (a == b) {
			return true;
		}

		int n = a.length();
		if (n == b.length()) {
			while (--n >= 0) {
				// faster cos it reads directly from the array
				if (a.charAt(n) != b.charAt(n))
					return false;
			}
			return true;
		}
		return false;
	}

	// public static void main(String args[]){
	// System.err.println(equals("asd", "asd"));
	// System.err.println(equals("asd", "asdf"));
	// System.err.println(equals("bsd", "asd"));
	//
	// long b4 = System.currentTimeMillis();
	// for(int i=0; i<10000000; i++){
	// "http://google.com/asd/asd".equals("http://google.com/asd/asd");
	// "http://google.com/asd/asd".equals("http://google.com/asd/asdf");
	// "http://google.com/asd/bsd".equals("http://google.com/asd/csd");
	// "http://google.com/asd/csd".equals("http://google.com/asd/dsd");
	// }
	//
	// System.err.println(System.currentTimeMillis()-b4);
	// b4 = System.currentTimeMillis();
	// for(int i=0; i<10000000; i++){
	// equals("http://google.com/asd/asd", "http://google.com/asd/asd");
	// equals("http://google.com/asd/asd", "http://google.com/asd/asdf");
	// equals("http://google.com/asd/bsd", "http://google.com/asd/csd");
	// equals("http://google.com/asd/csd", "http://google.com/asd/dsd");
	// }
	//
	// System.err.println(System.currentTimeMillis()-b4);
	// }
}
