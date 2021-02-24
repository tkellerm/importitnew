package de.abas.infosystem.importit;

import de.abas.ceks.jedp.EDPQuery;
import de.abas.utils.MessageUtil;

public class VartabField {

    private static final String VAR_NAME = "varName";
    private static final String VAR_NAME_NEW = "varNameNew";
    private static final String VAR_NAME_ENGLISH = "varNameEnglish";
    private static final String VAR_TYPE_NEW = "varTypeNew";
    private static final String VAR_TYPE = "varType";

    private String varName;
    private final String varNameNew;
    private final String varNameEnglish;
    private final String varTypeNew;
    private final String varType;

    public VartabField(EDPQuery query) {
        if (query != null) {
            String varNameString = query.getField(VAR_NAME);
            if (varNameString.length() > 0) {
                String varNameShort = varNameString.substring(2);
                this.varName = varNameShort;
            }
            String varNameNewString = query.getField(VAR_NAME_NEW);
            if (varNameNewString.length() > 0) {
                String varNameNewShort = varNameNewString.substring(2);
                this.varNameNew = varNameNewShort;
            } else {
                this.varNameNew = "";
            }
            this.varNameEnglish = query.getField(VAR_NAME_ENGLISH);
            this.varType = query.getField(VAR_TYPE);
            this.varTypeNew = query.getField(VAR_TYPE_NEW);
        } else throw new NullPointerException(MessageUtil.getMessage("vartab.field.query.not.defined"));
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
