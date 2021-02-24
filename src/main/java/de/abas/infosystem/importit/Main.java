package de.abas.infosystem.importit;

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
import de.abas.erp.db.infosystem.custom.owimportit.InfosystemImportit;
import de.abas.erp.jfop.rt.api.annotation.RunFopWith;
import de.abas.infosystem.importit.dataprocessing.AbasDataProcessFactory;
import de.abas.infosystem.importit.dataprocessing.AbasDataProcessable;
import de.abas.infosystem.importit.dataprocessing.EDPSessionHandler;
import de.abas.infosystem.importit.dataset.Data;
import de.abas.jfop.base.AbasColors;
import de.abas.jfop.base.Color;
import de.abas.jfop.base.buffer.BufferFactory;
import de.abas.jfop.base.buffer.EnvBuffer;
import de.abas.utils.BuildVersion;
import de.abas.utils.MessageUtil;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;


@Stateful
@EventHandler(head = InfosystemImportit.class, row = InfosystemImportit.Row.class)
@RunFopWith(EventHandlerRunner.class)
public class Main implements ProgressListener {

	private static final String DOCUMENTATION_URL = "https://documentation.abas.cloud/de/importit/";
	public static final String MAIN_EXCEPTION_TITLE = "main.exception.title";
	public static final String MAIN_STRUCTURE_CHECK_BOX_TITLE = "main.structure.check.box.title";
	private final Logger logger = Logger.getLogger(Main.class);
	private boolean showProgress;
	private List<Data> dataList;
	private AbasDataProcessable abasDataProcessing;
	private final EDPSessionHandler edpSessionhandler = EDPSessionHandler.getInstance();
	private ScreenControl screenControl;

	@ScreenEventHandler(type = ScreenEventType.ENTER)
	public void screenEnter(InfosystemImportit infosystem, ScreenControl screenControl, DbContext ctx) {

		infosystem.setYversion(BuildVersion.getBuildVersion());
		fillClientFields(infosystem);
		protectOptionFields(infosystem, screenControl, true);
		//TODO remove variable showprogress
		this.showProgress = infosystem.getYwithprogress();
		this.screenControl = screenControl;

	}

	private void protectOptionFields(InfosystemImportit infosystem, ScreenControl screenControl, Boolean protect) {

		screenControl.setProtection(infosystem, InfosystemImportit.META.yoptalwaysnew, protect);
		screenControl.setProtection(infosystem, InfosystemImportit.META.yoptnofop, protect);
		screenControl.setProtection(infosystem, InfosystemImportit.META.yoptdontchangeifeq, protect);
		screenControl.setProtection(infosystem, InfosystemImportit.META.yoptdeltab, protect);
		screenControl.setProtection(infosystem, InfosystemImportit.META.yoptmodifiable, protect);
		screenControl.setProtection(infosystem, InfosystemImportit.META.yoptuseenglvars, protect);

	}

	@ScreenEventHandler(type = ScreenEventType.END)
	public void screenEnd(InfosystemImportit infosystem, ScreenControl screenControl, DbContext ctx) {

		edpSessionhandler.closeAllConnections();
	}

	private void fillClientFields(InfosystemImportit infosystem) {
		BufferFactory bufferFactory = BufferFactory.newInstance();
		EnvBuffer envBuffer = bufferFactory.getEnvBuffer();
		String edpHost = envBuffer.getStringValue("EDPHOST");
		int edpPort = envBuffer.getIntegerValue("EDPPORT");
		String mandant = envBuffer.getStringValue("MANDANT");

		infosystem.setYserver(edpHost);
		infosystem.setYport(edpPort);
		infosystem.setYmandant(mandant);

		// TODO Change to MANDANTDIR
		File file = new File("");
		String absolutePath = file.getAbsolutePath();
		infosystem.setYmandant(absolutePath);
	}



	@FieldEventHandler(field = "ymandant", type = FieldEventType.EXIT)
	public void clientExit(InfosystemImportit infosystem, ScreenControl screenControl) {
		String client = infosystem.getYeigmandant();
		if (!infosystem.getYmandant().equals(client)) {
			screenControl.setColor(infosystem, InfosystemImportit.META.ymandant, Color.DEFAULT, AbasColors.RED);
		} else {
			screenControl.setColor(infosystem, InfosystemImportit.META.ymandant, Color.DEFAULT, Color.DEFAULT);
		}
	}

	@FieldEventHandler(field = "yoptdeltab", type = FieldEventType.EXIT)
	public void optdeltabExit(DbContext ctx, InfosystemImportit infosystem) {
		initOptions(ctx, infosystem);
	}

	@FieldEventHandler(field = "yoptnofop", type = FieldEventType.EXIT)
	public void optnofopExit(DbContext ctx, InfosystemImportit infosystem) {
		initOptions(ctx, infosystem);
	}

	@FieldEventHandler(field = "yoptalwaysnew", type = FieldEventType.EXIT)
	public void optalwaysnewExit(DbContext ctx, InfosystemImportit infosystem) {
		initOptions(ctx, infosystem);
	}

	@FieldEventHandler(field = "yopttransaction", type = FieldEventType.EXIT)
	public void opttransaction(DbContext ctx, InfosystemImportit infosystem) {
		initOptions(ctx, infosystem);
	}

	@FieldEventHandler(field = "yoptmodifiable", type = FieldEventType.EXIT)
	public void optmodifiableExit(DbContext ctx, InfosystemImportit infosystem) {
		initOptions(ctx, infosystem);
	}

	@FieldEventHandler(field = "yoptuseenglvars", type = FieldEventType.EXIT)
	public void optuseenglvarsExit(DbContext ctx, InfosystemImportit infosystem) {
		initOptions(ctx, infosystem);
	}

	@FieldEventHandler(field = "yoptdontchangeifeq", type = FieldEventType.EXIT)
	public void optdontchangeifeqExit(DbContext ctx, InfosystemImportit infosystem) {
		initOptions(ctx, infosystem);
	}

	@ButtonEventHandler(field = "ydoku", type = ButtonEventType.AFTER)
	public void dokuAfter(DbContext ctx, InfosystemImportit infosystem) {
		FOe.browser(DOCUMENTATION_URL);
	}

	private void initOptions(DbContext ctx, InfosystemImportit infosystem) {
		if (dataListNotEmpty()) {
			OptionCode optionCode = dataList.get(0).getOptionCode();
			optionCode.setOptionCode(infosystem.getYoptalwaysnew(), infosystem.getYoptnofop(), infosystem.getYopttransaction(),
					infosystem.getYoptdeltab(), infosystem.getYoptmodifiable(), infosystem.getYoptuseenglvars(),
					infosystem.getYoptdontchangeifeq());
		} else {
			new TextBox(ctx, MessageUtil.getMessage(MAIN_EXCEPTION_TITLE), MessageUtil.getMessage("main.err.no.data.read")).show();
		}

		setOptions(infosystem);
	}

	@ButtonEventHandler(field = "yimport", type = ButtonEventType.AFTER)
	public void importData(DbContext ctx, ScreenControl screenControl, InfosystemImportit infosystem) {
		try {
			infosystem.table().clear();
			startEdpSessionHandler(infosystem);
			logger.info(MessageUtil.getMessage("info.import.data.start"));
			if (dataListNotEmpty()) {
				if (isTransactionDataList()) {
					logger.info(MessageUtil.getMessage("info.transaction.start"));
					try {
						runImportWithTransaction(ctx);
					} finally {
						if (abasDataProcessing.isTransactionActive()){
							abasDataProcessing.abortTransaction();
						}
					}


				} else {
					logger.info(MessageUtil.getMessage("info.no.transaction.import"));
					if (this.showProgress) {
						abasDataProcessing.addListener(this);
					}
					abasDataProcessing.importDataList(dataList);
				}
				new TextBox(ctx, MessageUtil.getMessage(MAIN_STRUCTURE_CHECK_BOX_TITLE),
						MessageUtil.getMessage("info.import.data.success")).show();
			} else {
				new TextBox(ctx, MessageUtil.getMessage(MAIN_EXCEPTION_TITLE), MessageUtil.getMessage("info.import.data.error"));
			}
			logger.info(MessageUtil.getMessage("info.import.data.end"));
		} catch (ImportitException e) {
			logger.error(e);
			showErrorBox(ctx, e.getMessage());
		}
		infosystem.setYok(getDataCount());
		infosystem.setYfehler(getErrorCount());
		if (infosystem.getYfehler() == 0) {
			infosystem.setYstatus(MessageUtil.getMessage("info.import.data.success"));
		} else {
			infosystem.setYstatus(MessageUtil.getMessage("info.import.data.error"));
		}
		edpSessionhandler.closeAllConnections();
	}

	private void runImportWithTransaction(DbContext ctx) throws ImportitException {

			abasDataProcessing.startTransaction();
			abasDataProcessing.importDataListTransaction(dataList);
			TextBox textBox = new TextBox(ctx, MessageUtil.getMessage("import.data.transaction.box.title"),
					MessageUtil.getMessage("import.data.transaction.box.message", getErrorCount()));
			textBox.setButtons(ButtonSet.NO_YES);
			EnumDialogBox answer = textBox.show();
			if (answer.equals(EnumDialogBox.Yes)) {
				logger.info(MessageUtil.getMessage("info.transaction.cancel"));

				abasDataProcessing.abortTransaction();
			} else {
				logger.info(MessageUtil.getMessage("info.transaction.commit"));
				abasDataProcessing.commitTransaction();
			}

	}

	@ButtonEventHandler(field = "yintabladen", type = ButtonEventType.AFTER)
	public void loadTable(DbContext ctx, InfosystemImportit infosystem) {
		infosystem.table().clear();
		try {
			if (dataListNotEmpty()) {
				for (Data data : dataList) {
					data.createErrorReport();
					String errorReport = data.getErrorReport();
					if (!(errorReport.isEmpty() && infosystem.getYshowonlyerrorline())
							|| !infosystem.getYshowonlyerrorline()) {
						insertRowInTable(infosystem, data, errorReport);
					}
				}
			} else {
				if (infosystem.getYdatafile().isEmpty()) {
					new TextBox(ctx, MessageUtil.getMessage(MAIN_EXCEPTION_TITLE),
							MessageUtil.getMessage("main.err.load.table.no.file")).show();
				} else {
					new TextBox(ctx, MessageUtil.getMessage(MAIN_EXCEPTION_TITLE),
							MessageUtil.getMessage("main.err.load.table.no.structure.check")).show();
				}

			}
		} catch (Exception e) {
			showErrorBox(ctx, e.getMessage());
		}
	}

	private void insertRowInTable(InfosystemImportit infosystem, Data data, String errorReport) throws ImportitException, IOException {
		InfosystemImportit.Row row = infosystem.table().appendRow();
		if (data.getTypeCommand() == null) {
			row.setYsel(data.getValueOfKeyField());
			if (data.getAbasId() != null) {
				row.setString(InfosystemImportit.Row.META.ydatensatz, data.getAbasId());
			}
		} else {
			row.setYsel(MessageUtil.getMessage("main.load.table.type.command", data.getTypeCommand(),
					dataList.indexOf(data)));
		}

		row.setYimportiert(data.isImported());

		if (errorReport.isEmpty()) {
			row.setYicon("icon:ok");
		} else {
			writeErrorReport(errorReport, row);
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
	public void checkDataAfter(DbContext ctx, InfosystemImportit infosystem) {
		infosystem.table().clear();
		try {
			startEdpSessionHandler(infosystem);
			logger.debug(MessageUtil.getMessage("info.check.data.start"));

			if (dataListNotEmpty() && abasDataProcessing != null) {

					if (infosystem.getYfehlerstruktur() == 0) {

						if (this.showProgress) {
								abasDataProcessing.addListener(this);
						}

						abasDataProcessing.checkDataListValues(dataList);

						infosystem.setYok(getDataCount());
						infosystem.setYfehlerdatpruef(getErrorCount());
					} else {
						throw new ImportitException(MessageUtil.getMessage("main.err.structure.check.error"));
					}

				if (infosystem.getYfehlerdatpruef() == 0) {
					infosystem.setYstatus(MessageUtil.getMessage("main.check.data.success"));
				} else {
					infosystem.setYstatus(MessageUtil.getMessage("main.err.check.data"));
				}
				new TextBox(ctx, MessageUtil.getMessage(MAIN_STRUCTURE_CHECK_BOX_TITLE),
						MessageUtil.getMessage("main.data.check.box.message")).show();
			} else {
				showErrorBox(ctx, MessageUtil.getMessage("error.data.check.datalist.empty"));
			}
			logger.debug(MessageUtil.getMessage("info.check.data.end"));


		} catch (ImportitException e1) {
			showErrorBox(ctx, e1.getMessage());
			logger.error(e1);
		} finally {
			this.edpSessionhandler.closeAllConnections();
		}

	}

	private int getDataCount() {
		int okay = 0;
		if (dataListNotEmpty()) {
			for (Data data : dataList) {
				if (Boolean.TRUE.equals(data.isImported())) {
					okay++;
				}
			}
		}
		return okay;
	}

	@ButtonEventHandler(field = "ypruefstrukt", type = ButtonEventType.AFTER)
	public void checkStructureAfter(DbContext ctx, ScreenControl screenControl, InfosystemImportit infosystem) {
		this.screenControl = screenControl;
		infosystem.table().clear();
		infosystem.setYok(0);
		infosystem.setYfehler(0);
		infosystem.setYfehlerdatpruef(0);
		infosystem.setYfehlerstruktur(0);
		infosystem.setYdb("");
		infosystem.setYgruppe("");
		infosystem.setYtippkommando("");
		try {

			startEdpSessionHandler(infosystem);

			logger.info(MessageUtil.getMessage("info.structure.check.start.processing"));
			ExcelImportProcessing excelProcessing = new ExcelImportProcessing(infosystem.getYdatafile());
			dataList = excelProcessing.getDataList();
			logger.info(MessageUtil.getMessage("info.structure.check.end.processing"));
			logger.info(MessageUtil.getMessage("info.structure.check.start.data"));

			abasDataProcessing = new AbasDataProcessFactory().createAbasDataProcess(this.edpSessionhandler, dataList);
			if (abasDataProcessing != null) {
				abasDataProcessing.addListener(this);
				abasDataProcessing.checkDataListStructure(dataList);
			}
			//TODO END Review
			logger.info(MessageUtil.getMessage("info.structure.check.end.data"));
			infosystem.setYfehlerstruktur(getErrorCount());

			showDatabaseInfo(infosystem);
			setOptions(infosystem);
			protectOptionFields(infosystem, screenControl, false);
			if (infosystem.getYfehlerstruktur() == 0) {
				infosystem.setYstatus(MessageUtil.getMessage("main.status.structure.check.success"));
			} else {
				infosystem.setYstatus(MessageUtil.getMessage("main.status.structure.check.error"));
			}
			new TextBox(ctx, MessageUtil.getMessage(MAIN_STRUCTURE_CHECK_BOX_TITLE),
					MessageUtil.getMessage("main.structure.check.box.message")).show();

		} catch (ImportitException e) {
			showErrorBox(ctx, e.getMessage());
		}
		this.edpSessionhandler.closeAllConnections();
	}

	private void startEdpSessionHandler(InfosystemImportit infosystem) throws ImportitException {
		this.edpSessionhandler.initSession(infosystem.getYserver(), infosystem.getYport(), infosystem.getYmandant(),
				infosystem.getYpasswort());
	}

	private void showErrorBox(DbContext ctx, String message) {
		new TextBox(ctx, MessageUtil.getMessage(MAIN_EXCEPTION_TITLE), message).show();
	}

	private void setOptions(InfosystemImportit infosystem) {
		if (dataListNotEmpty()) {
			Data data = dataList.get(0);
			OptionCode optionCode = data.getOptionCode();
			infosystem.setYoptalwaysnew(optionCode.getAlwaysNew());
			infosystem.setYoptnofop(optionCode.noFop());
			infosystem.setYoptmodifiable(optionCode.getCheckFieldIsModifiable());
			infosystem.setYoptdeltab(optionCode.getDeleteTable());
			infosystem.setYopttransaction(optionCode.getInOneTransaction());
			infosystem.setYoptuseenglvars(optionCode.useEnglishVariables());
			infosystem.setYoptdontchangeifeq(optionCode.getDontChangeIfEqual());
			infosystem.setYoption(optionCode.getOptionsCode());
		} else {
			infosystem.setYoptalwaysnew(false);
			infosystem.setYoptnofop(false);
			infosystem.setYoptmodifiable(false);
			infosystem.setYoptdeltab(false);
			infosystem.setYopttransaction(false);
			infosystem.setYoptuseenglvars(false);
			infosystem.setYoptdontchangeifeq(false);
			infosystem.setYoption(0);
		}
	}

	private void showDatabaseInfo(InfosystemImportit infosystem) {
		if (dataListNotEmpty()) {
			Data data = dataList.get(0);
			if (data.getDatabase() != null) {
				infosystem.setYdb(data.getDatabase().toString());
			}
			if (data.getGroup() != null) {
				infosystem.setYgruppe(data.getGroup().toString());
			}
			if (data.getTypeCommand() != null) {
				infosystem.setYtippkommando(data.getTypeCommand().toString());
			}
			if (data.getSmlString() != null) {

				infosystem.setString(InfosystemImportit.META.ysml, data.getSmlString());

			}
			infosystem.setYtababspalte(data.getTableStartsAtField() + 1);
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
		return dataList != null && !dataList.isEmpty();
	}

	public boolean isTransactionDataList() throws ImportitException {
		if (dataListNotEmpty()) {
			return dataList.get(0).getOptionTransaction();
		}
		throw new ImportitException(MessageUtil.getMessage("main.err.no.data.read"));
	}

	@Override
	public void edpProgress(String message) {
		//TODO write messages only every second
		if (message != null) {
			this.screenControl.setNote(message, true, false);
		}
	}

}
