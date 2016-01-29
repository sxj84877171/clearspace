package com.clean.space;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.clean.space.log.FLog;

public class FeedbackNetHelp {
	public final static String uri = "http://" +"115.29.178.5"
			+ "/feedback/publish";

	public static boolean feedback(List<NameValuePair> paras) {
		HttpPost hp = new HttpPost(uri);
		HttpResponse httpResponse = null;

		try {
			hp.setEntity(new UrlEncodedFormEntity(paras));
			httpResponse = new DefaultHttpClient().execute(hp);
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean feedback(String username, String content) {
		NameValuePair cNameVaule = new BasicNameValuePair("content", content);
		NameValuePair dNameVaule = new BasicNameValuePair("devicetype",
				"android");
		NameValuePair uNameVaule = new BasicNameValuePair("username", username);
		List<NameValuePair> paras = new ArrayList<NameValuePair>();
		paras.add(cNameVaule);
		paras.add(dNameVaule);
		paras.add(uNameVaule);
		return feedback(paras);
	}

	public static boolean feedback(NameValuePair value) {
		List<NameValuePair> paras = new ArrayList<NameValuePair>();
		paras.add(value);
		return feedback(paras);
	}

	public static boolean feedback(NameValuePair connfail,
			NameValuePair badperformance, NameValuePair crash,
			NameValuePair fewfeatures, NameValuePair delay,
			NameValuePair hardtouse, NameValuePair content,
			NameValuePair devicetype, NameValuePair nonotification) {
		List<NameValuePair> paras = new ArrayList<NameValuePair>();
		if (connfail != null) {
			paras.add(connfail);
		}
		if (badperformance != null) {
			paras.add(badperformance);
		}
		if (crash != null) {
			paras.add(crash);
		}
		if (fewfeatures != null) {
			paras.add(fewfeatures);
		}
		if (delay != null) {
			paras.add(delay);
		}
		if (hardtouse != null) {
			paras.add(hardtouse);
		}
		if (content != null) {
			paras.add(content);
		}
		if (nonotification != null) {
			paras.add(nonotification);
		}
		if (devicetype != null) {
			paras.add(devicetype);
		}
		return feedback(paras);
	}
	
	public static boolean feedback(String username, String content, File logFile) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("username", username);
		params.put("content", content);
		params.put("devicetype", "android");
		try {
			Map<String, File> files = new HashMap<String, File>();
			files.put(logFile.getName(), logFile);
			if(uploadFile(uri, params, files) != null){
				return true;
			}
		} catch (Exception e) {
			FLog.e("FeedbackNetHelp", e);
		}
		return false;
	}
	
	public static String uploadFile(String actionUrl, Map<String, String> params, Map<String, File> files) throws IOException {
		final String PREFIX = "--";
		final String BOUNDARY = java.util.UUID.randomUUID().toString();		
		final String LINEND = "\r\n";
		final String MULTIPART_FROM_DATA = "multipart/form-data";
		final String CHARSET = "UTF-8";
		
		URL uri = new URL(actionUrl);
		HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
		conn.setReadTimeout(60 * 1000);
		conn.setUseCaches(false);
		conn.setDoInput(true);			// 允许输入
		conn.setDoOutput(true);			// 允许输出
		conn.setRequestMethod("POST");	// Post方式
		conn.setRequestProperty("connection", "keep-alive");
		conn.setRequestProperty("Charsert", "UTF-8");
		conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA + ";boundary=" + BOUNDARY);

		// 首先组拼文本类型的参数
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> entry : params.entrySet()) {
			sb.append(PREFIX);
			sb.append(BOUNDARY);
			sb.append(LINEND);
			sb.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + LINEND);
			sb.append("Content-Type: text/plain; charset=" + CHARSET + LINEND);
			sb.append("Content-Transfer-Encoding: 8bit" + LINEND);
			sb.append(LINEND);
			sb.append(entry.getValue());
			sb.append(LINEND);
		}

		DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());
		outStream.write(sb.toString().getBytes());

		// 发送文件数据
		if (files != null)
			for (Map.Entry<String, File> file : files.entrySet()) {
				StringBuilder sb1 = new StringBuilder();
				sb1.append(PREFIX);
				sb1.append(BOUNDARY);
				sb1.append(LINEND);
				sb1.append("Content-Disposition: form-data; name=\"file\"; filename=\""	+ file.getKey() + "\"" + LINEND);
				sb1.append("Content-Type: application/octet-stream; charset=" + CHARSET + LINEND);
				sb1.append(LINEND);
				outStream.write(sb1.toString().getBytes());
				File is = file.getValue();
				
				byte[] buffer = new byte[1024];
				int len = 0;
				FileInputStream fis = new FileInputStream(is);
				while ((len = fis.read(buffer)) != -1) {
					outStream.write(buffer, 0, len);
				}
				fis.close();
				outStream.write(LINEND.getBytes());
			}

		// 请求结束标志
		byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
		outStream.write(end_data);
		outStream.flush();

		// 得到响应码
        FLog.i("IconHelper","try to getResponseCode");
		int res = conn.getResponseCode();
        FLog.i("IconHelper","try to getResponseCode res=" + res);
        String line = "";
        String data = "";
        if(200 == res){
            InputStream in = conn.getInputStream();
            InputStreamReader isReader = new InputStreamReader(in);
            BufferedReader bufReader = new BufferedReader(isReader);

            while(res == 200 && line != null) {
                line = bufReader.readLine();
                if(line == null)
                    break;

                data += line;
            }

        }else{
        	return null;
        }

		outStream.close();
		conn.disconnect();
		return data;
	}

}
