package com.sissi.ucenter.muc.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import com.sissi.commons.Extracter;
import com.sissi.config.MongoConfig;
import com.sissi.context.JID;
import com.sissi.context.JIDBuilder;
import com.sissi.protocol.muc.ItemAffiliation;
import com.sissi.ucenter.Relation;
import com.sissi.ucenter.muc.MucAffiliationBuilder;

/**
 * @author kim 2014年3月19日
 */
public class MongoRelationMuc4AffiliationContext extends MongoRelationMucContext {

	private final DBObject aggregateUnwind = BasicDBObjectBuilder.start("$unwind", "$" + MongoConfig.FIELD_NICKS).get();

	private final DBObject aggregateSort = BasicDBObjectBuilder.start("$sort", BasicDBObjectBuilder.start(MongoConfig.FIELD_RESOURCE, -1).get()).get();

	private final DBObject aggregateProject = BasicDBObjectBuilder.start("$project", BasicDBObjectBuilder.start().add(MongoConfig.FIELD_AFFILIATION, "$" + MongoConfig.FIELD_AFFILIATIONS).get()).get();

	private final DBObject aggregateProjectSubscribed = BasicDBObjectBuilder.start("$project", BasicDBObjectBuilder.start().add(MongoConfig.FIELD_JID, "$" + MongoConfig.FIELD_JID).add(MongoConfig.FIELD_RESOURCE, "$" + MongoConfig.FIELD_NICKS + "." + MongoConfig.FIELD_NICK).get()).get();

	private final MucAffiliationBuilder mucAffiliationBuilder;

	private final boolean cascade;

	public MongoRelationMuc4AffiliationContext(boolean activate, boolean cascade, String mapping, MongoConfig config, JIDBuilder jidBuilder, MucAffiliationBuilder mucAffiliationBuilder) throws Exception {
		super(activate, mapping, config, jidBuilder);
		this.mucAffiliationBuilder = mucAffiliationBuilder;
		this.cascade = cascade;
	}

	public Set<JID> iSubscribedWho(JID from) {
		DBObject match = BasicDBObjectBuilder.start("$match", BasicDBObjectBuilder.start(MongoConfig.FIELD_AFFILIATIONS + "." + MongoConfig.FIELD_JID, from.asStringWithBare()).get()).get();
		return new JIDGroup(Extracter.asList(this.config.collection().aggregate(match, this.aggregateUnwindAffiliation, match, this.aggregateUnwind, BasicDBObjectBuilder.start("$match", BasicDBObjectBuilder.start(MongoConfig.FIELD_NICKS + "." + MongoConfig.FIELD_JID, from.asStringWithBare()).get()).get(), this.aggregateProjectSubscribed, this.aggregateSort, this.aggregateLimit).getCommandResult(), MongoConfig.FIELD_RESULT));
	}

	public Set<Relation> myRelations(JID from, String affiliation) {
		AggregationOutput output = super.config.collection().aggregate(super.buildMatcher(from), super.aggregateUnwindAffiliation, BasicDBObjectBuilder.start("$match", BasicDBObjectBuilder.start(MongoConfig.FIELD_AFFILIATIONS + "." + MongoConfig.FIELD_AFFILIATION, affiliation).get()).get(), this.aggregateProject);
		List<?> result = Extracter.asList(output.getCommandResult(), MongoConfig.FIELD_RESULT);
		return result.isEmpty() ? super.emptyRelations : new AffiliationRelations(result);
	}

	@Override
	public MongoRelationMucContext update(JID from, JID to, String status) {
		this.mucAffiliationBuilder.build(to).approve(from, status);
		if (this.cascade && (ItemAffiliation.OUTCAST.equals(status) || !ItemAffiliation.parse(status).contains(super.mucConfigBuilder.build(to).pull(MongoConfig.FIELD_AFFILIATION, String.class)))) {
			super.remove(from, to, true);
		}
		return this;
	}

	private final class AffiliationRelations extends HashSet<Relation> {

		private static final long serialVersionUID = 1L;

		private AffiliationRelations(List<?> affiliations) {
			for (Object each : affiliations) {
				DBObject affiliation = Extracter.asDBObject(DBObject.class.cast(each), MongoConfig.FIELD_AFFILIATION);
				super.add(new MucNoneRelation(MongoRelationMuc4AffiliationContext.this.jidBuilder.build(Extracter.asString(affiliation, MongoConfig.FIELD_JID)), ItemAffiliation.parse(Extracter.asString(affiliation, MongoConfig.FIELD_AFFILIATION))));
			}
		}
	}
}
