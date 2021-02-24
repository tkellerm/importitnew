package de.abas.infosystem.importit.datacheck;

import de.abas.eks.jfop.remote.EKSe;
import de.abas.jfop.base.buffer.BufferFactory;
import de.abas.jfop.base.buffer.GlobalTextBuffer;
import de.abas.jfop.base.buffer.UserTextBuffer;

import java.util.List;

public class EnumTypeCommand extends Enumeration {


    private static EnumTypeCommand instance;


    private EnumTypeCommand() {
        super();
        fillEnumeration();

    }
//TODO  make a normal Class not as Singleton
    public static synchronized EnumTypeCommand getInstance() {
        if (EnumTypeCommand.instance == null) {
            EnumTypeCommand.instance = new EnumTypeCommand();
        }
        return EnumTypeCommand.instance;
    }

    private void fillEnumeration() {

        List<EnumerationItem> listOfEnumItems = super.getListOfEnumItems();
        BufferFactory bufferFactory = BufferFactory.newInstance(true);
        GlobalTextBuffer globalTextbuffer = bufferFactory.getGlobalTextBuffer();
        UserTextBuffer userTextBuffer = bufferFactory.getUserTextBuffer();
        int cmdNameMax = globalTextbuffer.getIntegerValue("cmdNameMax");
        for (int i = 0; i < cmdNameMax; i++) {
            String descriptionVar = "xtnamebspr";
            String neutralNameVar = "xtnameneutral";
            String enumerationVar = "xtaufzaehlung";
            String description;
            String neutralName;
            if (!userTextBuffer.isVarDefined(descriptionVar)) {
                userTextBuffer.defineVar("Text", descriptionVar);
            }
            if (!userTextBuffer.isVarDefined(neutralNameVar)) {
                userTextBuffer.defineVar("Text", neutralNameVar);
            }
            if (!userTextBuffer.isVarDefined(enumerationVar)) {
                userTextBuffer.defineVar("A198", enumerationVar);
            }
            EKSe.assign("U|" + enumerationVar + " = \"(" + i + ")\"");
            boolean success = globalTextbuffer.getBooleanValue("success");
            if (success) {
                EKSe.formula("U|" + neutralNameVar + " = 'U|" + enumerationVar + "(L=\":\")'");
                neutralName = userTextBuffer.getStringValue(neutralNameVar);
                description = globalTextbuffer.getStringValue("cmdName" + i);
                EnumerationItem enumerationItem = new EnumerationItem(i, description, neutralName);
                listOfEnumItems.add(enumerationItem);
            }else{
                // Es gibt in dem Range nicht für jede Nummer ein Tippkommando
            }

        }
    }
}
