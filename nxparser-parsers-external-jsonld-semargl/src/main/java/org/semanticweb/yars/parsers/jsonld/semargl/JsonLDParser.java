package org.semanticweb.yars.parsers.jsonld.semargl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import org.semanticweb.yars.nx.BNode;
import org.semanticweb.yars.nx.Literal;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semarglproject.rdf.ParseException;
import org.semarglproject.jsonld.JsonLdParser;
import org.semarglproject.sink.QuadSink;
import org.semarglproject.source.StreamProcessor;

/**
 * Extracts JSON-LD from input streams / readers using semargl's JSON-LD parser to
 * NxParser's data model.
 *
 * @author Tobias Käfer
 * @author Leonard Lausen
 */
public class JsonLDParser implements Iterator<Node[]>, Iterable<Node[]> {

	BlockingDeque<Node[]> _deque;
	StreamProcessor _sp;

	boolean _streamOpen = true;

	/**
	 * @throws SAXException
	 */
	public JsonLDParser()  {
		this._deque = new LinkedBlockingDeque<Node[]>();
		this._sp = createStreamProcessor();
	}

	public Iterator<Node[]> parse(Reader r, String baseURI)
			throws ParseException {
		return parse(new BufferedReader(r), baseURI);
	}

	public Iterator<Node[]> parse(InputStream is, final String baseURI)
			throws  ParseException {
		return parse(new InputStreamReader(is), baseURI);
	}

	public Iterator<Node[]> parse(InputStream is, Charset cs,
			final String baseURI) throws ParseException {
		return parse(new InputStreamReader(is, cs), baseURI);
	}

	public Iterator<Node[]> parse(BufferedReader br, String baseURI)
			throws  ParseException {
		_sp.setProperty("setBaseUri", baseURI);
		_sp.process(br, baseURI);
		return this;
	}

	@Override
	public Iterator<Node[]> iterator() {
		return this;
	}

	@Override
	public boolean hasNext() {
		return _streamOpen || !_deque.isEmpty();
	}

	@Override
	public Node[] next() {
		try {
			return _deque.takeFirst();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return _deque.pop();
		}
	}

	private static Node createBnodeOrResource(String node, String context) {
		return node.startsWith("_:") ? BNode.createBNode(context,
				node.substring(3)) : new Resource(node);
	}

	private StreamProcessor createStreamProcessor() {
		StreamProcessor sp = new StreamProcessor(
				JsonLdParser.connect(new QuadSink() {

					Resource _context = null;
					String _contextString = null;

					@Override
					public void addNonLiteral(String arg0, String arg1,
							String arg2) {
						if (_contextString == null)
							throw new IllegalStateException(
									"need context to work.");
						_deque.add(new Node[] {
								createBnodeOrResource(arg0, _contextString),
								new Resource(arg1),
								createBnodeOrResource(arg2, _contextString),
								_context });

					}

					@Override
					public void addPlainLiteral(String arg0, String arg1,
							String arg2, String arg3) {
						if (_contextString == null)
							throw new IllegalStateException(
									"need context to work.");
						_deque.add(new Node[] {
								createBnodeOrResource(arg0, _contextString),
								new Resource(arg1), new Literal(arg2, arg3),
								_context });

					}

					@Override
					public void addTypedLiteral(String arg0, String arg1,
							String arg2, String arg3) {

						if (_contextString == null)
							throw new IllegalStateException(
									"need context to work.");
						_deque.add(new Node[] {
								createBnodeOrResource(arg0, _contextString),
								new Resource(arg1),
								new Literal(arg2, new Resource(arg3)), _context });

					}

					@Override
					public void endStream() throws ParseException {
						_streamOpen = false;
					}

					@Override
					public void setBaseUri(String arg0) {
						_contextString = arg0;
						_context = new Resource(arg0);
					}

					@Override
					public boolean setProperty(String arg0, Object arg1) {
						if (arg0.equals("setBaseUri")
								&& (arg1 instanceof String)) {
							setBaseUri((String) arg1);
							return true;
						}
						return false;
					}

					@Override
					public void startStream() throws ParseException {
						_streamOpen = true;
					}

					@Override
					public void addNonLiteral(String arg0, String arg1,
							String arg2, String arg3) {
						_deque.add(new Node[] {
								createBnodeOrResource(arg0, _contextString),
								new Resource(arg1),
								createBnodeOrResource(arg2, _contextString),
								_context });

					}

					@Override
					public void addPlainLiteral(String arg0, String arg1,
							String arg2, String arg3, String arg4) {
						_deque.add(new Node[] {
								createBnodeOrResource(arg0, _contextString),
								new Resource(arg1), new Literal(arg2, arg3),
								_context });

					}

					@Override
					public void addTypedLiteral(String arg0, String arg1,
							String arg2, String arg3, String arg4) {
						_deque.add(new Node[] {
								createBnodeOrResource(arg0, _contextString),
								new Resource(arg1),
								new Literal(arg2, new Resource(arg3)),
								_context });
					}
				}));

		return sp;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
