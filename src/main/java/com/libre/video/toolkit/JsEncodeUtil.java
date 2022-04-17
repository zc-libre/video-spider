package com.libre.video.toolkit;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.core.utils.StringUtil;
import net.dreamlu.mica.core.utils.UrlUtil;
import org.springframework.core.io.ClassPathResource;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Slf4j
@UtilityClass
public class JsEncodeUtil {

    public static String encodeRealVideoUrl(String html) {
        System.setProperty("nashorn.args", "--no-deprecation-warning");
        String regexEncode2 = "strencode2\\((.*?)\\)\\)";
        String encode2String = RegexUtil.getRegexValue(regexEncode2, 1, html);
        if (StringUtil.isBlank(encode2String)) {
            return null;
        }
        //调用js解码地址
        String strEncode = JsEncodeUtil.strencode(UrlUtil.decode(encode2String));
        String regex = "source src='(.*?)' type=";
        return RegexUtil.getRegexValue(regex, 1, strEncode);
    }

    public static String strencode(String str1) {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("js");

        try {
            ClassPathResource resource = new ClassPathResource("statics/js/md2.js");
            BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
            engine.eval(br);
        } catch (ScriptException e) {
            //忽略js脚本异常
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        if (engine instanceof Invocable) {
            Invocable invocable = (Invocable) engine;
            JavaScriptEncode executeMethod = invocable.getInterface(JavaScriptEncode.class);
            return executeMethod.strencode2(str1);
        }
        throw new RuntimeException("解密失敗");
    }

}



