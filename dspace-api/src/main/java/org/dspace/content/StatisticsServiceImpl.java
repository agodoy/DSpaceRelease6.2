package org.dspace.content;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.dao.BitstreamDAO;
import org.dspace.content.dao.ItemDAO;
import org.dspace.content.dao.MetadataValueDAO;
import org.dspace.core.Context;
import org.dspace.services.ConfigurationService;
import org.dspace.statistics.ObjectCount;
import org.dspace.statistics.service.SolrLoggerService;
import org.springframework.beans.factory.annotation.Autowired;

public class StatisticsServiceImpl extends DSpaceObjectServiceImpl<Holder> implements StatisticsService {

	@Autowired(required = true)
	protected ItemDAO itemDao;

	@Autowired(required = true)
	protected BitstreamDAO bitstreamDao;

	@Autowired(required = true)
	protected MetadataValueDAO metadataDao;

	@Autowired(required = true)
	private ConfigurationService configurationService;

	@Autowired(required = true)
	private SolrLoggerService solrLoggerService;

	@Override
	public List<Holder> findAll(Context context) throws SQLException {

		List<String> emails = new ArrayList<String>();
		List<Holder> holders = new ArrayList<Holder>();
		List<Item> items = itemDao.findAll(context);

		for (Item item : items) {

			if (item.getSubmitter() != null && !emails.contains(item.getSubmitter().getEmail())) {

				Holder holder = new Holder();

				holder.setCorreo(item.getSubmitter().getEmail());

				List<MetadataValue> metadata = item.getSubmitter().getMetadata();

				for (MetadataValue meta : metadata) {
					if (meta.getMetadataField().getID().equals(new Integer(74)))
						holder.setNombre(meta.getValue());
					if (meta.getMetadataField().getID().equals(new Integer(75))) {
						String[] apellidos = StringUtils.split(meta.getValue(), " ");
						if (apellidos.length > 0)
							holder.setpApellido(apellidos[0]);
						if (apellidos.length > 1)
							holder.setsApellido(apellidos[1]);
					}
					if (meta.getMetadataField().getID().equals(new Integer(76)))
						holder.setNumTel(meta.getValue());
				}

				holders.add(holder);
				emails.add(item.getSubmitter().getEmail());

			}
		}

		return holders;

	}

	@Override
	public Holder find(Context context, UUID id) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateLastModified(Context context, Holder dso) throws SQLException, AuthorizeException {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(Context context, Holder dso) throws SQLException, AuthorizeException, IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public int getSupportsTypeConstant() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Community findByIdOrLegacyId(Context context, String id) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Community findByLegacyId(Context context, int id) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ObjectCount[] viewItemsStatistics(Context context) throws SolrServerException, SQLException {

		String url = configurationService.getProperty("dspace.baseUrl");
		ObjectCount[] items = solrLoggerService.queryFacetField("type:2", "statistics_type:view", "id", 5000000, false,
				null);

		for (ObjectCount objectCount : items) {
			Item item;
			try {
				item = itemDao.findByLegacyId(context, Integer.parseInt(objectCount.getValue()), Item.class);
			} catch (NumberFormatException e) {
				item = itemDao.findByID(context, Item.class, UUID.fromString(objectCount.getValue()));
			}

			if (item != null)
				objectCount.setValue(url + "/handle/" + item.getHandle());

		}
		return items;
	}

	@Override
	public ObjectCount[] downloadItemsStatistics(Context context) throws SolrServerException, SQLException {

		String url = configurationService.getProperty("dspace.baseUrl");
		ObjectCount[] bitstreams = solrLoggerService.queryFacetField("type:0", "statistics_type:view", "id", 5000000,
				false, null);

		for (ObjectCount objectCount : bitstreams) {
			Bitstream bitstream;
			try {
				bitstream = bitstreamDao.findByLegacyId(context, Integer.parseInt(objectCount.getValue()),
						Bitstream.class);
			} catch (NumberFormatException e) {
				bitstream = bitstreamDao.findByID(context, Bitstream.class, UUID.fromString(objectCount.getValue()));
			}
			
			if(!bitstream.getBundles().isEmpty() && !bitstream.getBundles().get(0).getItems().isEmpty())
				objectCount.setValue(url + "/handle/" + bitstream.getBundles().get(0).getItems().get(0).getHandle());
			
		}

		return bitstreams;
	}

	@Override
	public HashMap authorsStatistics(Context context) throws SolrServerException, SQLException {

		ObjectCount[] items = solrLoggerService.queryFacetField("type:2", "statistics_type:view", "id", 500000, false,null);
		
		HashMap<String, Integer> authors = new HashMap<String, Integer>();

		for (ObjectCount objectCount : items) {

			Item item; 

			try {
				item = itemDao.findByLegacyId(context, Integer.parseInt(objectCount.getValue()), Item.class);
			} catch (NumberFormatException e) {
				item = itemDao.findByID(context, Item.class, UUID.fromString(objectCount.getValue()));
			}

			try {

				String authorName = null;
				for (MetadataValue meta : item.getMetadata())
					if (meta.getMetadataField().getID().equals(new Integer(3)))
						authorName = meta.getValue();

				if (authorName != null && authors.containsKey(authorName)) {
					Integer count = authors.get(authorName);
					authors.put(authorName, count + new Long(objectCount.getCount()).intValue());
				} else if (authorName != null)
					authors.put(authorName, new Long(objectCount.getCount()).intValue());

			} catch (Exception e1) {
				e1.getCause();
			}
		}

		return authors;
	}

}
