package com.sissi.offline.impl;

import java.util.Map;

import com.mongodb.BasicDBObjectBuilder;
import com.sissi.protocol.Element;
import com.sissi.protocol.offline.Delay;
import com.sissi.protocol.presence.Presence;
import com.sissi.protocol.presence.PresenceType;

/**
 * @author kim 2013-11-15
 */
public class DelayPresence extends DelayProtocol {

	public DelayPresence() {
		super(Presence.class);
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> query(Element element) {
		return BasicDBObjectBuilder.start().add(DelayProtocol.FIELD_FROM, element.getFrom()).add(DelayProtocol.FIELD_TO, element.getTo()).add(DelayProtocol.FIELD_TYPE, element.getType()).get().toMap();
	}
	
	@Override
	public Map<String, Object> write(Element element) {
		return super.based(element);
	}

	@Override
	public Element read(Map<String, Object> element) {
		Presence presence = (Presence) super.based(element, new Presence());
		return presence.setDelay(new Delay(super.getOffline(), presence.getFrom(), element.get(FIELD_DELAY).toString()));
	}

	public Boolean isSupport(Element element) {
		return super.isSupport(element) && !PresenceType.parse(element.getType()).in(PresenceType.AVAILABLE, PresenceType.UNAVAILABLE);
	}
}
