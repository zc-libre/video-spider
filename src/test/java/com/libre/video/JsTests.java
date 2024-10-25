package com.libre.video;

import net.dreamlu.mica.http.HttpRequest;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import java.util.List;

public class JsTests {

	public static void main(String[] args) {
//		ScriptEngineManager manager = new ScriptEngineManager();
//		ScriptEngine engine = manager.getEngineByName("javascript");
//       	System.out.println(engine);
		request();

	}

	private static void request() {
		String string = HttpRequest.get("https://ternity.net/vodplay/76656.shtml").execute().asString();
		System.out.println(string);
	}
}
