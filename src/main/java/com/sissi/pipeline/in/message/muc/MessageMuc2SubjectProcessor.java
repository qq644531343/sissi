package com.sissi.pipeline.in.message.muc;

import com.sissi.context.JID;
import com.sissi.context.JIDContext;
import com.sissi.pipeline.in.ProxyProcessor;
import com.sissi.protocol.Protocol;
import com.sissi.protocol.message.Message;

/**
 * @author kim 2014年3月13日
 */
public class MessageMuc2SubjectProcessor extends ProxyProcessor {

	@Override
	public boolean input(JIDContext context, Protocol protocol) {
		JID group = super.build(protocol.getTo());
		protocol.cast(Message.class).noneThread().setFrom(group.resource(super.ourRelation(context.jid(), group).name()));
		for (JID jid : super.whoSubscribedMe(group)) {
			super.findOne(jid, true).write(protocol);
		}
		return true;
	}
}
