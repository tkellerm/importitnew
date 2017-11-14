package de.abaspro.infosystem.importit.dataprocessing;

import java.util.ArrayList;

import de.abaspro.infosystem.importit.ImportitException;
import de.abaspro.infosystem.importit.dataset.Data;

public interface AbasDataProcessable {

	public void checkDataListStructure(ArrayList<Data> dataList) throws ImportitException;

	public void checkDataListValues(ArrayList<Data> dataList) throws ImportitException;

	public void importDataListTransaction(ArrayList<Data> dataList) throws ImportitException;

	public void startTransaction() throws ImportitException;

	public void abortTransaction() throws ImportitException;

	public void commitTransaction() throws ImportitException;

	public void importDataList(ArrayList<Data> dataList) throws ImportitException;

}
