package de.abaspro.infosystem.importit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import de.abas.erp.common.type.AbasDate;
import de.abaspro.infosystem.importit.dataset.Data;
import de.abaspro.infosystem.importit.dataset.DataTable;
import de.abaspro.infosystem.importit.dataset.Field;
import de.abaspro.utils.Util;

public class ExcelImportProcessing {

	private Sheet importSheet;
	private ArrayList<Data> dataList;
	private List<Field> headerFields;
	private List<Field> smlFields;
	private DataTable tableFields;
	private Integer database;
	private Integer group;
	private String databaseString;
	private String groupString;
	private Integer typeCommand;
	private String typeCommandString;
	private String smlString;
	private Integer tableFromField;
	private OptionCode optionCode;
	private Logger logger = Logger.getLogger(ExcelImportProcessing.class);

	public ExcelImportProcessing(String importFilename) throws ImportitException {
		super();
		this.headerFields = new ArrayList<Field>();
		this.tableFields = new DataTable();
		this.dataList = new ArrayList<Data>();
		this.smlFields = new ArrayList<Field>();
		checkImportFile(importFilename);
		this.importSheet = initWorkbook(importFilename).getSheetAt(0);
		getFromSheet();
		this.smlString = getSML();
		this.headerFields = readFieldsInHead();
		this.tableFields = readFieldsInTable();
		this.dataList = readAllData();
	}

	private String getSML() {
		try {
			return getCellContents(3, 0);
		} catch (ImportitException e) {
			return null;
		}
	}

	private ArrayList<Data> readAllData() throws ImportitException {
		ArrayList<Data> dataList = new ArrayList<Data>();
		Data data = null;
		for (Integer row = 2; row <= importSheet.getLastRowNum(); row++) {
			if (!getCellContents(0, row).isEmpty()) {
				if (data == null) {
					data = fillValueInHead(row);
					dataList.add(data);
				} else {
					String valueKeyfield = getCellContents(data.getKeyFieldPosition(), row);
					if ((!data.getValueOfKeyField().equals(valueKeyfield)) || this.tableFields == null) {
						data = fillValueInHead(row);
						dataList.add(data);
					}
				}
				readTableData(row, data);
			}
		}
		return dataList;
	}

	private void readTableData(Integer row, Data data) throws ImportitException {
		if (tableFields != null) {
			List<DataTable> rows = data.getTableRows();
			DataTable dataTable = new DataTable(tableFields);
			ArrayList<Field> fields = dataTable.getTableFields();
			for (Field field : fields) {
				field.setValue(getCellContents(field.getColNumber(), row));
			}
			if (!dataTable.isEmpty() || row == 2) {
				rows.add(dataTable);
			}
		}
	}

	private Data fillValueInHead(Integer tableBeginAtRow) throws ImportitException {
		Data data = initNewData();
		List<Field> headerFields = data.getHeaderFields();
		for (Field field : headerFields) {
			field.setValue(getCellContents(field.getColNumber(), tableBeginAtRow));
		}

		return data;

	}

	private Data initNewData() throws ImportitException {
		Data data = new Data();
		data.setHeaderFields(getCopyOfHeaderFields());
		data.fillKeyfield();
		data.setDatabase(this.database);
		data.setDbString(this.databaseString);
		data.setGroup(this.group);
		data.setDbGroupString(this.groupString);
		data.setTypeCommand(this.typeCommand);
		data.setTypeCommandString(this.typeCommandString);
		data.setOptionCode(this.optionCode);
		data.setTableStartsAtField(this.tableFromField);
		data.setSmlString(this.smlString);
		return data;

	}

	private List<Field> getCopyOfHeaderFields() throws ImportitException {
		List<Field> headerFields = new ArrayList<>();
		for (Field field : this.headerFields) {
			headerFields.add(new Field(field.getCompleteContent(), field));
		}
		return headerFields;
	}

	public ArrayList<Data> getDataList() {
		return dataList;
	}

	private List<Field> readFieldsInHead() throws ImportitException {
		List<Field> headerFields = new ArrayList<>();
		Integer row = 1;
		for (int col = 0; (col < getMaxCol()) && (col < (this.tableFromField) || this.tableFromField == 0); col++) {
			String content = getCellContents(col, row);
			if (content != null) {
				if (!content.isEmpty()) {
					Field field = new Field(content, true, col, this.optionCode);
					headerFields.add(field);
				}
			}
		}
		return headerFields;
	}

	private DataTable readFieldsInTable() throws ImportitException {
		DataTable dataTable = new DataTable();
		List<Field> tableFields = dataTable.getTableFields();
		Integer row = 1;
		if (tableFromField > 0) {
			for (int col = tableFromField; col < getMaxCol(); col++) {
				String content;
				content = getCellContents(col, row);
				if (!content.isEmpty()) {
					Field field = new Field(content, true, col, this.optionCode);
					tableFields.add(field);
				}
			}
			return dataTable;
		} else {
			return null;
		}
	}

	private void checkImportFile(String filename) throws ImportitException {
		File file = new File(filename);
		if (file.exists() & file.isFile()) {
			if (file.canRead()) {
				if (!filename.contains(".xlsx") && !filename.contains(".xls")) {

					logger.error(Util.getMessage("excel.check.import.file.no.excel", filename));
					throw new ImportitException(Util.getMessage("excel.check.import.file.no.excel", filename));
				}
			} else {
				logger.error(Util.getMessage("excel.check.import.file.cant.read", filename));
				throw new ImportitException(Util.getMessage("excel.check.import.file.cant.read", filename));
			}
		} else {
			logger.error(Util.getMessage("excel.check.import.file.not.found", filename));
			throw new ImportitException(Util.getMessage("excel.check.import.file.not.found", filename));
		}
	}

	private Workbook initWorkbook(String filename) throws ImportitException {
		try {
			if (filename.contains(".xlsx")) {
				org.apache.poi.ss.usermodel.Workbook workbook;
				workbook = new XSSFWorkbook(new FileInputStream(filename));
				return workbook;

			} else if (filename.contains(".xls")) {
				Workbook workbook = new HSSFWorkbook(new FileInputStream(filename));
				return workbook;

			} else {
				logger.error(Util.getMessage("excel.check.import.file.no.excel", filename));
				throw new ImportitException(Util.getMessage("excel.check.import.file.no.excel", filename));
			}

		} catch (FileNotFoundException e) {
			logger.error(Util.getMessage("excel.check.import.file.not.found", filename));
			throw new ImportitException(Util.getMessage("excel.check.import.file.not.found", filename));

		} catch (IOException e) {
			logger.error(Util.getMessage("excel.check.import.file.access.err", filename, e.getMessage()));
			throw new ImportitException(
					Util.getMessage("excel.check.import.file.access.err", filename, e.getMessage()));
		}
	}

	private void getFromSheet() throws ImportitException {
		try {
			this.databaseString = getDatabaseString();
			this.groupString = getGroupString();
			this.typeCommandString = getTypeCommandString();
			if (this.databaseString == null && this.typeCommandString == null) {
				throw new ImportitException(Util.getMessage("excel.error.noDatabase"));
			}
			this.database = getDatabase();
			this.group = getGroup();
			this.typeCommand = getTypeCommand();
			this.tableFromField = getTableFrom();
			this.optionCode = new OptionCode(getOptionCodeFromSheet());
		} catch (NumberFormatException e) {
			logger.error(Util.getMessage("excel.get.from.sheet.wrong.format"));
			throw new ImportitException(Util.getMessage("excel.get.from.sheet.wrong.format"));
		}
	}

	private int getMaxCol() {
		return importSheet.getRow(1).getPhysicalNumberOfCells();
	}

	private String getTypeCommandString() throws ImportitException {
		String group = getDbGroupComplete();
		int colon = group.indexOf(":");
		if (colon == 0 || colon == -1) {
			return group;
		} else {
			return null;
		}
	}

	private Integer getTypeCommand() throws ImportitException {
		String typeCommandString = getTypeCommandString();
		if (typeCommandString != null) {
			if (!typeCommandString.isEmpty()) {
				try {
					return Integer.parseInt(typeCommandString);
				} catch (NumberFormatException e) {
					return null;
				}
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	private Boolean isCellEmpty(int x, int y) throws ImportitException {
		Row row = importSheet.getRow(0);
		Cell cell = null;
		if (row != null) {
			cell = row.getCell(x);
		} else
			return true;

		if (cell == null) {
			return true;
		} else {

			if (importSheet.getRow(y).getCell(x).getCellType() == Cell.CELL_TYPE_BLANK) {
				return true;
			} else {
				if (getCellContents(x, y).equals("")) {
					return true;
				} else {
					if (getCellContents(x, y) == null) {
						return null;
					} else {
						return false;
					}
				}
			}
		}
	}

	private Integer getOptionCodeFromSheet() throws ImportitException {
		try {
			if (isCellEmpty(2, 0)) {
				return 0;
			} else {
				Integer option = Integer.parseInt(getCellContents(2, 0));
				return option;
			}
		} catch (NumberFormatException e) {
			throw new ImportitException(Util.getMessage("excel.get.options.invalid.value"));
		}
	}

	private Integer getGroup() throws ImportitException {
		String group = getDbGroupComplete();
		String groupString;
		int colon = group.indexOf(":") + 1;
		if (colon > 0) {
			groupString = group.substring(colon, group.length());
			if (!groupString.isEmpty() && groupString != null) {
				try {
					return Integer.parseInt(groupString);
				} catch (NumberFormatException e) {
					return null;
				}
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	private String getDatabaseString() throws ImportitException {
		String group = getDbGroupComplete();
		int colon = group.indexOf(":");
		if (colon > 0) {
			String test = group.substring(0, colon);
			return group.substring(0, colon);
		} else {
			return null;
		}
	}

	private String getGroupString() throws ImportitException {
		String group = getDbGroupComplete();
		int colon = group.indexOf(":") + 1;
		if (colon > 0) {
			String test = group.substring(colon, group.length());
			return group.substring(colon, group.length());
		} else {
			return null;
		}
	}

	private Integer getDatabase() throws ImportitException {
		String databaseString = this.databaseString;
		if (databaseString != null) {
			if (!databaseString.isEmpty()) {
				try {
					return Integer.parseInt(databaseString);
				} catch (NumberFormatException e) {
					return null;
				}
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	private String getDbGroupComplete() throws ImportitException {
		return getCellContents(0, 0);

	}

	private Integer getTableFrom() throws ImportitException {
		Integer tableFromField = 0;
		try {
			String cellContents = getCellContents(1, 0);
			if (cellContents.length() > 0) {
				tableFromField = Integer.parseInt(cellContents);
				if (tableFromField > 1) {
					tableFromField = tableFromField - 1;
				} else {
					tableFromField = 0;
				}
			}
			return tableFromField;
		} catch (NumberFormatException e) {
			throw new ImportitException(Util.getMessage("excel.get.table.from.invalid.value"));
		}
	}

	private String getCellContents(int column, int line) throws ImportitException {
		Cell cell = null;
		try {
			Row row = importSheet.getRow(line);
			if (row != null) {
				cell = row.getCell(column);
			}
		} catch (NullPointerException e) {
			logger.error(Util.getMessage("excel.get.cell.contents.null", line, column), e);
			throw new ImportitException(Util.getMessage("excel.get.cell.contents.null", line, column), e);
		}
		if (cell != null) {
			switch (cell.getCellType()) {
			case Cell.CELL_TYPE_STRING:
				return cell.getStringCellValue();
			case Cell.CELL_TYPE_NUMERIC:
				if (DateUtil.isCellDateFormatted(cell)) {
					Date date = cell.getDateCellValue();
					int hour = date.getHours();
					int min = date.getMinutes();
					int year = date.getYear();

					if (year == 1899 || year == -1) {
						String time = hour + ":" + min;
						return time;
					} else {
						return new AbasDate(cell.getDateCellValue()).toString();
					}

				} else {
					Double numericValue = cell.getNumericCellValue();
					Integer intValue = numericValue.intValue();
					if (intValue.doubleValue() == numericValue) {
						return intValue.toString();
					} else {
						return new DecimalFormat("#.#########").format(numericValue);
					}
				}
			case Cell.CELL_TYPE_BOOLEAN:
				return (cell.getBooleanCellValue() ? Util.getMessage("excel.bool.yes")
						: Util.getMessage("excel.bool.no"));
			case Cell.CELL_TYPE_FORMULA:
				return handleFormula(cell, line + 1, column + 1);
			case Cell.CELL_TYPE_BLANK:
				return "";
			case Cell.CELL_TYPE_ERROR:
				throw new ImportitException(Util.getMessage("excel.get.cell.contents.err.type", line + 1, column + 1));
			default:
				return null;
			}
		} else {
			return "";
		}
	}

	private String handleFormula(Cell cell, int errorLine, int errorColumn) throws ImportitException {
		switch (cell.getCachedFormulaResultType()) {
		case Cell.CELL_TYPE_STRING:
			return cell.getStringCellValue();
		case Cell.CELL_TYPE_NUMERIC:
			return ((Double) cell.getNumericCellValue()).toString();
		case Cell.CELL_TYPE_BOOLEAN:
			return (cell.getBooleanCellValue() ? Util.getMessage("excel.bool.yes") : Util.getMessage("excel.bool.no"));
		case Cell.CELL_TYPE_BLANK:
			return "";
		default:
			throw new ImportitException(Util.getMessage("excel.get.cell.contents.err.type", errorLine, errorColumn));
		}
	}

}
