package org.apache.marmotta.ldpath.model.functions;

import com.google.common.net.UrlEscapers;

public class UrlEncodeFunction<Node> extends AbstractTextFilterFunction {
    @Override
    protected String getLocalName() {
        return "urlencode";
    }

    @Override
    protected String doFilter(String in) {
        return UrlEscapers.urlPathSegmentEscaper().escape(in);
    }

    @Override
    public String getDescription() {
        return "fn:urlencode(String) : String";
    }
}
