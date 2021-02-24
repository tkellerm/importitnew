package de.abas.infosystem.importit;

import de.abas.erp.common.type.AbasDate;
import de.abas.infosystem.importit.dataset.Data;
import de.abas.infosystem.importit.dataset.DataTable;
import de.abas.infosystem.importit.dataset.Field;
import de.abas.utils.MessageUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ExcelImportProcessing {

    public static final String EXCEL_CHECK_IMPORT_FILE_NO_EXCEL = "excel.check.import.file.no.excel";
    public static final String EXCEL_CHECK_IMPORT_FILE_NOT_FOUND = "excel.check.import.file.not.found";
    public static final int GENERAL_INFORMATION_ROW = 0;
    public static final int FIELDNAME_ROW = 1;
    public static final int START_DATA_ROW = 2;
    public static final int DBCONTENT_FIELD_COLUMN = 0;
    public static final int TABLEFROM_FIELD_COLUMN = 1;
    public static final int OPTIONCODE_FIELD_COLUMN = 2;
    public static final int SML_NUMBER_FIELD_COLUMN = 3;
    private final Sheet importSheet;
    private final ArrayList<Data> dataList;
    private final List<Field> headerFields;
    private final DataTable tableFields;
    private Integer database;
    private Integer group;
    private String databaseString;
    private String groupString;
    private Integer typeCommand;
    private String typeCommandString;
    private String smlString;
    private Integer tableFromField;
    private OptionCode optionCode;
    private final Logger logger = Logger.getLogger(ExcelImportProcessing.class);


    public ExcelImportProcessing(String importFilename) throws ImportitException {
//Todo Aufrufe der Methoden mit Parametern, damit sie mehr Reihenfolge unabhängig sind
//, nicht so viele implizite Abhängigkeiten
        checkImportFile(importFilename);
        this.importSheet = initWorkbook(importFilename).getSheetAt(0);
        readGlobalSettingsFromSheet();
        this.headerFields = readFieldsInHead();
        this.tableFields = readFieldsInTable();
        this.dataList = readAllData();
        addSachField();
    }

    private void addSachField() {
        if (!this.smlString.isEmpty() && !this.dataList.isEmpty()) {


            for (Data data : dataList) {
                int maxcol = 0;
                List<Field> headerFieldList = data.getHeaderFields();
                boolean found = false;
                for (Field field : headerFieldList) {
                    if (field.getName().equalsIgnoreCase("sach")) {
                        found = true;
                    }
                    maxcol = field.getColNumber();
                }
                if (!found) {
                    Field field = new Field("sach@dontChangeIfEqual", true, maxcol + 1, this.optionCode);
                    field.setValue(this.smlString);
                    headerFieldList.add(field);
                }
            }

        }

    }

    private String getSML() {
        try {
            return getCellContents(SML_NUMBER_FIELD_COLUMN, GENERAL_INFORMATION_ROW);
        } catch (ImportitException e) {
            return null;
        }
    }

    private ArrayList<Data> readAllData() throws ImportitException {
        ArrayList<Data> dataListLocal = new ArrayList<>();
        Data data = null;
        for (int row = START_DATA_ROW; row <= importSheet.getLastRowNum(); row++) {
            if (StringUtils.isNoneBlank(getCellContents(0, row))) {
                if (data == null) {
                    data = fillValueInHead(row);
                    dataListLocal.add(data);
                } else {
                    String valueKeyfield = getCellContents(data.getKeyFieldPosition(), row);
                    if ((!data.getValueOfKeyField().equals(valueKeyfield)) || this.tableFields == null) {
                        data = fillValueInHead(row);
                        dataListLocal.add(data);
                    }
                }
                readTableData(row, data);
            }
        }
        return dataListLocal;
    }

    private void readTableData(Integer row, Data data) throws ImportitException {
        if (tableFields != null) {
            List<DataTable> rows = data.getTableRows();
            DataTable dataTable = new DataTable(tableFields);
            List<Field> fields = dataTable.getTableFields();
            for (Field field : fields) {
                field.setValue(getCellContents(field.getColNumber(), row));
            }
            if (!dataTable.isEmpty() || row == START_DATA_ROW) {
                rows.add(dataTable);
            }
        }
    }

    private Data fillValueInHead(Integer tableBeginAtRow) throws ImportitException {
        Data data = initNewData();
        List<Field> headerFieldsLocal = data.getHeaderFields();
        for (Field field : headerFieldsLocal) {
            field.setValue(getCellContents(field.getColNumber(), tableBeginAtRow));
        }

        return data;

    }

    private Data initNewData() throws ImportitException {
        Data data = new Data();
        data.setHeaderFields(getCopyOfHeaderFields());
        data.fillKeyField();
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
        List<Field> headerFieldsLocal = new ArrayList<>();
        for (Field field : this.headerFields) {
            headerFieldsLocal.add(new Field(field.getCompleteContent(), field));
        }
        return headerFieldsLocal;
    }

    public List<Data> getDataList() {
        return dataList;
    }

    private List<Field> readFieldsInHead() throws ImportitException {
        List<Field> fieldsInHead = new ArrayList<>();
        int row = 1;
        // TODO integriere die Prüfung auf this.tableFromField ind getMaxCol
        for (int col = 0; (col < getMaxCol()) && (col < (this.tableFromField) || this.tableFromField == 0); col++) {
            String content = getCellContents(col, row);

            if (StringUtils.isNoneBlank(content)) {
                Field field = new Field(content, true, col, this.optionCode);
                fieldsInHead.add(field);

            }
        }

        return fieldsInHead;
    }

    private DataTable readFieldsInTable() throws ImportitException {
        DataTable dataTable = new DataTable();
        List<Field> fieldInTable = dataTable.getTableFields();
        if (tableFromField > 0) {
            for (int col = tableFromField; col < getMaxCol(); col++) {
                String content;
                content = getCellContents(col, FIELDNAME_ROW);
                if (!content.isEmpty()) {
                    Field field = new Field(content, true, col, this.optionCode);
                    fieldInTable.add(field);
                }
            }

            return dataTable;
        } else {
            return null;
        }
    }

    private void checkImportFile(String filename) throws ImportitException {
        File file = new File(filename);
        if (file.exists() && file.isFile()) {
            if (file.canRead()) {
                if (!filename.contains(".xlsx") && !filename.contains(".xls")) {

                    logger.error(MessageUtil.getMessage(EXCEL_CHECK_IMPORT_FILE_NO_EXCEL, filename));
                    throw new ImportitException(MessageUtil.getMessage(EXCEL_CHECK_IMPORT_FILE_NO_EXCEL, filename));
                }
            } else {
                logger.error(MessageUtil.getMessage("excel.check.import.file.cant.read", filename));
                throw new ImportitException(MessageUtil.getMessage("excel.check.import.file.cant.read", filename));
            }
        } else {
            logger.error(MessageUtil.getMessage(EXCEL_CHECK_IMPORT_FILE_NOT_FOUND, filename));
            throw new ImportitException(MessageUtil.getMessage(EXCEL_CHECK_IMPORT_FILE_NOT_FOUND, filename));
        }
    }

    private Workbook initWorkbook(String filename) throws ImportitException {
        try {
            if (filename.contains(".xlsx")) {
                return new XSSFWorkbook(new FileInputStream(filename));

            } else if (filename.contains(".xls")) {
                return new HSSFWorkbook(new FileInputStream(filename));

            } else {
                logger.error(MessageUtil.getMessage(EXCEL_CHECK_IMPORT_FILE_NO_EXCEL, filename));
                throw new ImportitException(MessageUtil.getMessage(EXCEL_CHECK_IMPORT_FILE_NO_EXCEL, filename));
            }

        } catch (FileNotFoundException e) {
            logger.error(MessageUtil.getMessage(EXCEL_CHECK_IMPORT_FILE_NOT_FOUND, filename));
            throw new ImportitException(MessageUtil.getMessage(EXCEL_CHECK_IMPORT_FILE_NOT_FOUND, filename));

        } catch (IOException e) {
            logger.error(MessageUtil.getMessage("excel.check.import.file.access.err", filename, e.getMessage()));
            throw new ImportitException(
                    MessageUtil.getMessage("excel.check.import.file.access.err", filename, e.getMessage()));
        }
    }

    /**
     * This methods read all informations for database, import options from the excelsheet.
     * The order of the commands is important.
     *
     * @throws ImportitException if we have a NumberformatException
     */
    private void readGlobalSettingsFromSheet() throws ImportitException {
        try {
            this.databaseString = getDatabaseString();
            this.groupString = getGroupString();
            this.typeCommandString = getTypeCommandString();
            if (this.databaseString == null && this.typeCommandString == null) {
                throw new ImportitException(MessageUtil.getMessage("excel.error.noDatabase"));
            }
            // https://documentation.abas.ninja/de/importit/#_aufbau_der_excel_datei
            this.database = getDatabase();
            this.group = getGroup();
            this.typeCommand = getTypeCommand();
            this.tableFromField = getTableFromCellContent();
            this.optionCode = new OptionCode(getOptionCodeFromSheet());
            this.smlString = getSML();
        } catch (NumberFormatException e) {
            logger.error(MessageUtil.getMessage("excel.get.from.sheet.wrong.format"));
            throw new ImportitException(MessageUtil.getMessage("excel.get.from.sheet.wrong.format"));
        }
    }

    private int getMaxCol() {
        return importSheet.getRow(1).getPhysicalNumberOfCells();
    }

    private String getTypeCommandString() throws ImportitException {
        String databaseGroup = getTargetDBCellContent();
        int colon = databaseGroup.indexOf(":");
        if (colon == 0 || colon == -1) {
            return databaseGroup;
        } else {
            return null;
        }
    }

    private Integer getTypeCommand() throws ImportitException {
        String stringForTypeCommand = getTypeCommandString();
        return getTypefromTypeCommandORDatabase(stringForTypeCommand);
    }

    private Integer getTypefromTypeCommandORDatabase(String stringForTypeCommand) {
        if (StringUtils.isNoneBlank(stringForTypeCommand)) {
            try {
                return Integer.parseInt(stringForTypeCommand);
            } catch (NumberFormatException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    private boolean isCellEmpty(int x, int y) throws ImportitException {
        Row row = importSheet.getRow(0);
        Cell cell;
        if (row != null) {
            cell = row.getCell(x);
        } else
            return true;

        if (cell == null) {
            return true;
        } else {

            if (importSheet.getRow(y).getCell(x).getCellType() == CellType.BLANK) {
                return true;
            } else {
                if (getCellContents(x, y).equals("")) {
                    return true;
                } else {
                    //null ist leer
                    return getCellContents(x, y) == null;
                }
            }
        }
    }

    private Integer getOptionCodeFromSheet() throws ImportitException {
        try {
            if (isCellEmpty(OPTIONCODE_FIELD_COLUMN, GENERAL_INFORMATION_ROW)) {
                return 0;
            } else {
                return Integer.parseInt(getCellContents(OPTIONCODE_FIELD_COLUMN, GENERAL_INFORMATION_ROW));
            }
        } catch (NumberFormatException e) {
            throw new ImportitException(MessageUtil.getMessage("excel.get.options.invalid.value"));
        }
    }

    private Integer getGroup() throws ImportitException {
        String databaseGroup = getTargetDBCellContent();
        String databaseGroupString;
        int colon = databaseGroup.indexOf(":") + 1;
        if (colon > 0) {
            databaseGroupString = databaseGroup.substring(colon);
            if (!databaseGroupString.isEmpty()) {
                try {
                    return Integer.parseInt(databaseGroupString);
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
        String databaseGroup = getTargetDBCellContent();
        int colon = databaseGroup.indexOf(":");
        if (colon > 0) {
            return databaseGroup.substring(0, colon);
        } else {
            return null;
        }
    }

    private String getGroupString() throws ImportitException {
        String databaseGroup = getTargetDBCellContent();
        int colon = databaseGroup.indexOf(":") + 1;
        if (colon > 0) {
            return databaseGroup.substring(colon);
        } else {
            return null;
        }
    }

    private Integer getDatabase() {
        String stringForDatabase = this.databaseString;
        return getTypefromTypeCommandORDatabase(stringForDatabase);
    }


    /**
     * This Methods read the cell with the database information
     * the value kann be Database:Database group or the name or number of a typecommand.
     * see at https://documentation.abas.ninja/de/importit/#_aufbau_der_excel_datei
     *
     * @return Content of the Cell 0,0
     * @throws ImportitException when the cell value is null or from type error
     */
    private String getTargetDBCellContent() throws ImportitException {
        return getCellContents(DBCONTENT_FIELD_COLUMN, GENERAL_INFORMATION_ROW);

    }

    private int getTableFromCellContent() throws ImportitException {
        int fieldsInTable = 0;
        try {
            String cellContents = getCellContents(TABLEFROM_FIELD_COLUMN, GENERAL_INFORMATION_ROW);
            if (cellContents.length() > 0) {
                fieldsInTable = Integer.parseInt(cellContents);
                if (fieldsInTable > 1) {
                    fieldsInTable = fieldsInTable - 1;
                } else {
                    fieldsInTable = 0;
                }
            }
            return fieldsInTable;
        } catch (NumberFormatException e) {
            throw new ImportitException(MessageUtil.getMessage("excel.get.table.from.invalid.value"));
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
            logger.error(MessageUtil.getMessage("excel.get.cell.contents.null", line, column), e);
            throw new ImportitException(MessageUtil.getMessage("excel.get.cell.contents.null", line, column), e);
        }
        if (cell != null) {
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue();
                case NUMERIC:
                    return analyzeNumericCell(cell);
                case BOOLEAN:
                    return (cell.getBooleanCellValue() ? MessageUtil.getMessage("excel.bool.yes")
                            : MessageUtil.getMessage("excel.bool.no"));
                case FORMULA:
                    return handleFormula(cell, line + 1, column + 1);


                case ERROR:
                    throw new ImportitException(MessageUtil.getMessage("excel.get.cell.contents.err.type", line + 1, column + 1));

                case BLANK:
                default:
                    return "";
            }
        } else {
            return "";
        }
    }

    private String analyzeNumericCell(Cell cell) {
        if (DateUtil.isCellDateFormatted(cell)) {
            Date date = cell.getDateCellValue();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int min = calendar.get(Calendar.MINUTE);
            int year = calendar.get(Calendar.YEAR);

            if (year == 1899 || year == -1) {
                return hour + ":" + min;
            } else {
                return new AbasDate(cell.getDateCellValue()).toString();
            }

        } else {
            Double numericValue = cell.getNumericCellValue();

            if (Double.valueOf(numericValue.intValue()).equals(numericValue)) {
                return String.valueOf(numericValue.intValue());
            } else {
                return new DecimalFormat("#.#########").format(numericValue);
            }
        }
    }

    private String handleFormula(Cell cell, int errorLine, int errorColumn) throws ImportitException {
        switch (cell.getCachedFormulaResultType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return ((Double) cell.getNumericCellValue()).toString();
            case BOOLEAN:
                return (cell.getBooleanCellValue() ? MessageUtil.getMessage("excel.bool.yes") : MessageUtil.getMessage("excel.bool.no"));
            case BLANK:
                return "";
            default:
                throw new ImportitException(MessageUtil.getMessage("excel.get.cell.contents.err.type", errorLine, errorColumn));
        }
    }

}
