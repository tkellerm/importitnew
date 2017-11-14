package de.abaspro.infosystem.importit;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import de.abas.eks.jfop.annotation.Stateful;
import de.abas.eks.jfop.remote.FOe;
import de.abas.erp.api.gui.ButtonSet;
import de.abas.erp.api.gui.TextBox;
import de.abas.erp.axi.screen.ScreenControl;
import de.abas.erp.axi2.EventHandlerRunner;
import de.abas.erp.axi2.annotation.ButtonEventHandler;
import de.abas.erp.axi2.annotation.EventHandler;
import de.abas.erp.axi2.annotation.FieldEventHandler;
import de.abas.erp.axi2.annotation.ScreenEventHandler;
import de.abas.erp.axi2.type.ButtonEventType;
import de.abas.erp.axi2.type.FieldEventType;
import de.abas.erp.axi2.type.ScreenEventType;
import de.abas.erp.common.type.enums.EnumDialogBox;
import de.abas.erp.db.DbContext;
import de.abas.erp.db.infosystem.custom.owfw7.InfosystemImportit;
import de.abas.erp.jfop.rt.api.annotation.RunFopWith;
import de.abas.jfop.base.Color;
import de.abas.jfop.base.buffer.BufferFactory;
import de.abas.jfop.base.buffer.EnvBuffer;
import de.abaspro.infosystem.importit.dataprocessing.AbasDataProcessFactory;
import de.abaspro.infosystem.importit.dataprocessing.AbasDataProcessable;
import de.abaspro.infosystem.importit.dataprocessing.EDPSessionHandler;
import de.abaspro.infosystem.importit.dataset.Data;
import de.abaspro.utils.Util;

@Stateful
@EventHandler(head = InfosystemImportit.class, row = InfosystemImportit.Row.class)
@RunFopWith(EventHandlerRunner.class)
public class Main {

	private Logger logger = Logger.getLogger(Main.class);
	static private String HELPFILE = "owfw7/owimportitDocumentation.tar";
	static private String HELPDEST = "win/tmp/owimportitDocumentation";
	static private String HELPDESTTAR = "win/tmp/";

	private ArrayList<Data> dataList;
	private AbasDataProcessable abasDataProcessing;
	private EDPSessionHandler edpSessionhandler = EDPSessionHandler.getInstance();

	@ScreenEventHandler(type = ScreenEventType.ENTER)
	public void screenEnter(InfosystemImportit infosys, ScreenControl screenControl, DbContext ctx) {

		infosys.setYversion("3.0.1");
		fillClientFields(infosys);
		extractHelpTar(ctx);
	}

	@ScreenEventHandler(type = ScreenEventType.END)
	public void screenEnd(InfosystemImportit infosys, ScreenControl screenControl, DbContext ctx) {

		edpSessionhandler.closeAllConnections();
	}

	private void fillClientFields(InfosystemImportit infosys) {
		BufferFactory bufffactory = BufferFactory.newInstance();
		EnvBuffer envBuffer = bufffactory.getEnvBuffer();
		String edpHost = envBuffer.getStringValue("EDPHOST");
		Integer edpPort = envBuffer.getIntegerValue("EDPPORT");
		String mandant = envBuffer.getStringValue("MANDANT");

		infosys.setYserver(edpHost);
		infosys.setYport(edpPort);
		infosys.setYmandant(mandant);

		File file = new File("");
		String abspfad = file.getAbsolutePath();
		infosys.setYmandant(abspfad);
	}

	private void extractHelpTar(DbContext ctx) {
		File destfile = new File(HELPDESTTAR);
		try {

			Util.unTarFile(new File(HELPFILE), destfile);

		} catch (IOException e) {
			logger.error(e);
			showErrorBox(ctx, Util.getMessage("main.docu.extract.error"));
		}
	}

	@FieldEventHandler(field = "ymandant", type = FieldEventType.EXIT)
	public void clientExit(InfosystemImportit infosys, ScreenControl screenControl) {
		String client = infosys.getYeigmandant();
		if (!infosys.getYmandant().equals(client)) {
			screenControl.setColor(infosys, InfosystemImportit.META.ymandant, Color.DEFAULT, Color.RED);
		} else {
			screenControl.setColor(infosys, InfosystemImportit.META.ymandant, Color.DEFAULT, Color.DEFAULT);
		}
	}

	@FieldEventHandler(field = "yoptdeltab", type = FieldEventType.EXIT)
	public void optdeltabExit(DbContext ctx, InfosystemImportit infosys) {
		initOptions(ctx, infosys);
	}

	@FieldEventHandler(field = "yoptnofop", type = FieldEventType.EXIT)
	public void optnofopExit(DbContext ctx, InfosystemImportit infosys) {
		initOptions(ctx, infosys);
	}

	@FieldEventHandler(field = "yoptalwaysnew", type = FieldEventType.EXIT)
	public void optalwaysnewExit(DbContext ctx, InfosystemImportit infosys) {
		initOptions(ctx, infosys);
	}

	@FieldEventHandler(field = "yopttransaction", type = FieldEventType.EXIT)
	public void opttransaction(DbContext ctx, InfosystemImportit infosys) {
		initOptions(ctx, infosys);
	}

	@FieldEventHandler(field = "yoptmodifiable", type = FieldEventType.EXIT)
	public void optmodifiableExit(DbContext ctx, InfosystemImportit infosys) {
		initOptions(ctx, infosys);
	}

	@FieldEventHandler(field = "yoptuseenglvars", type = FieldEventType.EXIT)
	public void optuseenglvarsExit(DbContext ctx, InfosystemImportit infosys) {
		initOptions(ctx, infosys);
	}

	@FieldEventHandler(field = "yoptdontchangeifeq", type = FieldEventType.EXIT)
	public void optdontchangeifeqExit(DbContext ctx, InfosystemImportit infosys) {
		initOptions(ctx, infosys);
	}

	@ButtonEventHandler(field = "ydoku", type = ButtonEventType.AFTER)
	public void dokuAfter(DbContext ctx, InfosystemImportit infosys) {
		File documentationDir = new File(HELPDEST);
		if (documentationDir.exists()) {
			String url = String.format("-FILE %s/Dokumentation.html", documentationDir.getPath());
			FOe.browser(url);
		} else {
			new TextBox(ctx, Util.getMessage("main.exception.title"), Util.getMessage("main.docu.error")).show();
		}
	}

	private void initOptions(DbContext ctx, InfosystemImportit infosys) {
		if (dataListNotEmpty()) {
			OptionCode optionCode = dataList.get(0).getOptionCode();
			optionCode.setOptionCode(infosys.getYoptalwaysnew(), infosys.getYoptnofop(), infosys.getYopttransaction(),
					infosys.getYoptdeltab(), infosys.getYoptmodifiable(), infosys.getYoptuseenglvars(),
					infosys.getYoptdontchangeifeq());
		} else {
			new TextBox(ctx, Util.getMessage("main.exception.title"), Util.getMessage("main.err.no.data.read")).show();
		}

		setOptions(infosys);
	}

	@ButtonEventHandler(field = "yimport", type = ButtonEventType.AFTER)
	public void importData(DbContext ctx, InfosystemImportit infosys) {
		try {
			logger.info(Util.getMessage("info.import.data.start"));
			if (dataListNotEmpty()) {
				if (isTransactionDataList()) {
					logger.info(Util.getMessage("info.transaction.start"));
					// edpProcessing.startTransaction();
					// edpProcessing.importDataListTransaction(dataList);
					abasDataProcessing.startTransaction();
					abasDataProcessing.importDataListTransaction(dataList);
					TextBox textBox = new TextBox(ctx, Util.getMessage("import.data.transaction.box.title"),
							Util.getMessage("import.data.transaction.box.message", getErrorCount()));
					textBox.setButtons(ButtonSet.NO_YES);
					EnumDialogBox answer = textBox.show();
					if (answer.equals(EnumDialogBox.Yes)) {
						logger.info(Util.getMessage("info.transaction.cancel"));
						// edpProcessing.abortTransaction();
						abasDataProcessing.abortTransaction();
					} else {
						logger.info(Util.getMessage("info.transaction.commit"));
					}
				} else {
					logger.info(Util.getMessage("info.no.transaction.import"));
					// edpProcessing.importDataList(dataList);
					abasDataProcessing.importDataList(dataList);
				}
				new TextBox(ctx, Util.getMessage("main.structure.check.box.title"),
						Util.getMessage("info.import.data.success")).show();
			} else {
				new TextBox(ctx, Util.getMessage("main.exception.title"), Util.getMessage("info.import.data.error"));
			}
			logger.info(Util.getMessage("info.import.data.end"));
		} catch (ImportitException e) {
			logger.error(e);
			showErrorBox(ctx, e.getMessage());
		}
		infosys.setYok(getDataCount());
		infosys.setYfehler(getErrorCount());
		if (infosys.getYfehler() == 0) {
			infosys.setYstatus(Util.getMessage("info.import.data.success"));
		} else {
			infosys.setYstatus(Util.getMessage("info.import.data.error"));
		}
	}

	@ButtonEventHandler(field = "yintabladen", type = ButtonEventType.AFTER)
	public void loadTable(DbContext ctx, InfosystemImportit infosys) {
		infosys.table().clear();
		try {
			if (dataListNotEmpty()) {
				for (Data data : dataList) {
					data.createErrorReport();
					String errorReport = data.getErrorReport();
					if (!(errorReport.isEmpty() && infosys.getYshowonlyerrorline())
							|| !infosys.getYshowonlyerrorline()) {
						InfosystemImportit.Row row = infosys.table().appendRow();
						if (data.getTypeCommand() == null) {
							row.setYsel(data.getValueOfKeyField());
							if (data.getAbasId() != null) {
								row.setString(InfosystemImportit.Row.META.ydatensatz, data.getAbasId());
							}
						} else {
							row.setYsel(Util.getMessage("main.load.table.type.command", data.getTypeCommand(),
									dataList.indexOf(data)));
						}
						row.setYimportiert(data.isImported());
						if (errorReport.isEmpty()) {
							row.setYicon("icon:ok");
						} else {
							writeErrorReport(errorReport, row);
						}
					}
				}
			} else {
				if (infosys.getYdatafile().isEmpty()) {
					new TextBox(ctx, Util.getMessage("main.exception.title"),
							Util.getMessage("main.err.load.table.no.file")).show();
				} else {
					new TextBox(ctx, Util.getMessage("main.exception.title"),
							Util.getMessage("main.err.load.table.no.structure.check")).show();
				}
			}
		} catch (Exception e) {
			showErrorBox(ctx, e.getMessage());
		}
	}

	private void writeErrorReport(String errorReport, InfosystemImportit.Row row) throws IOException {
		if (!row.getYimportiert()) {
			row.setYicon("icon:stop");
		} else {
			row.setYicon("icon:attention");
		}
		row.setYfehlerda(true);
		int errorReportLength = errorReport.length();
		int fieldLength = InfosystemImportit.Row.META.ytfehler.getLength();
		if (errorReportLength > fieldLength) {
			row.setYtfehler(errorReport.substring(0, fieldLength));
		} else {
			row.setYtfehler(errorReport);
		}
		StringReader reader = new StringReader(errorReport);
		row.setYkomtext(reader);
	}

	@ButtonEventHandler(field = "ypruefdat", type = ButtonEventType.AFTER)
	public void checkDataAfter(DbContext ctx, InfosystemImportit infosys) {

		logger.debug(Util.getMessage("info.check.data.start"));

		if (dataListNotEmpty()) {
			try {
				if (infosys.getYfehlerstruktur() == 0) {
					// edpProcessing.checkDataListValues(dataList);
					abasDataProcessing.checkDataListValues(dataList);
					infosys.setYok(getDataCount());
					infosys.setYfehlerdatpruef(getErrorCount());
				} else {
					throw new ImportitException(Util.getMessage("main.err.structure.check.error"));
				}
			} catch (ImportitException e) {
				showErrorBox(ctx, e.getMessage());
			}
			if (infosys.getYfehlerdatpruef() == 0) {
				infosys.setYstatus(Util.getMessage("main.check.data.success"));
			} else {
				infosys.setYstatus(Util.getMessage("main.err.check.data"));
			}
			new TextBox(ctx, Util.getMessage("main.structure.check.box.title"),
					Util.getMessage("main.data.check.box.message")).show();
		} else {
			showErrorBox(ctx, Util.getMessage("error.data.check.datalist.empty"));
		}
		logger.debug(Util.getMessage("info.check.data.end"));
	}

	private int getDataCount() {
		int okay = 0;
		if (dataListNotEmpty()) {
			for (Data data : dataList) {
				if (data.isImported()) {
					okay++;
				}
			}
		}
		return okay;
	}

	@ButtonEventHandler(field = "ypruefstrukt", type = ButtonEventType.AFTER)
	public void checkStructureAfter(DbContext ctx, InfosystemImportit infosys) {
		infosys.table().clear();
		infosys.setYok(0);
		infosys.setYfehler(0);
		infosys.setYfehlerdatpruef(0);
		infosys.setYfehlerstruktur(0);
		infosys.setYdb("");
		infosys.setYgruppe("");
		infosys.setYtippkommando("");
		try {

			this.edpSessionhandler.initSession(infosys.getYserver(), infosys.getYport(), infosys.getYmandant(),
					infosys.getYpasswort());

			logger.info(Util.getMessage("info.structure.check.start.processing"));
			ExcelImportProcessing excelProcessing = new ExcelImportProcessing(infosys.getYdatafile());
			dataList = excelProcessing.getDataList();
			logger.info(Util.getMessage("info.structure.check.end.processing"));
			logger.info(Util.getMessage("info.structure.check.start.data"));

			abasDataProcessing = new AbasDataProcessFactory().createAbasDataProcess(this.edpSessionhandler, dataList);
			abasDataProcessing.checkDataListStructure(dataList);

			logger.info(Util.getMessage("info.structure.check.end.data"));
			infosys.setYfehlerstruktur(getErrorCount());
			showDatabaseInfo(infosys);
			setOptions(infosys);
			if (infosys.getYfehlerstruktur() == 0) {
				infosys.setYstatus(Util.getMessage("main.status.structure.check.success"));
			} else {
				infosys.setYstatus(Util.getMessage("main.status.structure.check.error"));
			}
			new TextBox(ctx, Util.getMessage("main.structure.check.box.title"),
					Util.getMessage("main.structure.check.box.message")).show();

		} catch (ImportitException e) {
			showErrorBox(ctx, e.getMessage());
		}
	}

	private void showErrorBox(DbContext ctx, String message) {
		new TextBox(ctx, Util.getMessage("main.exception.title"), message).show();
	}

	private void setOptions(InfosystemImportit infosys) {
		if (dataListNotEmpty()) {
			Data data = dataList.get(0);
			OptionCode optionCode = data.getOptionCode();
			infosys.setYoptalwaysnew(optionCode.getAlwaysNew());
			infosys.setYoptnofop(optionCode.noFop());
			infosys.setYoptmodifiable(optionCode.getCheckFieldIsModifiable());
			infosys.setYoptdeltab(optionCode.getDeleteTable());
			infosys.setYopttransaction(optionCode.getInOneTransaction());
			infosys.setYoptuseenglvars(optionCode.useEnglishVariables());
			infosys.setYoptdontchangeifeq(optionCode.getDontChangeIfEqual());
			infosys.setYoption(optionCode.getOptionsCode());
		} else {
			infosys.setYoptalwaysnew(false);
			infosys.setYoptnofop(false);
			infosys.setYoptmodifiable(false);
			infosys.setYoptdeltab(false);
			infosys.setYopttransaction(false);
			infosys.setYoptuseenglvars(false);
			infosys.setYoptdontchangeifeq(false);
			infosys.setYoption(0);
		}
	}

	private void showDatabaseInfo(InfosystemImportit infosys) {
		if (dataListNotEmpty()) {
			Data data = dataList.get(0);
			if (data.getDatabase() != null) {
				infosys.setYdb(data.getDatabase().toString());
			}
			if (data.getGroup() != null) {
				infosys.setYgruppe(data.getGroup().toString());
			}
			if (data.getTypeCommand() != null) {
				infosys.setYtippkommando(data.getTypeCommand().toString());
			}
			infosys.setYtababspalte(data.getTableStartsAtField() + 1);
		}
	}

	private int getErrorCount() {
		int numberOfError = 0;
		if (dataListNotEmpty()) {
			for (Data data : dataList) {
				data.createErrorReport();
				String error = data.getErrorReport();
				if (error != null && !error.isEmpty()) {
					numberOfError++;
				}
			}
		}
		return numberOfError;
	}

	private boolean dataListNotEmpty() {
		return dataList != null && dataList.size() > 0;
	}

	public boolean isTransactionDataList() throws ImportitException {
		if (dataListNotEmpty()) {
			return dataList.get(0).getOptionTransaction();
		}
		throw new ImportitException(Util.getMessage("main.err.no.data.read"));
	}
}
