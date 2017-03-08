package de.abaspro.infosystem.importit;

import java.util.ArrayList;

public interface AbasDataProcessable {

	public void checkDataListStructure(ArrayList<Data> dataList) throws ImportitException;
	public void checkDataListValues(ArrayList<Data> dataList) throws ImportitException;
	public void importDataListTransaction(ArrayList<Data> dataList) throws ImportitException;
	public void startTransaction() throws ImportitException;
	public void abortTransaction() throws ImportitException;
	public void commitTransaction() throws ImportitException;
	public void importDataList(ArrayList<Data> dataList) throws ImportitException;
	
}
