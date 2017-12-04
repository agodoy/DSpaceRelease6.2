/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.rest;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.dspace.authorize.factory.AuthorizeServiceFactory;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.content.StatisticsService;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.ItemService;
import org.dspace.content.service.WorkspaceItemService;
import org.dspace.rest.common.Holder;
import org.dspace.rest.exceptions.ContextException;
import org.dspace.statistics.ObjectCount;
import org.dspace.statistics.ObjectStatistics;

/**
 * This class provides all CRUD operation over holders.
 * 
 * @authora agodoy
 */
@Path("/estadisticas")
public class StatisticsResource extends Resource {
	protected StatisticsService statisticsService = ContentServiceFactory.getInstance().getStatisticsService();
	protected ItemService itemService = ContentServiceFactory.getInstance().getItemService();
	protected AuthorizeService authorizeService = AuthorizeServiceFactory.getInstance().getAuthorizeService();
	protected WorkspaceItemService workspaceItemService = ContentServiceFactory.getInstance().getWorkspaceItemService();
	
	private static Logger log = Logger.getLogger(StatisticsResource.class);

	/**
	 * Return array of all holders in DSpace. You can add more properties
	 * through expand parameter.
	 * 
	 * @return Return array of holders, on which has logged user permission to
	 *         view.
	 * @throws WebApplicationException
	 *             It is thrown when was problem with database reading
	 *             (SQLException) or problem with creating
	 *             context(ContextException).
	 */
	@GET
	@Path("/padron")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public List<Holder> getHolders(@QueryParam("user") String user, @QueryParam("password") String password)
			throws WebApplicationException {
		org.dspace.core.Context context = null;
		List<org.dspace.content.Holder> holders = null;
		List<Holder> holdersResult = new ArrayList<Holder>();

		try {

			if (authorizeService.holderAuthorization(user, password)) {
				context = createContext();

				holders = statisticsService.findAll(context);

				for (org.dspace.content.Holder holder : holders) {
					Holder h = new Holder();
					h.setCorreo(holder.getCorreo());
					h.setNombre(holder.getNombre());
					h.setNumTel(holder.getNumTel());
					h.setpApellido(holder.getpApellido());
					h.setsApellido(holder.getsApellido());
					holdersResult.add(h);
				}

				context.complete();

			}

		} catch (SQLException e) {
			processException("Something went wrong while reading holders from database. Message: " + e, context);
		} catch (ContextException e) {
			processException("Something went wrong while reading holders, ContextError. Message: " + e.getMessage(),
					context);
		} finally {
			processFinally(context);
		}

		log.info("All holders were successfully read.");
		return holdersResult;
	}

	@GET
	@Path("/ranking/articulos")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public ObjectCount[] viewItems() throws WebApplicationException {

		ObjectCount[] result = null;
		org.dspace.core.Context context = null;

		try {

			context = createContext();

			result = statisticsService.viewItemsStatistics(context);
			
			context.complete();

		} catch (SQLException e) {
			processException("Something went wrong while reading items views statistics from database. Message: " + e,
					context);
		} catch (ContextException e) {
			processException("Something went wrong while reading items views statistics, ContextError. Message: "
					+ e.getMessage(), context);
		} catch (SolrServerException e) {
			processException("Something went wrong while reading items views statistics, ContextError. Message: "
					+ e.getMessage(), context);
		} finally {
			processFinally(context);
		}

		return result;
	}

	@GET
	@Path("/descargas")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public ObjectStatistics[] downloadItems() throws WebApplicationException {

		ObjectStatistics[] result = null;
		org.dspace.core.Context context = null;

		try {

			context = createContext();
			
			result = statisticsService.downloadItemsStatistics(context);

			context.complete();

		} catch (SQLException e) {
			processException(
					"Something went wrong while reading items downloads statistics from database. Message: " + e,
					context);
		} catch (ContextException e) {
			processException("Something went wrong while reading items downloads statistics, ContextError. Message: "
					+ e.getMessage(), context);
		} catch (SolrServerException e) {
			processException("Something went wrong while reading items downloads statistics, ContextError. Message: "
					+ e.getMessage(), context);
		} finally {
			processFinally(context);
		}

		return result;
	}
	

	@GET
	@Path("/ranking/autores")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Map viewAuthors() throws WebApplicationException {

		Map result = null;
		org.dspace.core.Context context = null;

		try {

			context = createContext();
			
			result = statisticsService.authorsStatistics(context);

			context.complete();

		} catch (SQLException e) {
			processException(
					"Something went wrong while reading authors statistics from database. Message: " + e,
					context);
		} catch (ContextException e) {
			processException("Something went wrong while reading authors statistics, ContextError. Message: "
					+ e.getMessage(), context);
		} catch (SolrServerException e) {
			processException("Something went wrong while reading authors statistics, ContextError. Message: "
					+ e.getMessage(), context);
		} finally {
			processFinally(context);
		}

		return result;
	}
}
