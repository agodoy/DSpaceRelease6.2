package org.dspace.authority.curp;

import java.net.MalformedURLException;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.dspace.authority.AuthoritySearchService;
import org.dspace.authority.AuthorityTypes;
import org.dspace.authority.indexer.AuthorityIndexingService;
import org.dspace.services.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class CurpServiceImpl implements CurpService {

	@Autowired(required = true)
	private AuthoritySearchService solrService;

	@Autowired(required = true)
	private AuthorityIndexingService indexingService;

	@Autowired(required = true)
	protected AuthorityTypes authorityTypes;

	@Autowired(required = true)
	private ConfigurationService configurationService;

	private static Logger log = Logger.getLogger(CurpServiceImpl.class);

	private static final String REST_CREDENTIALS = "imta:imTAt05_06";
	private static final Integer DEFAULT_LOWER_LIMIT = 0;

	@Override
	public void indexCurps() throws Exception {

		String credentials = configurationService.getProperty("curp.authorization.credentials");
		String curpIdFromValue = configurationService.getProperty("curp.curp_id_from");

		Integer curpIdFrom = curpIdFromValue != null && !curpIdFromValue.isEmpty() ? Integer.parseInt(curpIdFromValue)
				: DEFAULT_LOWER_LIMIT;

		RestTemplate restTemplate = new RestTemplate();

		byte[] plainCredsBytes = credentials != null && !credentials.isEmpty() ? credentials.getBytes()
				: REST_CREDENTIALS.getBytes();
		byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
		String base64Creds = new String(base64CredsBytes);

		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Basic " + base64Creds);

		HttpEntity<String> request = new HttpEntity<String>(headers);

		Integer curpIdTo;
		try {
			HttpHeaders headers1 = new HttpHeaders();
			headers1.add("Authorization", "Basic " + base64Creds);
			headers1.add("Accept", "text/plain");
			HttpEntity<String> request1 = new HttpEntity<String>(headers1);
			String url = "http://catalogs.repositorionacionalcti.mx/webresources/persona/count";
			ResponseEntity<String> upperLimit = restTemplate.exchange(url, HttpMethod.GET, request1, String.class);
			curpIdTo = Integer.parseInt(upperLimit.getBody());
		} catch (Exception e) {
			System.out.println("Ocurrió un error al obtener el limite superior de curps");
			e.printStackTrace();
			throw e;
		}

		for (int i = curpIdFrom; i < curpIdTo; i++)

			try {
				String url = "http://catalogs.repositorionacionalcti.mx/webresources/persona/" + i;
				ResponseEntity<CurpData> response = restTemplate.exchange(url, HttpMethod.GET, request, CurpData.class);
				CurpData curp = response.getBody();

				if (curp != null && curp.getCurp() != null) {
					log.info("Se va a indexar el CURP: " + curp.getCurp() + " Nombre: " + curp.getNombres()
							+ " Apellido: " + curp.getPrimerApellido() + " ID: " + i);
					System.out.println("Se va a indexar el CURP: " + curp.getCurp() + " Nombre: " + curp.getNombres()
							+ " Apellido: " + curp.getPrimerApellido() + " ID: " + i);

					indexCurpData(curp);
				}
			} catch (Exception e) {
				System.out.println("Ocurrió un error al procesar los CURP");
				e.printStackTrace();
			}

	}

	private void indexCurpData(CurpData curpData) throws MalformedURLException, SolrServerException {

		CurpAuthorityValue curpAuth = new CurpAuthorityValue();

		curpAuth.setId(UUID.randomUUID().toString());
		curpAuth.setCurp_id(curpData.getCurp());
		curpAuth.setCreationDate(new Date());
		curpAuth.setLastModified(new Date());

//		if (curpData.getNombres() != null && !curpData.getNombres().isEmpty())
//			curpAuth.setFirstName(StringUtils.capitalize(curpData.getNombres().toLowerCase()));
//
//		if (curpData.getPrimerApellido() != null && !curpData.getPrimerApellido().isEmpty())
//			if (curpData.getSegundoApellido() != null && !curpData.getSegundoApellido().isEmpty())
//				curpAuth.setLastName(StringUtils.capitalize(curpData.getPrimerApellido().toLowerCase()) + " "
//						+ StringUtils.capitalize(curpData.getSegundoApellido().toLowerCase()));
//			else
//				curpAuth.setLastName(StringUtils.capitalize(curpData.getPrimerApellido().toLowerCase()));

		curpAuth.setFirstName(curpData.getNombres());
		curpAuth.setLastName(curpData.getPrimerApellido() + " " + curpData.getSegundoApellido());
		curpAuth.setValue(curpAuth.getLastName() + ", " + curpAuth.getFirstName());
		curpAuth.setField("dc_contributor_author");

		SolrQuery solrQuery = new SolrQuery();
		solrQuery.setQuery("curp_id:" + curpData.getCurp());
		QueryResponse response = solrService.search(solrQuery);

		if (response.getResults().isEmpty())
			indexingService.indexContent(curpAuth, true);

		indexingService.commit();

	}

}
