package de.abaspro.infosystem.importit;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import jxl.Cell;
import jxl.Sheet;
import jxl.write.Label;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import de.abas.ceks.jedp.CantBeginEditException;
import de.abas.ceks.jedp.CantBeginSessionException;
import de.abas.ceks.jedp.CantSaveException;
import de.abas.ceks.jedp.EDPEditor;
import de.abas.ceks.jedp.EDPFactory;
import de.abas.ceks.jedp.EDPQuery;
import de.abas.ceks.jedp.EDPRuntimeException;
//JEDP API
import de.abas.ceks.jedp.EDPSession;
import de.abas.ceks.jedp.InvalidQueryException;
import de.abas.ceks.jedp.InvalidRowOperationException;
import de.abas.ceks.jedp.TransactionException;
import de.abas.eks.jfop.FOPException;
import de.abas.eks.jfop.remote.EKS;
//JFOP-API (basic classes FO, FOe, EKS, EKSe and others)
import de.abas.eks.jfop.remote.FOPRunnable;

public class ImportIt implements FOPRunnable {
/** 
 * "main"-method, called from abas-ERP 
 *  @param arg arg[0]="Test_fop" (name of this class); 
 *             arg[1],arg[2],...: given paramters
 *  @return exitcode (0 = ok)
 */
    @SuppressWarnings("empty-statement")
public int runFop(String[] arg) throws FOPException
{


    		 String Zellenstring;
              String yin =  EKS.Mvar("ydatafile");
              if (yin.contains(".xlsx")) {
            	  EKS.formel("M|yerrorfile=M|ydatafile<<\".xls"+"\""+"+\"error.xlsx \"");
			} else {
				  EKS.formel("M|yerrorfile=M|ydatafile<<\".xls"+"\""+"+\"error.xls\"");
			}
              
              
              String yout =EKS.Mvar("yerrorfile");
              int errory = 1;
              Integer yfehler = 0;
              Integer yok = 0;
              String evtvar=EKS.Tvar("evtvar");
              
              int yport=Integer.parseInt (EKS.Mvar("yport"));
              String datensatz="";
              boolean Tabellensatz=false;
               int ytababspalte=Integer.parseInt (EKS.Mvar("ytababspalte"));
              //int ytababspalte=0;
              String schluessel="";
              String TippKommando="";
              String db="";
              String gruppe="";
              String selektionalt="";
              String selektion="";
              Integer yoption=0;
              String binoption="";
              Integer failure=0;
              String dbsel="";
              Boolean ycellmodifiable=false;
              String Ueberschrift="";
              String Ftext="";
              Boolean notempty=false;
              Boolean xtippaktiv=false;
              int y= 0;
              String dbgroup="";
              

              String xdbgroup;
              // Versionshistorie
              // 1.0 Erste Veröffentlichte Version
              // 1.1 
              //1. Bugfix: Wenn Option "Datensätze anzeigen" gewählt wurde, so wurde bei einem erfolgreichen Import die Fehlerinfo in der Zeile nicht gelöscht. 
              //           Somit war ein Fehler eines Imports zuvor immer noch sichtbar
              //2. @skip in feldnamen eingeführt -> Übergehen der Spalte
              //3. Versionsfeld eingeführt -  Ruhe mit "Welche Version kann was?"
              //4.  erste Version für subeditor.-> klappt aber noch nicht
              //1.2
              // 1. Bugfix - > Importzähler korrigiert
              // 2.  Es wird nicht mehr nach Import jeder Tabellenzeile gespeichert. Erst beim nächsten Datensatz wird gespeichert!
              //    => Fibu Buchungen sind möglich zu importieren 
              //1.3 
              // 1.  Bugfix -> "Datensätze immer neu Anlegen" funktionierte nicht - korrigiert!
              //1.4
              // 1. Option Modifiable eingeführt - Alle Felder werden optional auf "änderbar" geprüft
              // 2. Optionen in Excel vorbelegbar über Zelle A2
              //    Binärcode wird angegeben und wird auch in IS angezeigt in neuem Feld!
              // 3. Auch ohne die Option "Datensätze anzeigen" wird im Fehlerfall die Fehlerzeile mit Selektion und DB ID dargestellt
              // 4. Bugfix -> Durch die Änderung des Speicherverhaltens in Version 1.2 wurde bei jedem Importfehler eine Java
              //    Exception ausgelöst. Dadurch wurden ab dieser Excelzeile keine weiteren Daten mehr importiert.
              // 5. TABABSPALTE wird nun mit 0 vorbelegt, wenn in Excel File als Leere Zelle vorhanden
              // 6. @modifiable als Spalten Option
              // 7. @notempty als Spalten Option
              //1.5 
              // 1.Bugfix beim EInlesen von tabellendaten
              // 2. Löschen von tabellendaten wird nun nicht mehr spearat gemacht sondern direkt beim editieren des Datensatzes vor dem Import der weiteren Daten
              // 3.  Bugfix bei Modifizierbarkeit in Tabellen (moifiable benötigt Tabellenzeilenangabe)
              // 4. Bugfix Anzeige Anzahl Datensätze korrigiert( war immer eins zu hoch)
              // 1.6 EDP Vesion reduziert auf 3.25 => 2008 auch lauffähig (alte Libs eingelinkt)
              // 2.0 Umstellung auf Poi
              
 // Version des Java Programms setzen
              EKS.formel("M|yversion=\""+"2.0"+"\"");
 
  // Workbook und Sheet öffnen                   
   try {
      
       
	   
	   // Import Workbook öffnen

    	  
//    	  Hier wird die poi.apache Bibliothek verwendet
    	  
    	  org.apache.poi.ss.usermodel.Workbook workbook;
          
          if (yin.contains(".xlsx")){
       	   	workbook = new XSSFWorkbook(new FileInputStream(yin)); 
          }else {
        	  workbook = new HSSFWorkbook(new FileInputStream(yin));
          }
          	
//          poi sheet öffnen
          
          org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);
          
//          Fehlerdatei mit poi wird poi vorangestellt.
          
          org.apache.poi.ss.usermodel.Workbook errorworkbook;
          
          if (yout.contains(".xlsx")) {
       	   errorworkbook = new XSSFWorkbook(new FileInputStream(yin));
          }else {
   		   errorworkbook = new HSSFWorkbook(new FileInputStream(yin));
   	   }
          
          org.apache.poi.ss.usermodel.Sheet errorsheet = errorworkbook.getSheetAt(0);



       
        
       
       
       
       // Datenbank:Gruppe abholen
          dbgroup = getdbgroup(sheet);
          db = getdb(sheet);
          gruppe = getgroup(sheet);
          TippKommando = getTippkomanndo(sheet);
	      xdbgroup = getxdbgroup(sheet);
              
    
     //Daten in Maske eintragen
       if (TippKommando.equals(""))
      {
             
       EKS.formel("M|ydb=\""+db+"\"");
       EKS.formel("M|ygruppe=\""+gruppe+"\"");
       }
       else
       {         
           EKS.formel("M|ydb=\""+TippKommando+"\"");   
           EKS.formel("M|ygruppe=\""+gruppe+"\"");
       }
       
       
       
    	   
    	EKS.formel("M|yzeilen="+ getAnzDatenzeilen(sheet));
        
       
       
       
       
       //ytababspalte=Integer.parseInt(EKS.Mvar("ytababspalte"));
       //Wenn evtvar = ydatafile
       if (evtvar.equals("ydatafile"))
       {
       // die Daten des Excelsheet genauer anschauen
       // Ab welcher Spalte stehen Tabellendaten
    	
       
   
	if (isZelleleer(sheet, 1, 0))
        {ytababspalte=0;
       }
      else{
   
    	   try {
    		   ytababspalte=Integer.parseInt(getZellenInhaltString(sheet, 1, 0));
		} catch (NumberFormatException e) {
			// TODO: handle exception
//			Es wurde keine Integerzahl in das Feld ausgegeben
			EKS.box("Das Feld in der Exceltabelle für die Tabellenfelder wurde falsch ausgefüllt" );
		}
            
           }
             
       // Ergebnis in Maske eintragen
       EKS.formel("M|ytababspalte="+ytababspalte);   
         //    ytababspalte=ytababspalte-2;
      // Optionscode einlesen
      //EKS.box("TAB\n"+ytababspalte);
       if (isZelleleer(sheet, 2, 0))
        {yoption=0;}
      else
        {   
           yoption=Integer.parseInt(getZellenInhaltString(sheet, 2, 0));}
      
      binoption="0000"+Integer.toBinaryString(yoption);
      binoption=binoption.substring ( (binoption.length()-5) ); 
      EKS.formel("M|yoption="+yoption);
      EKS.formel("M|yimmerneu=T|false");
      EKS.formel("M|ynofop=T|false");
      EKS.formel("M|ytransaction=T|false");
      EKS.formel("M|yloetab=T|false");
      EKS.formel("M|ymodifiable=T|false");

      if ( binoption.substring(4).equals("1")) { EKS.formel ("M|yimmerneu=T|true"); }
      if ( binoption.substring(3,4).equals("1")) { EKS.formel ("M|ynofop=T|true"); }
      if ( binoption.substring(2,3).equals("1")) { EKS.formel ("M|ytransaction=T|true"); }
      if ( binoption.substring(1,2).equals("1")) { EKS.formel ("M|yloetab=T|true"); }         
      if ( binoption.substring(0,1).equals("1")) { EKS.formel ("M|ymodifiable=T|true"); }
       }
      
       
       // Optioncode aufbauen
       if ((evtvar.equals("ynofop"))||(evtvar.equals("yimmerneu"))|| (evtvar.equals("ytransaction"))||(evtvar.equals("yloetab"))||(evtvar.equals("ymodifiable")))
       {
       // Immerneu -> 1
       // NoFop    -> 2
       // Rollback -> 4
       // Loe Tab  -> 8
       // Modifiable -> 16
       yoption=0;    
       if (EKS.Mvar("yimmerneu").equals("ja")) { yoption=yoption+1; }
       if (EKS.Mvar("ynofop").equals("ja")) { yoption=yoption+2; }
       if (EKS.Mvar("ytransaction").equals("ja")) { yoption=yoption+4; }
       if (EKS.Mvar("yloetab").equals("ja")) { yoption=yoption+8; }
       if (EKS.Mvar("ymodifiable").equals("ja")) { yoption=yoption+16; }
       EKS.formel ("M|yoption="+(yoption));
       }
       
       
       // Variablenwerte aus Maske abholen
     //  ytababspalte=Integer.parseInt (EKS.Mvar("ytababspalte"));
       // Varibale -1 , da mit 0 los geht und nicht mit 1
       if (ytababspalte>0) { ytababspalte=ytababspalte -1;        } 
       //EKS.box("TAB2\n"+ytababspalte);
       String ytabelle =EKS.Mvar("ytabelle");
       String yserver = EKS.Mvar("yserver");
       String ymandant=EKS.Mvar("ymandant");
       String ypasswort=EKS.Mvar("ypasswort");
       String ymodifiable =EKS.Mvar("ymodifiable");




       
    
       
       
                             
       // Tabelle erzeugen wenn Häkchen gesetzt
       if (evtvar.equals("ytabelle"))
       {
            //TabelleAufbauen();
            EKS.mache("MASKE ZEILE --");      
            if (TippKommando.equals(""))
            {
                if (EKS.Mvar("ytabelle").equals("ja"))
                {
                // EDP Session starten
                EDPSession session=SessionAufbauen(yserver,yport,ymandant,ypasswort);  
                if (session != null) {
                	// Tabelle aufbauen in Function
                    TabelleAufbauen(session,sheet,dbgroup);
                    session.endSession();
				}
                
                }
            }
            else
            {
                EKS.formel("M|ytabelle=T|false"); 
            }
            
       }
         
       // yimport  gedrückt
       if (evtvar.equals ("yimport"))
       {   
            EKS.formel("M|yok=0"); 
            EKS.formel("M|yfehler=0"); 
           // Tabelle löschen, wenn keine dargestellt werden soll
           if (EKS.Mvar("ytabelle").equals("nein"))
            {
              EKS.mache("MASKE ZEILE --");    
           }
            EDPSession session=SessionAufbauen(yserver,yport,ymandant,ypasswort);                                         
            EDPQuery edpQ1 = session.createQuery();
            EDPEditor edpE1 = session.createEditor();
            // Evtl Tabelle Löschen
             if (EKS.Mvar("ytransaction").equals("ja")) 
             {
                try {

                    // Daten lesen -> Durch alle Zeilen durchlaufen
                    session.startTransaction();
                } catch (TransactionException ex) {
                    Logger.getLogger(ImportIt.class.getName()).log(Level.SEVERE, null, ex);
                }
             }
           
            if (EKS.Mvar("yloetab").equals("ja"))
                  {
                
               //   Loeschtab(edpQ1,edpE1,sheet,dbgroup);
                  }
            // Feldbezeichner abholen
            int maxcolumn=getMaxCol(sheet)+1;
            String [] Felder = new String[getMaxCol(sheet)];
            // Die Überschriftenzeile Zellenweise durchlaufen
            for ( int x=0; x < getMaxCol(sheet); x++)						
            {
                    
                 Zellenstring = getZellenInhaltString(sheet, x, 1); 
                 if (x==0)
                 {
                        int klammeraffe =Zellenstring.indexOf("@");
                        if (klammeraffe>0)
                        {  
                            String dummy=Zellenstring;
                            Zellenstring =dummy.substring(0,klammeraffe);
                            schluessel=";@sort="+dummy.substring(klammeraffe+1);       
                        }
                        
                 }
                 // und in einem Array ablegen
                 Felder[x]= Zellenstring;
            } 
         
            for (y = 2 ; y <= getMaxRow(sheet) ; y++)
            {
               //  errory++;
                 
                 EKS.hinweis("-SOFORT \"Importiere ZEILE "+y+" "+ getZellenInhaltString(sheet, 0, y) +"     -> Erfolgreich "+yok+"      ->Fehler "+yfehler+" \"");
                 try {
                       for ( int x=0; x < getMaxCol(sheet); x++)						
                       {   
                            
                            Zellenstring = getZellenInhaltString(sheet, x, y); 
                              //@modifiable
                            if (Felder[x].indexOf("@modifiable")==-1) 
                            {ycellmodifiable=false;  }
                            else
                            { ycellmodifiable=true;
                            }
                            //@notempty  
                              if (Felder[x].indexOf("@notempty")==-1) 
                            {notempty=false;  }
                            else
                            { notempty=true;
                            }
                              //@ Spalten optionen raus filtern
                             Ueberschrift=Felder[x];
                             int at =Ueberschrift.indexOf("@");
                             if (at > 0) 
                             { 
                                  
                                  db = getdb(sheet);
                           Ueberschrift=Felder[x].substring(0,at);
                             }
                            
                            if ((Felder[x].indexOf("@skip")==-1) && !(notempty && isZelleleer(sheet, x, y)))
                           {  
                           
                            // Wenn erste Spalte angeschaut wird, dann selektion starten
                          
                                if(x==0)
                                 {
                                     
                                     // Wenn 1. Spalte leer, und Tabelleninfos
                                      if ((Zellenstring.equals(""))&(ytababspalte!=0)&(!datensatz.equals("")))
                                      {
                                     
                                      //  edpE1.beginEdit(datensatz);  
                                        Tabellensatz=true;
                                      }
                                      else
                                      {    
                                                                          
                                      // Wenn 1. Spalte leer und keine Tabelleninfos
                                          
                                      // 1. Spalte ist gefüllt -> selektion starten
                                        Tabellensatz=false;  
                                       //DB import
                                        if (TippKommando.equals(""))
                                        {
                                             selektion=Felder[x]+"=="+Zellenstring+schluessel;
                                             dbsel=Zellenstring;
                                           
                                        if ((!selektion.equals(selektionalt))|| (selektion.equals("")))
                                        {
                                         
                                         if ((!selektionalt.equals(""))&& (failure==0) )
                                         {
                                            //Datensatz abspeichern, da ein neuer Datensatz vorhanden ist.
                                             try {
                                             edpE1.endEditSave();
                                              failure=0;
                                                if (!Tabellensatz) { datensatz=edpE1.getEditRef();}
                                                yok ++;
                                                EKS.formel("M|yok="+yok);

//                                              EKS.box(String.valueOf(errory));
                                              errory++;
                                              errorsheet.getLastRowNum();                                       
                                              	delRowInSheet(errorsheet, errory);
                                               
                                              errorsheet.getLastRowNum();
                                               
//                                               bei poi wird nicht die anzahl der Rows reduziert sondern auf null gesetzt
//                                               errory--;
                                                if (ytabelle.equals("ja"))
                                                {
                                                  
                                                EKS.hole("MASKE ZEILE "+(y-2));
                                                EKS.formel("M|yicon=\"icon:ok\"");
                                                 }
                                             }
                                                 catch (CantSaveException e)
                                                 {
                                                    edpE1.endEditCancel();
                                                    yfehler ++;
                                                    failure=1;
                                                    EKS.formel("M|yfehler="+yfehler);
                                                    errory++;
                                                  
                                                   
                                                    errorsheet.getRow(errory).createCell(maxcolumn).setCellValue(e.getMessage());   
													  //errorworkbook.write();
                                                    
                                                    if (ytabelle.equals("nein"))
                                                    {
                                                        EKS.mache("MASKE ZEILE +");
                                                        EKS.formel("M|ysel=\""+dbsel+"\"");
                                                        EKS.formel("M|ydatensatz=\""+datensatz+"\"");
                                                        EKS.formel("M|ytfehler=\""+e.getMessage()+"\"");
                                                        EKS.formel("M|yicon=\"icon:stop\"");
                                                    }
                                                    else
                                                    {
                                                        EKS.hole("MASKE ZEILE "+(y-2));
                                                        EKS.formel("M|ytfehler=\""+e.getMessage()+"\"");
                                                        EKS.formel("M|yicon=\"icon:stop\"");
                                                    }
                                                 }

                                         }
                                        
                                                                                 
                                      
                                        selektionalt=selektion;
                                        edpQ1.startQuery(xdbgroup,"",selektion,"nummer,such,id");
                                        // Fehler im Edp zurücksetzen
                                        failure=0;
                                        if ((edpQ1.getRecordCount()==0) || (EKS.Mvar("yimmerneu").equals("ja")))
                                             {
                                          
                                            //Neu 
                                        
                                                 edpE1.beginEditNew(db,gruppe);
                                                if ((edpE1.fieldIsModifiable(Ueberschrift) && (ymodifiable.equals("ja") || ycellmodifiable) )|| (ymodifiable.equals("nein")&& !ycellmodifiable) )
                                                   {
                                                   Ftext=Ftext+"|"+Ueberschrift+"="+Zellenstring;
                                                     edpE1.setFieldVal(Ueberschrift, Zellenstring);
                                        
                                                 }
                                             }
                                             else
                                             {
                                         //   EKS.box("ändern");
                                                 // Ändern
                                                edpQ1.getNextRecord();
                                                 //EKS.box("1");
                                                 datensatz=edpQ1.getField(3);
                                                 //EKS.box("2");
                                                 edpE1.beginEdit(datensatz); 
                                                 //EKS.box("3");
                                                 if (EKS.Mvar("yloetab").equals("ja"))
                                                    {
                                                   //  EKS.box("ZEILEN\n"+edpE1.getRowCount());
                                                     if (edpE1.getRowCount() >0)
                                                     {
                                                     //    EKS.box("delete");
                                                        edpE1.deleteAllRows();
                                                       //  EKS.box("deletefertig");
                                                     }  
                                                    } 
                                                 
                                             }
                                        } // ende 1.1 - 5
                                        }
                                        else
                                        {
                                            if (xtippaktiv) {edpE1.endEditSave();}
                                         edpE1.beginEditCmd(TippKommando,""); 
                                         xtippaktiv=true;
                                      // if ((edpE1.fieldIsModifiable(Felder[x]) && ymodifiable.equals("ja"))|| ymodifiable.equals("nein") )
                                           if ((edpE1.fieldIsModifiable(Ueberschrift) && (ymodifiable.equals("ja") || ycellmodifiable )) || (ymodifiable.equals("nein")&& !ycellmodifiable) )
                                            {
                                            Ftext=Ftext+"|"+Ueberschrift+"="+Zellenstring;  
                                            edpE1.setFieldVal(0,Ueberschrift,Zellenstring);
                                            }
                                        }
                                      }
				  }

                                //Felder größer 0 => Datenfelder
                                  if (x>0)
                                  {
                                  // Tabellenimport - Tabellenzeile erzeugen
                      
                                  if ((ytababspalte!= 0) &(ytababspalte==x)&(TippKommando.equals("")))
                                  { 
                                      if (edpE1.hasTablePart())
                                      {
                                            
                                           
                                       edpE1.insertRow(edpE1.getRowCount()+1);
                                          
                                           
                                      }
                                      else
                                      {
                                          //Datensatz besitzt keine Tabelle
                                          EKS.formel("M|yfehler=Datensatz hat keine Tabelle");
                                      }    
                                   }
                                   
                                   // Kopfdaten
                                     //    EKS.box("\n"+ytababspalte+"\n"+x);
                                           if ((ytababspalte!= 0) &(x>=ytababspalte))
                                           {
                                                if (((ymodifiable.equals("nein")&& !ycellmodifiable) ) || (edpE1.fieldIsModifiable(edpE1.getRowCount(),Ueberschrift) && (ymodifiable.equals("ja")|| ycellmodifiable)))  
                                              {
                                          
                                               
                                                    edpE1.setFieldVal(edpE1.getRowCount(),Ueberschrift,Zellenstring);
                                                 
                                                } 
                                           }
                                           else
                                           {    
                                               if (!Tabellensatz)
                                               {
                                              
                                                     if ((edpE1.fieldIsModifiable(Ueberschrift) && (ymodifiable.equals("ja") || ycellmodifiable) )|| (ymodifiable.equals("nein")&& !ycellmodifiable) )
                                                   {
                                                           Ftext=Ftext+"|"+Ueberschrift+"="+Zellenstring;
                                                   edpE1.setFieldVal(Ueberschrift, Zellenstring); 
                                                       
                                                    
                                                   }
                                                   
                                                   
                                                   
                                               }
                                           }
                                      
                                   }
                              
                            }
                         }// ende für SKip Schleife
                        
//4.10.2009                              edpE1.endEditSave();
                              // Id des gespeicherten Datensatzes merken für später!
//24.04.2013         raus genommen
                       /*
                       if (!Tabellensatz) { datensatz=edpE1.getEditRef();}
                              yok ++; 
                              EKS.formel("M|yok="+yok);

                              errorsheet.removeRow(errory);
                              errory --;
                              if (ytabelle.equals("ja"))
                              {
                                  EKS.hole("MASKE ZEILE "+(y-1));
                                  EKS.formel("M|yicon=\"icon:ok\"");
                              //    EKS.formel("M|y")        ;
                              }
*/                              //24.04.2013 ende
                 
                       }
                                  catch (Exception e){
                           try {
                        	   edpE1.endEditCancel();
						} catch (EDPRuntimeException e2) {
							// Es war keine EDP-Session offen
//							nichtsmachen
						}     	  	
                           
                                             
                                                
                                                    yfehler ++;
                                                    failure=1;
                                                    EKS.formel("M|yfehler="+yfehler);
                                                    errory ++;
                                            new Label(maxcolumn,errory, e.getMessage());
                                                 
                                                    errorsheet.getRow(errory).createCell(maxcolumn).setCellValue(e.getMessage());
													  //errorworkbook.write();
                                                    if (ytabelle.equals("nein"))
                                                    {
                                                        EKS.mache("MASKE ZEILE +");
                                                        EKS.formel("M|ysel=\""+dbsel+"\"");
                                                        EKS.formel("M|ydatensatz=\""+datensatz+"\"");
                                                        EKS.formel("M|ytfehler=\""+e.getMessage()+"\"");
                                                        EKS.formel("M|yicon=\"icon:stop\"");
                                                    }
                                                    else
                                                    {
                                                        EKS.hole("MASKE ZEILE "+(y-1));
                                                        EKS.formel("M|ytfehler=\""+e.getMessage()+"\"");
                                                        EKS.formel("M|yicon=\"icon:stop\"");
                                                    }

                    }
              }
            // Den letzten Datensatz müssen wir auch noch
                try {
                    if (failure==0){

                   
                    edpE1.endEditSave();
                  
                      if (!Tabellensatz) { datensatz=edpE1.getEditRef();}
                                                yok ++;
                                                EKS.formel("M|yok="+yok);
                                               errory++;
                                               delRowInSheet(errorsheet, errory);
                                               
                                               //errorworkbook.write();
                                                if (ytabelle.equals("ja"))
                                                {
                                                   // EKS.box(y.toString());
                                                EKS.hole("MASKE ZEILE "+(y-2));
                                                EKS.formel("M|yicon=\"icon:ok\"");
                                                 }
           }
                } catch (CantSaveException e)

                         {
            
                    
                                         //           edpE1.endEditCancel();
                                                    yfehler ++;
                                                    failure=1;
                                                    EKS.formel("M|yfehler="+yfehler);
                                                    errory ++;
                                                  //  EKS.box("x"+String.valueOf(errory));
                                                    
                                                    errorsheet.getRow(errory).createCell(maxcolumn).setCellValue(e.getMessage());
													  //errorworkbook.write();
                                                    if (ytabelle.equals("nein"))
                                                    {
                                                        EKS.mache("MASKE ZEILE +");
                                                        EKS.formel("M|ysel=\""+dbsel+"\"");
                                                        EKS.formel("M|ydatensatz=\""+datensatz+"\"");
                                                        EKS.formel("M|ytfehler=\""+e.getMessage()+"\"");
                                                        EKS.formel("M|yicon=\"icon:stop\"");
                                                    }
                                                    else
                                                    {
                                                        EKS.hole("MASKE ZEILE "+(y-2));
                                                        EKS.formel("M|ytfehler=\""+e.getMessage()+"\"");
                                                        EKS.formel("M|yicon=\"icon:stop\"");
                                                    }
                                                }

              //session.commitTransaction();
//              workbook.  close();
              //errorworkbook.write();
        
                    
                       try {
                        //    errory++;
                    	   	delRowInSheet(errorsheet, errory);
                            
                    
                    FileOutputStream outs = new FileOutputStream(yout);        
                    errorworkbook.write(outs);
              
                    outs.close();
                               
                 
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(ImportIt.class.getName()).log(Level.SEVERE, null, ex);
                }catch (IOException ex) {
                    Logger.getLogger(ImportIt.class.getName()).log(Level.SEVERE, null, ex);
                }

                 
                
              if (EKS.Mvar("ytransaction").equals("ja")) 
              {
              int auswahl;
              do {
                    auswahl = EKS.menue("Daten\nendgültig speichern\nRollback");
                    if (auswahl == 1){try {
                        
                            session.commitTransaction();
                        } catch (TransactionException ex) {
                            Logger.getLogger(ImportIt.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    
                  } while (auswahl == 0);
              
              }
               EKS.hinweis("-SOFORT");
                    
              session.endSession();          
          }    
        
       
     } catch (IOException ex) 
        {
             EKS.box("Fehler\n"+ex.getMessage());
        } 
            

    return 0;
}

   
	private void delRowInSheet(org.apache.poi.ss.usermodel.Sheet sheet,
		int rowNumber) {
	// TODO Auto-generated method stub
		Row row = sheet.getRow(rowNumber);
		if (row != null) {
			sheet.removeRow(row);
		}
	
}


	private int getMaxCol(org.apache.poi.ss.usermodel.Sheet sheet) {
	// Anzahl Spalten(Cols) in der 2. Zeile (entspricht 1) 
		 Integer maxcol = sheet.getRow(1).getPhysicalNumberOfCells();
	return maxcol;
}


	private int getMaxRow(org.apache.poi.ss.usermodel.Sheet sheet) {
		// TODO Auto-generated method stub
			
		return sheet.getLastRowNum();
}

	private int getMaxRow(Sheet sheet) {
		// TODO Auto-generated method stub
	return sheet.getRows();
}	

	private void TabelleAufbauen(EDPSession session,
		org.apache.poi.ss.usermodel.Sheet sheet, String dbgroup) {
	// TODO Auto-generated method stub
		

        
	    EDPQuery edpQT1 = session.createQuery();
	    String schluessel = "";
	    String xdbgroup="";
	    String selfield=getZellenInhaltString(sheet, 0, 1);
	    int klammeraffe =selfield.indexOf("@");
	    if (klammeraffe>0)
	            {  
	               String dummy=selfield;
	               selfield =dummy.substring(0,klammeraffe);
	               schluessel=";@sort="+dummy.substring(klammeraffe+1);       
	               
	            }
	                        
	    xdbgroup=dbgroup;
	    // Schleife über alle Zeilen
	   
	    for (int y = 2 ; y <= getMaxRow(sheet) ; y++)
	    {	
	    	getZellenInhaltString(sheet, 0, y);
	        EKS.hinweis("-SOFORT \"Lese ZEILE "+y+" "+getZellenInhaltString(sheet, 0, y)+"\"");
	        // Zeile erzeugen
	        EKS.mache("MASKE ZEILE +");
	        EKS.formel("M|ysel=\""+getZellenInhaltString(sheet, 0, y)+"\"");
	        
	        // Selektion aufbauen leere Spalte A übergehen ( Könnte auch ein tabellenimport sein mit leerer Spalte A)
	        if (!(isZelleleer(sheet, 0, y)))
	        {    
	            String selektionT=selfield+"=="+getZellenInhaltString(sheet, 0, y)+";@sort="+schluessel;
	            // Selektion starten
	            try {


	    
	        int doppelpunkt =dbgroup.indexOf(":");
	       if (doppelpunkt>0)
	       {
	    
	       String db =dbgroup.substring(0,doppelpunkt);
	       String gruppe=dbgroup.substring((doppelpunkt+1));
	      

	        if ((db.equals("Firma")) || (db.equals("12"))  && (gruppe.equals("Sachmerkmal-Leiste")))
	              {
	            xdbgroup="Firma:Selektionsrohling";
	              }
	       }

	            edpQT1.startQuery(xdbgroup, "", selektionT, "id");
	                
	                } catch (InvalidQueryException ex) 
	                    {
	                    EKS.box("Fehler\n"+ex.getMessage());
	                    Logger.getLogger(ImportIt.class.getName()).log(Level.SEVERE, null, ex);
	                    }
	                // Selektion erfolgreich?
	                if (edpQT1.getRecordCount()==1){
	                //Datensatz
	                edpQT1.getNextRecord();
	                // Datensatz eintragen über die id
	                EKS.formel("M|ydatensatz=\""+edpQT1.getField(1)+"\"");    
	                }
	        } 
	     }
	    
	
}


	private String getZellenInhaltString(Sheet sheet, int x, int y) {
		
		return sheet.getCell(x, y).getContents();
	// TODO Auto-generated method stub
	
}



	private Boolean isZelleString(Sheet sheet, int x, int y) {
	// TODO Auto-generated method stub
	 
	return (sheet.getCell(x,y).getType() == jxl.CellType.STRING_FORMULA);
}



	private Boolean isZelleDatum(Sheet sheet, int x, int y) {
	// TODO Auto-generated method stub
	return (sheet.getCell(x,y).getType() == jxl.CellType.DATE);
}



	private Boolean isZelleleer(Sheet sheet, int x, int y) {
	// Prüft ob die Zelle leer ist
		int numCols = sheet.getColumns();
		if ((numCols-1) > x) {
			Cell cell = sheet.getCell(x, y);
			if (cell != null) {
				cell.getType();
			}
			return (sheet.getCell(x,y).getType() == jxl.CellType.EMPTY);
		}else {
			return true;
		}
		
	
}



	private int getAnzDatenzeilen(Sheet sheet) {
    	// holt alle gefüllten Zeilen aus dem sheet
    	// laut Definition sind die ersten beidenZeilen nur für die Konfiguration 
    	int i = sheet.getRows();
    	if (i > 2) {
    		return sheet.getRows()-2;		
		}else {
			return 0;
		}
    
}

	
//	private Boolean isZelleDatum(org.apache.poi.ss.usermodel.Sheet sheet, int x, int y) {
//		// TODO Auto-generated method stub
//		
//		if (sheet.getRow(x).getCell(y).getCellType() == org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC){
//			if (sheet.getRow(x).getCell(y).getCellStyle() == org.apache.poi.ss.usermodel.CellStyle.)
//		}
//		
//		return (sheet.getRow(x).getCell(y).getCellType() == org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC);
//	}
	
	private Boolean isZelleleer(org.apache.poi.ss.usermodel.Sheet sheet, int x, int y) {
		// Prüft ob die Zelle leer ist
		org.apache.poi.ss.usermodel.Cell cell = sheet.getRow(y).getCell(x);
		
		if (cell == null) {
			return true;
		}else {
			if (sheet.getRow(y).getCell(x).getCellType() == org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BLANK){
				return true;
			}else {
				if (getZellenInhaltString(sheet, x, y).equals("")) {
					return true;
				}else {
					if (getZellenInhaltString(sheet, x, y) == null) {
						return null;
					}else {
						return false;
					}
				}
			}
		}
					
	}
	
private String getZellenInhaltString(org.apache.poi.ss.usermodel.Sheet sheet, int x, int y) {
//		Hier werden alle Inhaltsmöglichkeiten einer Celle in einen String umgewandelt 
		org.apache.poi.ss.usermodel.Cell cell = sheet.getRow(y).getCell(x);
		if (cell != null) {
			
			if ( cell.getCellType() == org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING)  {
				return cell.getStringCellValue();
			}else {
		
				if ( cell.getCellType() == org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC)  {
			
					Double nummericvalue = cell.getNumericCellValue();
					Integer intvalue = nummericvalue.intValue();
					if 	(intvalue.doubleValue()  == nummericvalue){
						return intvalue.toString();
					}else {
						return nummericvalue.toString();	
				}
				
			}else {
				if ( cell.getCellType() == org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BOOLEAN){
					if (cell.getBooleanCellValue() == true) {
						return "ja";
					}else {
						return "nein";
					}
				}else {
						if ( cell.getCellType() == org.apache.poi.ss.usermodel.Cell.CELL_TYPE_FORMULA){
								if (cell.getCachedFormulaResultType() == org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING) {
										return cell.getStringCellValue();
								}else {
									if (cell.getCachedFormulaResultType() == org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC) {
										Double nummericvalue = cell.getNumericCellValue();
										return nummericvalue.toString();
									}else {
										if ( cell.getCachedFormulaResultType() == org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BOOLEAN){
											if (cell.getBooleanCellValue() == true) {
													return "ja";
											}else {
													return "nein";
											}
										}else {
											if ( cell.getCachedFormulaResultType() == org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BLANK){
												return "";
											}else {
												if ( cell.getCachedFormulaResultType() == org.apache.poi.ss.usermodel.Cell.CELL_TYPE_ERROR){
														return null;
												}
											}
										}
									}
								}
							}else {
								if ( cell.getCellType() == org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BLANK){
									return "";
								}else {
									if ( cell.getCellType() == org.apache.poi.ss.usermodel.Cell.CELL_TYPE_ERROR){
										return null;
									}
									
								}
		
	
							}	
	
					}
				}
			}
		
		}
	// Falls irgendein Fall vergessen wurde wird null übertragen
		return null;
	
}
	
	

	private int getAnzDatenzeilen(org.apache.poi.ss.usermodel.Sheet poisheet) {
	// holt alle gefüllten Zeilen aus dem sheet
	// laut Definition sind die ersten beidenZeilen nur für die Konfiguration 
		return poisheet.getPhysicalNumberOfRows()-2;
}



	private String getxdbgroup(org.apache.poi.ss.usermodel.Sheet sheet) {
	// wenn es Sachmerkmalsleisten sind, dann soll es eine Rückgabe geben
    	
    	String db = getdb(sheet);
    	String group = getgroup(sheet); 
    	
    	if ((db.equals("Firma")) || (db.equals("12"))  && (group.equals("Sachmerkmal-Leiste")))
        {
         return "Firma:Selektionsrohling";
        }else {
			return getdbgroup(sheet);
		}
 }
    	




	private String getTippkomanndo(org.apache.poi.ss.usermodel.Sheet sheet) {
		
//		Es ist ein Tipkommdo, wenn es keinen Doppelpunkt besitzt 		
		String dbgroup = getdbgroup(sheet);
		int doppelpunkt =dbgroup.indexOf(":");
        
        if (doppelpunkt == 0)
     
        {
        	return dbgroup;
        	
        }else {
			return "";
		}
        
}



	private String getgroup(org.apache.poi.ss.usermodel.Sheet sheet) {
		
//		nach dem Doppelpunkt steht die Datenbankgruppe
//		leerer String wenn es ein Tippkommando ist
		
		String dbgroup = getdbgroup(sheet);
		int doppelpunkt =dbgroup.indexOf(":");
        
        if (doppelpunkt>0)
     
        {
     
        	return dbgroup.substring((doppelpunkt+1));
        }else{
            return "";
        }
    }
	



	private String getdb(org.apache.poi.ss.usermodel.Sheet sheet) {
//		vor dem Doppelpunkt steht die Datenbank
//		leerer String wenn es ein Tippkommando ist
		
		String dbgroup = getdbgroup(sheet); 
		
		int doppelpunkt =dbgroup.indexOf(":");
        
        if (doppelpunkt>0)
     
        	{
     
        	return dbgroup.substring(0,doppelpunkt);
        
        	}else {
        		return "";
        		}
        }


	private String getdbgroup(org.apache.poi.ss.usermodel.Sheet sheet) {
	// TODO Auto-generated method stub

    	 
		
//		return sheet.getRow(0).getCell(0).getStringCellValue();
		return getZellenInhaltString(sheet, 0, 0);
         
        
}

	 private String getxdbgroup(Sheet sheet) {
			// wenn es Sachmerkmalsleisten sind, dann soll es eine Rückgabe geben
		    	
		    	String db = getdb(sheet);
		    	String group = getgroup(sheet); 
		    	
		    	if ((db.equals("Firma")) || (db.equals("12"))  && (group.equals("Sachmerkmal-Leiste")))
		        {
		         return "Firma:Selektionsrohling";
		        }else {
					return getdbgroup(sheet);
				}
		 }
		    	




			private String getTippkomanndo(Sheet sheet) {
				
//				Es ist ein Tipkommdo, wenn es keinen Doppelpunkt besitzt 		
				String dbgroup = getdbgroup(sheet);
				int doppelpunkt =dbgroup.indexOf(":");
		        
		        if (doppelpunkt == 0)
		     
		        {
		        	return dbgroup;
		        	
		        }else {
					return "";
				}
		        
		}



			private String getgroup(Sheet sheet) {
				
//				nach dem Doppelpunkt steht die Datenbankgruppe
//				leerer String wenn es ein Tippkommando ist
				
				String dbgroup = getdbgroup(sheet);
				int doppelpunkt =dbgroup.indexOf(":");
		        
		        if (doppelpunkt>0)
		     
		        {
		     
		        	return dbgroup.substring((doppelpunkt+1));
		        }else{
		            return "";
		        }
		    }
			



			private String getdb(Sheet sheet) {
//				vor dem Doppelpunkt steht die Datenbank
//				leerer String wenn es ein Tippkommando ist
				
				String dbgroup = getdbgroup(sheet); 
				
				int doppelpunkt =dbgroup.indexOf(":");
		        
		        if (doppelpunkt>0)
		     
		        	{
		     
		        	return dbgroup.substring(0,doppelpunkt);
		        
		        	}else {
		        		return "";
		        		}
		        }


			private String getdbgroup(Sheet sheet) {
			// TODO Auto-generated method stub
	    	
		        return getZellenInhaltString(sheet , 0 , 0); 
		        
		}	


	private void Loeschtab(EDPQuery edpQ1, EDPEditor edpE1, Sheet sheet,String dbgroup) {
   //     throw new UnsupportedOperationException("Not yet implemented");
     String selfield=getZellenInhaltString(sheet, 0, 1);
     for (int y = 2 ; y < sheet.getRows() ; y++)
          {
            if (!sheet.getCell(0,y).getContents().equals(""))
             { 
             EKS.hinweis("-SOFORT \"Lösche Tabelle ZEILE "+y+" "+ getZellenInhaltString(sheet, 0, y)+"\"");
              // Selektion aufbauen
             String selektionT=selfield+"=="+getZellenInhaltString(sheet, 0, y);
              // Selektion starten
              try {
                     edpQ1.startQuery(dbgroup, "", selektionT, "nummer,such,id");
                  } catch (InvalidQueryException ex) 
                    {
                    Logger.getLogger(ImportIt.class.getName()).log(Level.SEVERE, null, ex);
                    }
                 // Selektion erfolgreich?
                 if (edpQ1.getRecordCount()==1)
                    {
                        //Datensatz
                        edpQ1.getNextRecord();
                try {
                    // Datensatz editieren
                    edpE1.beginEdit(edpQ1.getField(3));
                } catch (CantBeginEditException ex) {
                    Logger.getLogger(ImportIt.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    //Tabelle löschen
                    edpE1.deleteAllRows();
                } catch (InvalidRowOperationException ex) {
                    Logger.getLogger(ImportIt.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    edpE1.endEditSave();
                } catch (CantSaveException ex) {
                    Logger.getLogger(ImportIt.class.getName()).log(Level.SEVERE, null, ex);
                }
                         
                    }
           }   
     }
    }

    private EDPSession SessionAufbauen(String yserver, int yport, String ymandant, String ypasswort) 
    { 
      EDPSession  session = EDPFactory.createEDPSession ();
        try {
            session.beginSession(EKS.Mvar("yserver"), yport, EKS.Mvar("ymandant"), EKS.Mvar("ypasswort"), "JEDP_0001");
            } catch (CantBeginSessionException ex) 
                {
                EKS.box("FEHLER\n EDP Session kann nicht gestartet werden\n"+ex.getMessage());
                Logger.getLogger(ImportIt.class.getName()).log(Level.SEVERE, null, ex);
                return null;
                }
               
           // FOP Mode ein /ausschalten
            if (EKS.Mvar("ynofop").equals("ja"))
            {
            session.setFOPMode(false);        
            }
            else
            {
            session.setFOPMode(true);              
            }
            
      return session;
    }

    private void TabelleAufbauen(EDPSession session,Sheet sheet,String dbgroup) {
        
    EDPQuery edpQT1 = session.createQuery();
    String schluessel = "";
    String xdbgroup="";
    String selfield=getZellenInhaltString(sheet, 0, 1);
    int klammeraffe =selfield.indexOf("@");
    if (klammeraffe>0)
            {  
               String dummy=selfield;
               selfield =dummy.substring(0,klammeraffe);
               schluessel=";@sort="+dummy.substring(klammeraffe+1);       
               
            }
                        
    xdbgroup=dbgroup;
    // Schleife über alle Zeilen
   
    for (int y = 2 ; y < sheet.getRows() ; y++)
    {
        EKS.hinweis("-SOFORT \"Lese ZEILE "+y+" "+getZellenInhaltString(sheet, 0, y)+"\"");
        // Zeile erzeugen
        EKS.mache("MASKE ZEILE +");
        EKS.formel("M|ysel=\""+getZellenInhaltString(sheet, 0, y)+"\"");
        // Selektion aufbauen leere Spalte A übergehen ( Könnte auch ein tabellenimport sein mit leerer Spalte A)
        if (!(isZelleleer(sheet, 0, y)))
        {    
            String selektionT=selfield+"=="+getZellenInhaltString(sheet, 0, y)+";@sort="+schluessel;
            // Selektion starten
            try {


    
        int doppelpunkt =dbgroup.indexOf(":");
       if (doppelpunkt>0)
       {
    
       String db =dbgroup.substring(0,doppelpunkt);
       String gruppe=dbgroup.substring((doppelpunkt+1));
      

        if ((db.equals("Firma")) || (db.equals("12"))  && (gruppe.equals("Sachmerkmal-Leiste")))
              {
            xdbgroup="Firma:Selektionsrohling";
              }
       }

            edpQT1.startQuery(xdbgroup, "", selektionT, "id");
                
                } catch (InvalidQueryException ex) 
                    {
                    EKS.box("Fehler\n"+ex.getMessage());
                    Logger.getLogger(ImportIt.class.getName()).log(Level.SEVERE, null, ex);
                    }
                // Selektion erfolgreich?
                if (edpQT1.getRecordCount()==1){
                //Datensatz
                edpQT1.getNextRecord();
                // Datensatz eintragen über die id
                EKS.formel("M|ydatensatz=\""+edpQT1.getField(1)+"\"");    
                }
        } 
     }
    }


}
