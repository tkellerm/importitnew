package de.abas.infosystem.importit.dataprocessing;

import de.abas.infosystem.importit.ImportitException;
import de.abas.infosystem.importit.ProgressListener;
import de.abas.infosystem.importit.dataset.Data;

import java.util.List;

public interface AbasDataProcessable {

	public void checkDataListStructure(List<Data> dataList) throws ImportitException;

	public void checkDataListValues(List<Data> dataList) throws ImportitException;

	public void importDataListTransaction(List<Data> dataList) throws ImportitException;

	public void startTransaction() throws ImportitException;

	public void abortTransaction() throws ImportitException;

	public void commitTransaction() throws ImportitException;

	public boolean isTransactionActive();
	public void importDataList(List<Data> dataList) throws ImportitException;

	public void addListener(ProgressListener toAdd);
}
