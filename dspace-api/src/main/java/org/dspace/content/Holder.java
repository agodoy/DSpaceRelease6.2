package org.dspace.content;

public class Holder extends DSpaceObject implements DSpaceObjectLegacySupport{
	
	private String correo;
	private String nombre;
	private String numTel;
	private String pApellido;
	private String sApellido;
	
	public String getCorreo() {
		return correo;
	}
	public void setCorreo(String correo) {
		this.correo = correo;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public String getNumTel() {
		return numTel;
	}
	public void setNumTel(String numTel) {
		this.numTel = numTel;
	}
	public String getpApellido() {
		return pApellido;
	}
	public void setpApellido(String pApellido) {
		this.pApellido = pApellido;
	}
	public String getsApellido() {
		return sApellido;
	}
	public void setsApellido(String sApellido) {
		this.sApellido = sApellido;
	}
	@Override
	public Integer getLegacyId() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public int getType() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}
	

}
