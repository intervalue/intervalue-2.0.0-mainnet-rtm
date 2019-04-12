package one.inve.contract.sig;

import org.apache.commons.cli.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * 
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: Execute command of generating template or generating t.json
 * @author: Francis.Deng
 * @date: 2018年12月11日 下午4:55:07
 * @version: V1.0
 */
public class Signature {

	private String templateOutputFile = "consig.conf";
	private String confInputFile = "consig.conf";
	private String nonceURL = "http://localhost:8888/v1/getNonce";
	private String signingOutputFile = "t.json";

	public void run(String[] args) {
		ICommand com;
		// create the command line parser
		CommandLineParser parser = new DefaultParser();

		// create the Options
		Options options = new Options();

		// template part:
		Options genTempOptions = options.addOption("T", "gen-config-template", false,
				"generate a contract signing configuration template(consig.conf)");
		// genTempOptions.addOption("N", "nonce-endpoint", true, "to retrieve nonce by
		// the endpoint");
		genTempOptions.addOption("O", true, "output configuration template file");

		// signing part:
		Options signingOptions = options.addOption("S", "output-sig-msg", false, "output signature msg by consig.conf");
		Options iOptions = signingOptions.addOption("I", true,
				"input configuration file 'consig1.conf'(default 'consig.conf')");
		Options nOptions = iOptions.addOption("N", true,
				"the url from which system would get a nonce by address(default 'http://localhost:8888/v1/getNonce')");
		nOptions.addOption("O", true, "output signing file(default 't.json')");

		// options.addOption("C", "connect-http", false, "call http service");
		String CD = null;
		try {
			CD = new java.io.File(".").getCanonicalPath();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);

			if (line.hasOption('T')) {
				com = new GenTemplateCommand();

				if (line.hasOption('O'))
					templateOutputFile = line.getOptionValue('O');
//				String nonceEndpoint;
//
//				if (line.hasOption('N')) {
//					nonceEndpoint = line.getOptionValue('N');
//				} else {
//					nonceEndpoint = "http://localhost:8888/v1/getNonce";
//				}
//
//				com.execute(CD, "consig.conf", nonceEndpoint);
				com.execute(CD, templateOutputFile);
			} else if (line.hasOption('S')) {
				com = new OutputSigCommand();

				if (line.hasOption('I'))
					confInputFile = line.getOptionValue('I');
				if (line.hasOption('N'))
					nonceURL = line.getOptionValue('N');
				if (line.hasOption('O'))
					signingOutputFile = line.getOptionValue('O');

				com.execute(CD, confInputFile, nonceURL, signingOutputFile);
			} // else if (line.hasOption('C')) {
//				com = new OutputSigCommand();
//				Optional<Tuple> urlAndDataPairOpt = com.execute(CD, "consig.conf", "t.json");
//				// indicate whether there are target value and work-fine process
//				if (!urlAndDataPairOpt.isPresent()) {
//					Pair<String, String> urlAndDataPair = (Pair<String, String>) (urlAndDataPairOpt.get());
//
//					System.out.println(postJSON(urlAndDataPair.getValue0(), urlAndDataPair.getValue1()));
//				}
//			}
		} catch (ParseException exp) {
			System.out.println("Unexpected exception:" + exp.getMessage());
		}

	}

	protected String postJSON(String url, String json) {
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(url);
		String response = null;
		try {
			StringEntity s = new StringEntity(json.toString());
			s.setContentEncoding("UTF-8");
			s.setContentType("application/json");
			post.setEntity(s);

			HttpResponse res = client.execute(post);
			if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				HttpEntity entity = res.getEntity();
				String charset = EntityUtils.getContentCharSet(entity);
				response = steamOut(entity.getContent(), "UTF-8");
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return response;
	}

	private String steamOut(InputStream is, String charset) throws IOException {
		final int bufferSize = 1024;
		final char[] buffer = new char[bufferSize];
		final StringBuilder out = new StringBuilder();
		Reader in = new InputStreamReader(is, charset);// "UTF-8"
		for (;;) {
			int rsz = in.read(buffer, 0, buffer.length);
			if (rsz < 0)
				break;
			out.append(buffer, 0, rsz);
		}
		return out.toString();
	}

}
