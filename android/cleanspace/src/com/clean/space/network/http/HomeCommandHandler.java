package com.clean.space.network.http;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import android.content.Context;

public class HomeCommandHandler implements HttpRequestHandler {

	private Context context = null;
	private String host = "localhost";

	public HomeCommandHandler(Context context) {
		this.context = context;
	}

	public void handle(HttpRequest req, HttpResponse resp, HttpContext arg2)
			throws HttpException, IOException {

		this.host = req.getFirstHeader("Host").getValue();
		System.out.println("Host : " + host);
		HttpEntity entity = new EntityTemplate(new ContentProducer() {
			public void writeTo(final OutputStream outstream)
					throws IOException {
				OutputStreamWriter writer = new OutputStreamWriter(outstream,
						"UTF-8");
				String resp = "<html><head></head>"
						+ "<body><center><h1>Welcome to ClearSpace Server<h1></center>";
				writer.write(resp);
				writer.flush();
			}
		});
		resp.setHeader("Content-Type", "text/html");

		resp.setEntity(entity);

	}

}
