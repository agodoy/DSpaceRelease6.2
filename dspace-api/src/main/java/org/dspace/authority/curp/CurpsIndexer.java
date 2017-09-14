package org.dspace.authority.curp;

import org.apache.log4j.Logger;
import org.dspace.authority.factory.AuthorityServiceFactory;

public final class CurpsIndexer {

	private static Logger log = Logger.getLogger(CurpsIndexer.class);

	public static void main(String[] args) {

		log.info("Inicia procesamiento de CURPs.");
		System.out.println("Inicia procesamiento de CURPs.");
		
		CurpService curpsService = AuthorityServiceFactory.getInstance().getCurpsService();
		try {
			curpsService.indexCurps();
		} catch (Exception e) {
			log.info("Procesamiento de CURPs finalizado con errores.");
			System.out.println("Procesamiento de CURPs finalizado con errores.");
		}

		log.info("Procesamiento de CURPs finalizado.");
		System.out.println("Procesamiento de CURPs finalizado.");

	}

}
