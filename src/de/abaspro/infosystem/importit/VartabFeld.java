package de.abaspro.infosystem.importit;

import de.abas.ceks.jedp.EDPQuery;

public class VartabFeld {
	
	 static private String varNameVarName = "varName";
	 static private String varNameVarNameNew = "varNameNew";
	 static private String varNameVarNameEnglish = "varNameEnglish"; 
	 static private String varNameVarTypeNew = "varTypeNew";
	 static private String varNameVarType = "varType";
	 static private String varNameVarLength = "varLengthExt";
	 static private String varNameVarInTab = "inTab";
	
	
	
	private String varName; 
	private String varNameNew; 
	private String varNameEnglish; 
	private String varTypeNew; 
	private String varType; 
	private String varLength; 
	private Boolean varInTab;
	
	public VartabFeld(EDPQuery query) {
		if (query != null) {
			
			String varname = query.getField(varNameVarName);
			if (varname.length() > 0) {
				String varnamekurz = varname.substring(2);
				this.varName = varnamekurz;	
			}
			
			String varnamenew = query.getField(varNameVarNameNew);
			if (varnamenew.length() > 0) {
				String varnameNewkurz = varnamenew.substring(2);
				this.varNameNew = varnameNewkurz;
			}else {
				this.varNameNew = "";
			}
			
			this.varNameEnglish = query.getField(varNameVarNameEnglish);
			this.varType = query.getField(varNameVarType);
			this.varTypeNew = query.getField(varNameVarTypeNew);
			this.varLength = query.getField(varNameVarLength);
			String testlength = query.getField("varLengthExt");
			String inTab = query.getField(varNameVarInTab);
			if (inTab.equals("ja")||inTab.equals("yes")||inTab.equals("1") ) {
				this.varInTab = true;
			}else {
				this.varInTab = false;
			}
				
		}else throw new NullPointerException("Das Query-Object war nicht definiert");	
	}

	public String getVarNameEnglish() {
		return varNameEnglish;
	}

	public String getAktivType() {
		
		if (this.varTypeNew.length()>0) {
			return this.varTypeNew;	
		}else {
			return this.varType;	
		}	
	}

	public String getVarLength() {
		return varLength;
	}

	public Boolean getVarInTab() {
		return varInTab;
	}

	public String getAktivVarName() {
		
		if (!this.varNameNew.isEmpty()) {
			return this.varNameNew;
		}else {
			return this.varName;
		}		
	}
	
	
	
	
}
