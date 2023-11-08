package com.libre.video.core.websocker;

import com.libre.core.json.JsonUtil;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@Component
@EqualsAndHashCode
@ServerEndpoint("/webSocket/{sid}")
public class WebSocketServer {

	/**
	 * concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
	 */
	private static final CopyOnWriteArraySet<WebSocketServer> webSocketSet = new CopyOnWriteArraySet<>();

	/**
	 * 与某个客户端的连接会话，需要通过它来给客户端发送数据
	 */
	private Session session;

	/**
	 * 接收sid
	 */
	private String sid = "";

	/**
	 * 连接建立成功调用的方法
	 */
	@OnOpen
	public void onOpen(Session session, @PathParam("sid") String sid) {
		this.session = session;
		webSocketSet.removeIf(webSocket -> webSocket.sid.equals(sid));
		webSocketSet.add(this);
		this.sid = sid;
	}

	/**
	 * 连接关闭调用的方法
	 */
	@OnClose
	public void onClose() {
		webSocketSet.remove(this);
	}

	/**
	 * 收到客户端消息后调用的方法
	 * @param message 客户端发送过来的消息
	 */
	@OnMessage
	public void onMessage(String message, Session session) {
		log.debug("收到来" + sid + "的信息:" + message);
		// 群发消息
		for (WebSocketServer item : webSocketSet) {
			try {
				item.sendMessage(message);
			}
			catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	@OnError
	public void onError(Session session, Throwable error) {
		log.error("发生错误: {}", error.getMessage());
	}

	/**
	 * 实现服务器主动推送
	 */
	private void sendMessage(String message) throws IOException {
		this.session.getBasicRemote().sendText(message);
	}

	/**
	 * 自定义消息
	 */
	public void sendInfo(VideoDownloadMessage message, @PathParam("sid") String sid) throws IOException {
		String msg = JsonUtil.toJson(message);
		log.debug("推送消息到: {}，推送内容:{}", sid, message);
		for (WebSocketServer item : webSocketSet) {
			try {
				if (sid == null) {
					item.sendMessage(msg);
				}
				else if (item.sid.equals(sid)) {
					item.sendMessage(msg);
				}
			}
			catch (IOException e) {
				log.error(e.getMessage());
			}
		}
	}

}
