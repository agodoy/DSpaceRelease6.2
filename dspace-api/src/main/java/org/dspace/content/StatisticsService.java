package org.dspace.content;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import org.apache.solr.client.solrj.SolrServerException;
import org.dspace.content.service.DSpaceObjectLegacySupportService;
import org.dspace.content.service.DSpaceObjectService;
import org.dspace.core.Context;
import org.dspace.statistics.ObjectCount;
import org.dspace.statistics.ObjectStatistics;

public interface StatisticsService extends DSpaceObjectService<Holder>, DSpaceObjectLegacySupportService<Community>
{

	List<Holder> findAll(Context context) throws SQLException;

	ObjectCount[] viewItemsStatistics(Context context) throws SolrServerException, SQLException;

	ObjectStatistics[] downloadItemsStatistics(Context context) throws SolrServerException, SQLException;

	HashMap authorsStatistics(Context context) throws SolrServerException, SQLException;

}
