
package com.screen.mirroring.casttotv.tv.cast.screencast.castserver;

import java.util.Map;

/**
 * @author Paul S. Hawke (paul.hawke@gmail.com) On: 9/15/13 at 2:52 PM
 */
public class InternalRewrite extends NanoHTTPD.Response {
    private final String uri;
    private final Map<String, String> headers;

    public InternalRewrite(final Map<String, String> headers, final String uri) {
        super(null);
        this.headers = headers;
        this.uri = uri;
    }

    public String getUri() {
        return uri;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
}
