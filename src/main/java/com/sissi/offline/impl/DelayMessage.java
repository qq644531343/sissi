package com.sissi.offline.impl;

import java.util.Map;

import com.mongodb.BasicDBObjectBuilder;
import com.sissi.protocol.Element;
import com.sissi.protocol.message.Body;
import com.sissi.protocol.message.Message;
import com.sissi.protocol.offline.Delay;

/**
 * @author kim 2013-11-15
 */
public class DelayMessage extends DelayProtocol {

	private final String body = "body";

	public DelayMessage() {
		super(Message.class);
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> query(Element element) {
		return BasicDBObjectBuilder.start(DelayProtocol.FIELD_ID, element.getId()).get().toMap();
	}

	@Override
	public Map<String, Object> write(Element element) {
		Map<String, Object> entity = super.based(element);
		entity.put(this.body, Message.class.cast(element).getBody().getText());
		return entity;
	}

	@Override
	public Message read(Map<String, Object> element) {
		Message message = Message.class.cast(super.based(element, new Message()));
		return message.setBody(new Body(element.get(this.body).toString())).setDelay(new Delay(super.getOffline(), message.getFrom(), element.get(FIELD_DELAY).toString()));
	}

	public Boolean isSupport(Element element) {
		return super.isSupport(element) && Message.class.cast(element).hasContent();
	}
}
