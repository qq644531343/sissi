package com.sissi.ucenter.relation;

import java.util.Map;

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import com.sissi.config.MongoConfig;
import com.sissi.protocol.iq.roster.RosterSubscription;
import com.sissi.ucenter.Relation;
import com.sissi.ucenter.RelationRoster;

/**
 * @author kim 2013年12月30日
 */
public class MongoRelationRosterContext extends MongoRelationContext {

	private final String groups = "groups";

	public MongoRelationRosterContext(MongoConfig config) {
		super(config);
	}

	@Override
	protected Relation build(DBObject db) {
		return new MongoRelationRoster(db);
	}

	private class MongoRelationRoster implements RelationRoster {

		private final String jid;

		private final String name;

		private final Integer subscription;

		private final String[] groups;

		public MongoRelationRoster(DBObject db) {
			super();
			this.jid = MongoRelationRosterContext.super.config.asString(db, MongoRelationRosterContext.super.slave);
			this.name = MongoRelationRosterContext.super.config.asString(db, MongoRelationRosterContext.super.nick);
			this.subscription = MongoRelationRosterContext.super.config.asInteger(db, MongoRelationRosterContext.super.state);
			this.groups = BasicDBList.class.cast(db.get(MongoRelationRosterContext.this.groups)).toArray(new String[] {});
		}

		public String getJID() {
			return this.jid;
		}

		public String getName() {
			return this.name;
		}

		public String[] asGroups() {
			return this.groups;
		}

		public String getSubscription() {
			return RosterSubscription.toString(this.subscription);
		}

		public Map<String, Object> plus() {
			return MongoRelationRosterContext.this.plus;
		}
	}
}
