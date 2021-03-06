package com.dexmatech.styx.core.http.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

/**
 * Created by aortiz on 14/09/16.
 */
public class Uris {
	// TODO : add user:password
	// scheme:[//[user:password@]host[:port]][/]path[?query][#fragment]
	public static String changeHost(URI uri, String host) {

		String hostAndPort;
		String[] splitByPortSeparator = host.split(":");
		boolean isPortIncluded = splitByPortSeparator.length == 2;
		if(isPortIncluded) {
			hostAndPort = host;
		} else {
			String port = Optional.ofNullable(uri.getPort()).filter(p -> p != -1).map(p -> ":" + p).orElseGet(() -> "");
			hostAndPort = host + port;
		}

		String scheme = Optional.ofNullable(uri.getScheme()).map(s -> s + "://").orElseGet(() -> "http://");
		String queryParams = Optional.ofNullable(uri.getQuery()).map(q -> "?" + q).orElseGet(() -> "");
		return scheme + hostAndPort + uri.getPath() + queryParams;
	}

	public static URI create(String uri) {

		try {
			return URI.create(uri).parseServerAuthority();
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		}

	}
}
