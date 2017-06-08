package de.abaspro.infosystem.importit;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.abas.ceks.jedp.CantBeginEditException;
import de.abas.ceks.jedp.CantBeginSessionException;
import de.abas.ceks.jedp.EDPConstants;
import de.abas.ceks.jedp.EDPEditor;
import de.abas.ceks.jedp.EDPFactory;
import de.abas.ceks.jedp.EDPQuery;
import de.abas.ceks.jedp.EDPSession;
import de.abas.ceks.jedp.EDPVariableLanguage;
import de.abas.ceks.jedp.InvalidQueryException;
import de.abas.ceks.jedp.InvalidSettingValueException;
import de.abas.eks.jfop.remote.FOe;
import de.abas.erp.common.type.enums.EnumTypeCommands;
import de.abas.jfop.base.buffer.BufferFactory;
import de.abas.jfop.base.buffer.GlobalTextBuffer;
import de.abas.jfop.base.buffer.UserTextBuffer;
import de.abaspro.utils.Util;

public class AbasDataCheckAndComplete {
	
	private static String edpLogFile = "java/log/importit21edp.log";
    private String server;
    private Integer port;
    private String client;
    private String password;
    private EDPSession edpSession;
	private Logger logger = Logger.getLogger(Main.class);
	private ArrayList<Data> dataList;

	public AbasDataCheckAndComplete(String server, Integer port, String client, String password , ArrayList<Data> dataList) throws ImportitException {
		
	this.edpSession = EDPFactory.createEDPSession();
	this.server = server;
	this.port = port;
	this.client = client;
	this.password = password;
	this.dataList = dataList;
	}
	
	public boolean checkandcompleteDataList() throws ImportitException {
	try {
        startEdpSession(EDPVariableLanguage.ENGLISH);
        Data data = dataList.get(0);
        if (data != null) {
            if (checkandcompleteData(data)) {
            	List<Field> smlFieldList = createDefaultSMLFieldList(data);
            
                for (Data dataset : dataList) {
                    dataset.copyDatabase(data);
                    dataset.copyAbasType(data);
                    
                    if (smlFieldList != null) {
						if (!dataset.getSmlString().isEmpty()) {
							fillSmlArray(dataset , smlFieldList);
							deleteSmlFieldfromHeaderFields(dataset);
						} 
					}
                }
                return true;
            }else return false;
        }
    } finally {
        closeEdpSession(this.edpSession);
    }
	return false;
		
		
	}
	
	

	private List<Field> createDefaultSMLFieldList(Data data) {
		if (data.getSmlString() != null) {
			List<Field> smlFieldlist = null;
	        String fieldNames = "vdn,vgr,grpDBDescr,grpGrpNo";
	        String tableName = "12:24";
	        String key= "Nummer";
	        Boolean inTable = false;
	        int aliveFlag = 1;
	        EDPQuery query = this.edpSession.createQuery();
	        String criteria = "nummer=" + data.getSmlString() + ";@englvar=true;@language=en";
	        
			try {
				query.startQuery(tableName, key, criteria, inTable, aliveFlag, true, true, fieldNames, 0, 10000);
			} catch (InvalidQueryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//          query.getLastRecord();
	        
	        
			return smlFieldlist;
		}else {
			return null;	
		}
		
	}

	private void fillSmlArray(Data dataset, List<Field> smlFieldList) {
		String smlString = dataset.getSmlString();
		
//		Suche nach SML in abas
//		String key = "";
//        
//        String fieldNames = "vdn,vgr,grpDBDescr,grpGrpNo";
//        String tableName = "12:24";
//        Boolean inTable = false;
//        int mode = EDPConstants.ENUMPOS_CODE;
//        try {
//            this.edpSession.setEnumMode(mode);
//        } catch (InvalidSettingValueException e) {
//            logger.error(e);
//        }
//        EDPQuery query = this.edpSession.createQuery();
//        try {
//            query.startQuery(tableName, key, criteria, inTable, aliveFlag, true, true, fieldNames, 0, 10000);
//            query.getLastRecord();
//            if (query.getRecordCount() == 1) {
//                String dbString = query.getField("grpDBDescr");
//                dbString = dbString.replaceAll("\\(*", "");
//                dbString = dbString.replaceAll("\\)*", "");
//                data.setDatabase(new Integer(dbString));
//                String group = query.getField("grpGrpNo");
//                group = group.replaceAll(" ", "");
//                data.setGroup(new Integer(group));
//                
//            }
//		Suche nach den Checke ob die verwendeten Felder in SML vorhanden.
//		Wenn ja dann nehme Sie in SML-Array auf 
		
		
		
	}

	private void deleteSmlFieldfromHeaderFields(Data dataset) {
		// TODO Auto-generated method stub
		
	}

	private void moveSmlFieldsfromHeaderArrayToSmlArray(Data dataset) {
		List<Field> headerFields = dataset.getHeaderFields();
		List<Field> smlFields =  dataset.getSmlFields();
		for (Field field : smlFields) {
			String completeContent = field.getCompleteContent();
			if (completeContent.startsWith("S.")) {
				smlFields.add(field);
				headerFields.remove(field);
			}
		}
	}

	private boolean checkandcompleteData(Data data) {
        Boolean exists = false;
        if (data.getTypeCommand() == null && data.getTypeCommandString() != null) {
            data.setTypeCommand(checkTypeCommandString(data.getTypeCommandString()));
        }
        if (data.getTypeCommand() != null && data.getDatabase() == null) {
            EnumTypeCommands[] typeCommands = EnumTypeCommands.values();
            for (EnumTypeCommands enumTypeCommands : typeCommands) {
                if (data.getTypeCommand() == enumTypeCommands.getCode()) {
                    exists = true;
                }

            }
            if (exists) {
                try {
                    findDatabaseForTypeCommand(data);
                } catch (ImportitException e) {
                    data.appendError(e);
                }
            } else {
                data.appendError(Util.getMessage("err.invalid.type.command", data.getTypeCommand()));
            }
        }
        exists = checkDatabaseName(data);
        return exists;
    }
	
	private void findDatabaseForTypeCommand(Data data) throws ImportitException {
        if (data.getTypeCommand() != null) {
            startEdpSession(EDPVariableLanguage.ENGLISH);
            if (this.edpSession != null) {
                EDPEditor edpEditor = this.edpSession.createEditor();
                try {
                    edpEditor.beginEditCmd(data.getTypeCommand().toString(), "");
                    data.setDatabase(edpEditor.getEditDatabaseNr());
                    data.setGroup(edpEditor.getEditGroupNr());
                    edpEditor.endEditCancel();
                } catch (CantBeginEditException e) {
                    throw new ImportitException(Util.getMessage("err.getting.database", e));
                }
            }
        }
    }
	
	private Integer checkTypeCommandString(String typeCommandString) {
        Integer typeCommandCode = null;
        Enumeration enumeration = fillEnumeration();
        if (enumeration.getListOfEnumItems().size() > 0) {
            EnumerationItem enumerationItem = enumeration.searchItem(typeCommandString);
            typeCommandCode = enumerationItem.getNumber();
        }
        return typeCommandCode;
    }
	
	private Boolean checkDatabaseName(Data data) {
        if (data.getDatabase() != null && data.getGroup() != null) {
            String criteria = "0:grpDBDescr=("
                    + data.getDatabase().toString() + ");0:grpGrpNo="
                    + data.getGroup().toString() + ";"
                    + ";@englvar=true;@language=en";
            if (searchDatabase(data, criteria)) {
                return true;
            } else {
                data.appendError(Util.getMessage("err.invalid.database.group", data.getDatabase(), data.getGroup()));
                return false;
            }
        } else {
            if (data.getDbString() != null && data.getDbGroupString() != null) {
                String criteria = "0:vdntxt==" + data.getDbString()
                        + ";0:vgrtxtbspr=="
                        + data.getDbGroupString() + ";"
                        + ";@englvar=false;@language=de";
                if (searchDatabase(data, criteria)) {
                    return true;
                } else {
                    criteria = "0:DBCmd==" + data.getDbString()
                            + ";0:grpGrpCmd==" + data.getDbGroupString()
                            + ";" + ";@englvar=true;@language=en";
                    if (searchDatabase(data, criteria)) {
                        return true;
                    } else {
                        data.appendError(Util.getMessage("err.invalid.database.group", data.getDatabase(), data.getGroup()));
                        return false;
                    }
                }
            }
        }
        return false;
    }
	
	private boolean searchDatabase(Data data, String criteria) {
        String key = "";
        int aliveFlag = EDPConstants.ALIVEFLAG_ALIVE;
        String fieldNames = "vdn,vgr,grpDBDescr,grpGrpNo";
        String tableName = "12:26";
        Boolean inTable = false;
        int mode = EDPConstants.ENUMPOS_CODE;
        try {
            this.edpSession.setEnumMode(mode);
        } catch (InvalidSettingValueException e) {
            logger.error(e);
        }
        EDPQuery query = this.edpSession.createQuery();
        try {
            query.startQuery(tableName, key, criteria, inTable, aliveFlag, true, true, fieldNames, 0, 10000);
            query.getLastRecord();
            if (query.getRecordCount() == 1) {
                String dbString = query.getField("grpDBDescr");
                dbString = dbString.replaceAll("\\(*", "");
                dbString = dbString.replaceAll("\\)*", "");
                data.setDatabase(new Integer(dbString));
                String group = query.getField("grpGrpNo");
                group = group.replaceAll(" ", "");
                data.setGroup(new Integer(group));
                return true;
            }
        } catch (InvalidQueryException e) {
            data.appendError(Util.getMessage("err.invalid.selection.criteria", criteria, e));
            return false;
        }
        return false;
    }
		
	
	private Enumeration fillEnumeration() {
        Enumeration enumeration = new Enumeration();
        ArrayList<EnumerationItem> listOfEnumItems = enumeration.getListOfEnumItems();
        BufferFactory bufferFactory = BufferFactory.newInstance(true);
        GlobalTextBuffer globalTextbuffer = bufferFactory.getGlobalTextBuffer();
        UserTextBuffer userTextBuffer = bufferFactory.getUserTextBuffer();
        int cmdNameMax = globalTextbuffer.getIntegerValue("cmdNameMax");
        for (Integer i = 0; i < cmdNameMax; i++) {
            String descrVar = "xtnamebspr";
            String neutralNameVar = "xtnameneutral";
            String enumerationVar = "xtaufzaehlung";
            String descr;
            String neutralName;
            if (!userTextBuffer.isVarDefined(descrVar)) {
                userTextBuffer.defineVar("Text", descrVar);
            }
            if (!userTextBuffer.isVarDefined(neutralNameVar)) {
                userTextBuffer.defineVar("Text", neutralNameVar);
            }
            if (!userTextBuffer.isVarDefined(enumerationVar)) {
                userTextBuffer.defineVar("A198", enumerationVar);
            }
            FOe.assign("U|" + enumerationVar + " = \"(" + i + ")\"");
            Boolean success = globalTextbuffer.getBooleanValue("success");
            if (success) {
                FOe.formula("U|" + neutralNameVar + " = 'U|" + enumerationVar + "(L=\":\")'");
                neutralName = userTextBuffer.getStringValue(neutralNameVar);
                descr = globalTextbuffer.getStringValue("cmdName" + i);
                EnumerationItem enumerationItem = new EnumerationItem(i, descr, neutralName);
                listOfEnumItems.add(enumerationItem);
            }
        }
        return enumeration;
    }

	private void startEdpSession(EDPVariableLanguage english) throws ImportitException {
		try {
			this.edpSession.beginSession(server, port , client, password, "ImportIt_21_CheckDatabase");
			this.edpSession.loggingOn(edpLogFile);
			this.edpSession.setVariableLanguage(EDPVariableLanguage.ENGLISH);
		} catch (CantBeginSessionException e) {
	        logger.error(e);
	        throw new ImportitException(Util.getMessage("err.edp.session.start", e));
	    } catch (Exception e) {
	        logger.error(e);
	        throw new ImportitException(Util.getMessage("err.edp.session.start", e));
	    }
		
	}
	
	private void closeEdpSession(EDPSession edpSession) {
        if (edpSession.isConnected()) {
            edpSession.endSession();
            logger.info(Util.getMessage("info.edp.session.closed", edpSession.getSessionTag()));
        } else {
            logger.error(Util.getMessage("err.edp.session.lost", edpSession.getSessionTag()));
        }
    }

}
