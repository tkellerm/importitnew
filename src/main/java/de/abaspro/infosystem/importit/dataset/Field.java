package de.abaspro.infosystem.importit.dataset;

import static de.abaspro.infosystem.importit.ImportOptions.DONT_CHANGE_IF_EQUAL;
import static de.abaspro.infosystem.importit.ImportOptions.KEY;
import static de.abaspro.infosystem.importit.ImportOptions.MODIFIABLE;
import static de.abaspro.infosystem.importit.ImportOptions.NOTEMPTY;
import static de.abaspro.infosystem.importit.ImportOptions.SKIP;

import java.text.MessageFormat;

import de.abas.ceks.jedp.EDPEKSArtInfo;
import de.abas.ceks.jedp.EDPTools;
import de.abas.ceks.jedp.EDPVariableLanguage;
import de.abaspro.infosystem.importit.ImportOptions;
import de.abaspro.infosystem.importit.ImportitException;
import de.abaspro.infosystem.importit.OptionCode;
import de.abaspro.utils.Util;

public class Field {

	private String name;
	private String value;
	private String keySelectionString;
	private String key;
	private boolean optionNotEmpty;
	private boolean optionModifiable;
	private boolean optionGlobalModifiable;
	private boolean optionSkip;
	private boolean optionDontChangeIfEqual;
	private boolean optionKeySelection;
	private String error;
	private String completeContent;
	private boolean fieldToCopy;
	private Integer colNumber;
	private String abasTyp;
	private String abasID;
	private String fieldSelectionString;
	private OptionCode optionCode;
	private boolean optionKey;
	private boolean optionfieldseletion;

	public Field(String completeContent, Boolean fieldToCopy, Integer col, OptionCode optionCode) {
		initField();
		this.completeContent = completeContent;
		this.fieldToCopy = fieldToCopy;
		this.colNumber = col;
		this.optionCode = optionCode;
		this.name = extractValue(completeContent);
		if (fieldToCopy) {
			fillOptions();
			fillkeyfield();
		}
	}

	protected void fillkeyfield() {
		if (this.optionKey) {
			this.key = extractkey(ImportOptions.KEY);
		} else {
			if (this.optionKeySelection) {

				this.key = extractKeyFromSelection();
			}
		}

	}

	protected String extractKeyFromSelection() {
		final String SORT = "@sort=";
		;
		String key = "";
		if (this.keySelectionString.contains(SORT)) {
			int indexOf = this.keySelectionString.indexOf(SORT);
			String test = this.keySelectionString.substring(indexOf + SORT.length(), this.keySelectionString.length());
			if (test.contains(";")) {
				key = test.substring(0, test.indexOf(";"));
			} else if (test.contains("'")) {
				key = test.substring(0, test.indexOf("'"));
			} else {
				key = test;
			}

		}
		return key;
	}

	public Field(String completeContent, Field headfield) throws ImportitException {
		this.optionGlobalModifiable = false;
		if (headfield.getFieldInHead()) {
			initField();
			this.completeContent = completeContent;
			this.name = headfield.getName();
			this.key = headfield.getKey();
			this.optionCode = headfield.optionCode;
			this.value = extractValue(completeContent);
			this.optionModifiable = headfield.getOptionModifiable();
			this.optionNotEmpty = headfield.getOptionNotEmpty();
			this.optionSkip = headfield.getOptionSkip();
			this.optionDontChangeIfEqual = headfield.getOptionDontChangeIfEqual();
			this.optionKeySelection = headfield.getOptionKeySelection();
			this.keySelectionString = headfield.getKeySelectionString();
			this.fieldSelectionString = headfield.getFieldSelectionString();
			this.colNumber = headfield.colNumber;
		} else {
			throw new ImportitException(Util.getMessage("error.Field.noFieldinHead"));
		}
	}

	protected String getKeySelectionString() {
		return keySelectionString;
	}

	protected void setKeySelectionString(String keySelectionString) {
		this.keySelectionString = keySelectionString;
	}

	protected Boolean getOptionKeySelection() {
		return optionKeySelection;
	}

	protected void setOptionKeySelection(Boolean optionKeySelection) {
		this.optionKeySelection = optionKeySelection;
	}

	public String getAbasID() {
		return abasID;
	}

	public void setAbasID(String abasID) {
		this.abasID = abasID;
	}

	private void initField() {
		this.error = "";
		this.name = "";
		this.abasTyp = "";
		this.abasID = "";
		this.value = "";
		this.key = "";
		this.fieldToCopy = false;
		this.keySelectionString = "";
		this.fieldSelectionString = "";
		this.optionGlobalModifiable = false;
		this.optionModifiable = false;
		this.optionNotEmpty = false;
		this.optionSkip = false;
		this.optionKeySelection = false;
		this.optionDontChangeIfEqual = false;
	}

	public void setOptionGlobalModifiable(Boolean optionGlobalModifiable) {
		this.optionGlobalModifiable = optionGlobalModifiable;
	}

	@Override
	public String toString() {
		return "Field [name=" + name + ", value=" + value + ", abasTyp=" + abasTyp + "]";
	}

	public Boolean getOptionDontChangeIfEqual() {
		return optionDontChangeIfEqual;
	}

	public void setOptionDontChangeIfEqual(Boolean optionDontChangeIfEqual) {
		this.optionDontChangeIfEqual = optionDontChangeIfEqual;
	}

	protected void fillOptions() {
		if (!this.completeContent.isEmpty()) {
			optionNotEmpty = this.completeContent.contains(NOTEMPTY.toString());
			optionModifiable = this.completeContent.contains(MODIFIABLE.toString());
			optionSkip = this.completeContent.contains(SKIP.toString());
			optionDontChangeIfEqual = this.completeContent.contains(DONT_CHANGE_IF_EQUAL.toString());

			if (this.completeContent.contains(KEY.toString())) {
				this.optionKey = true;
				this.keySelectionString = extractSelectionString(ImportOptions.KEY);
			}
			if (this.completeContent.contains(ImportOptions.KEYSELECTION.toString())) {
				this.optionKeySelection = true;
				this.keySelectionString = extractSelectionString(ImportOptions.KEYSELECTION);
			}
			if (completeContent.contains(ImportOptions.SELECTION.toString())) {
				this.optionfieldseletion = true;
				this.setFieldSelectionString(extractSelectionString(ImportOptions.SELECTION));
			}

		} else {
			optionSkip = true;
		}
	}

	protected String extractSelectionString(ImportOptions importOptions) {
		switch (importOptions) {
		case SELECTION:
			return createSelfromSelection(importOptions);
		case KEYSELECTION:
			return createSelfromSelection(importOptions);
		case KEY:
			return createSelFromKey(importOptions);
		default:
			return createSelFromFieldname();
		}
	}

	protected String createSelFromFieldname() {
		String selectionString = this.name + "={0}";
		return selectionString;
	}

	protected String createSelFromKey(ImportOptions importOptions) {

		String key = extractkey(importOptions);

		String selectionString = this.name + "={0};@sort=" + key;

		return selectionString;
	}

	private String extractkey(ImportOptions importOptions) {
		int indexoftrenner = this.completeContent.indexOf("@");
		int optionpluslength = (importOptions.toString() + "=").length();

		String key = this.completeContent.substring(optionpluslength + indexoftrenner, this.completeContent.length());
		return key;
	}

	protected String createSelfromSelection(ImportOptions importOptions) {
		// @selection=selectionString
		String testString = importOptions.toString() + "='";
		int lengthTestString = testString.length();
		int index = this.completeContent.indexOf(testString);

		String substring = this.completeContent.substring(index + lengthTestString, this.completeContent.length());
		String result = "";
		if (substring.contains("'")) {
			result = substring.substring(0, substring.indexOf("'"));
		} else {
			result = substring;
		}

		if (result.startsWith("$,,") || result.startsWith("%,,")) {
			return result.substring(3);
		}
		if (result.startsWith("$,")) {
			return result.substring(2);
		}

		return result;
	}

	protected static String extractValue(String completeContent) {
		int index = completeContent.indexOf("@");
		if (index != -1) {
			return completeContent.substring(0, index);
		}
		return completeContent;
	}

	public String getCompleteContent() {
		return completeContent;
	}

	public Integer getColNumber() {
		return colNumber;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		if (abasTyp.equals("B")) {
			if (value.equals("ja") || value.equals("yes") || value.equals("true")) {
				return "1";
			} else if (value.equals("nein") || value.equals("no") || value.equals("false")) {
				return "0";
			}
		}
		return value;
	}

	public String getReferenceFieldValue() {
		if (abasTyp.startsWith("V")) {
			return this.value.replaceFirst("^[A-Z][ ]", "");
		} else {
			return this.value;
		}

	}

	public void setValue(String value) {
		this.value = value;
	}

	public Boolean getOptionModifiable() {
		return this.optionModifiable || this.optionGlobalModifiable;
	}

	public Boolean getOptionSkip() {
		return optionSkip;
	}

	public String getKey() {
		return this.key;

	}

	public String getKeySelection() {
		return keySelectionString;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public Boolean getFieldInHead() {
		return fieldToCopy;
	}

	public String getAbasTyp() {
		return abasTyp;
	}

	public void setAbasType(String abasTyp) {
		this.abasTyp = abasTyp;
	}

	public Boolean getOptionNotEmpty() {
		return optionNotEmpty;
	}

	public String getFieldSelectionString() {
		return fieldSelectionString;
	}

	public void setFieldSelectionString(String fieldSelectionString) {
		this.fieldSelectionString = fieldSelectionString;
	}

	public EDPVariableLanguage getEDPVariableLanguage() {
		if (optionCode.useEnglishVariables()) {
			return EDPVariableLanguage.ENGLISH;
		} else {
			return EDPVariableLanguage.GERMAN;
		}
	}

	/**
	 * This function check the different Value-Fields in this Class.
	 * 
	 * @return evaluated Value
	 */
	public String getValidateFieldValue() {

		if (this.isReferenceField()) {
			if (!this.abasID.isEmpty()) {
				return this.abasID;
			} else if (!this.fieldSelectionString.isEmpty()) {
				// Es muss noch die Selektionskriterien hinzugefügt werden,
				// damit es so in das Feld eingetragen werden kann
				String responseValue = "$,," + MessageFormat.format(this.fieldSelectionString, this.value);
				return responseValue;
			} else {
				return this.value;
			}
		} else {
			return this.value;
		}

	}

	public boolean isReferenceField() {
		if (!this.abasTyp.isEmpty()) {
			int datatyp = new EDPEKSArtInfo(this.abasTyp).getDataType();
			if (datatyp == EDPTools.EDP_REFERENCE) {
				return true;
			}
			if (datatyp == EDPTools.EDP_ROWREFERENCE) {
				return true;
			}
		}
		return false;

	}

	public boolean iswithKeySelection() {
		if (this.optionKeySelection) {
			return true;
		}
		// if (!this.fieldSelectionString.isEmpty()) {
		// return true;
		// }
		return false;
	}

	public Boolean isKeySelectionField() {
		if (this.optionKeySelection) {
			return true;
		}
		if (!this.keySelectionString.isEmpty() && this.name.isEmpty()) {
			return true;
		}
		return false;
	}

	public void convertToKeyField() {
		if (this.keySelectionString.isEmpty()) {
			this.keySelectionString = createSelFromFieldname();
		}

	}

}
