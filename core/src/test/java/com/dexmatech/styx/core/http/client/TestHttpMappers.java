package com.dexmatech.styx.core.http.client;

import com.dexmatech.styx.core.http.Headers;
import com.dexmatech.styx.core.http.HttpRequest;
import com.dexmatech.styx.core.http.HttpResponse;
import com.dexmatech.styx.testing.asynchttpclient.ClientResponse;
import com.dexmatech.styx.core.utils.IOUTils;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by aortiz on 7/09/16.
 */
public class TestHttpMappers {

	@Test
	public void shouldParseHeadersToClientHeaders() {
		// given
		Headers headers = Headers.from(new HashMap<>()).put("header-1", "value-1").put("header-2", "value-2");
		// when
		HttpHeaders clientHeaders = HttpMappers.asClientHeaders(headers);
		// then
		assertThat(clientHeaders.entries().size(), is(2));
		assertThat(clientHeaders.get("header-1"), is("value-1"));
		assertThat(clientHeaders.get("header-2"), is("value-2"));
	}

	@Test
	public void shouldParseClientHeadersToHeaders() {
		// given
		HttpHeaders clientHeaders = new DefaultHttpHeaders().add("header-1", "value-1").add("header-2", "value-2");
		// when
		Headers headers = HttpMappers.asHeaders(clientHeaders);
		// then
		assertThat(headers.toMap().entrySet().size(), is(2));
		assertThat(headers.get("header-1"), is("value-1"));
		assertThat(headers.get("header-2"), is("value-2"));
	}

	@Test
	public void shouldParseRequestAsClientRequest() throws URISyntaxException {
		// given
		Headers headers = Headers.empty().put("header-1", "value-1").put("header-2", "value-2");
		HttpRequest request = HttpRequest.get("", headers);
		// when
		Request clientRequest = HttpMappers.asClientRequest("http://localhost",request);
		// then
		assertThat(clientRequest, notNullValue());
		assertThat(clientRequest.getHeaders().get("header-1"), is("value-1"));
		assertThat(clientRequest.getHeaders().get("header-2"), is("value-2"));
		assertThat(clientRequest.getUrl(), is("http://localhost"));

	}

	@Test
	public void shouldParseClientResponseToResponse() {
		// given
		Map<String, String> headers = Headers.empty().put("header-1", "value-1").put("header-2", "value-2").toMap();
		Response clientResponse = ClientResponse.fake().withHeaders(headers).withBody("Hello world".getBytes()).build();
		Function<Response, HttpResponse> mapper = HttpMappers.generateResponseMapperFromHttpVersion("HTTP/1.1");
		// when
		HttpResponse response = mapper.apply(clientResponse);
		// then
		assertThat(response, notNullValue());
		assertThat(response.getHeaders().get("header-1"), is("value-1"));
		assertThat(response.getHeaders().get("header-2"), is("value-2"));
		assertThat(response.getMessageBody().isPresent(), is(true));
		assertThat(response.getMessageBody().map(IOUTils::toString).orElse("$"), is("Hello world"));

	}

	@Test
	public void shouldParseClientInternalServerErrorWithoutBodyToResponse() {
		// given
		Map<String, String> headers = Headers.empty().put("header-1", "value-1").put("header-2", "value-2").toMap();
		Response clientResponse = ClientResponse.fake().withHeaders(headers).build();
		Function<Response, HttpResponse> mapper = HttpMappers.generateResponseMapperFromHttpVersion("HTTP/1.1");
		// when
		HttpResponse response = mapper.apply(clientResponse);
		// then
		assertThat(response, notNullValue());
		assertThat(response.getHeaders().get("header-1"), is("value-1"));
		assertThat(response.getHeaders().get("header-2"), is("value-2"));
		assertThat(response.getMessageBody().isPresent(), is(false));

	}

}