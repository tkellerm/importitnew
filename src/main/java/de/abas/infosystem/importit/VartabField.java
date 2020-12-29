package de.abas.infosystem.importit;

import de.abas.ceks.jedp.EDPQuery;
import de.abas.utils.Util;

public class VartabField {

    private static String varNameVarName = "varName";
    private static String varNameVarNameNew = "varNameNew";
    private static String varNameVarNameEnglish = "varNameEnglish";
    private static String varNameVarTypeNew = "varTypeNew";
    private static String varNameVarType = "varType";

    private String varName;
    private String varNameNew;
    private String varNameEnglish;
    private String varTypeNew;
    private String varType;

    public VartabField(EDPQuery query) {
        if (query != null) {
            String varName = query.getField(varNameVarName);
            if (varName.length() > 0) {
                String varNameShort = varName.substring(2);
                this.varName = varNameShort;
            }
            String varNameNew = query.getField(varNameVarNameNew);
            if (varNameNew.length() > 0) {
                String varNameNewShort = varNameNew.substring(2);
                this.varNameNew = varNameNewShort;
            } else {
                this.varNameNew = "";
            }
            this.varNameEnglish = query.getField(varNameVarNameEnglish);
            this.varType = query.getField(varNameVarType);
            this.varTypeNew = query.getField(varNameVarTypeNew);
        } else throw new NullPointerException(Util.getMessage("vartab.field.query.not.defined"));
    }

    public String getVarNameEnglish() {
        return varNameEnglish;
    }

    public String getActiveType() {
        if (this.varTypeNew.length() > 0) {
            return this.varTypeNew;
        } else {
            return this.varType;
        }
    }

    public String getActiveVarName() {
        if (!this.varNameNew.isEmpty()) {
            return this.varNameNew;
        } else {
            return this.varName;
        }
    }

}
