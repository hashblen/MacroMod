package com.hashblen.macromod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Loader;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import static com.hashblen.macromod.CSVManip.*;
import static com.hashblen.macromod.MacroMod.macroName;
import static com.hashblen.macromod.MacroMod.path;

public class MenuGUI extends GuiScreen {

    //Minecraft mc = Minecraft.getMinecraft();

    public MenuGUI(){
        this.lineList = CSVManip.linesToMacroLines(path + macroName);
    }

    GuiButton close;
    GuiButton save;
    GuiTextField name;
    GuiButton load;
    MacroFile lines;
    GuiButton addRow;
    GuiButton duplicateRow;
    GuiButton deleteRow;
    @SuppressWarnings("FieldMayBeFinal")
    private List<MacroLine> lineList;

    private int selected = -1;

    public void initGui(){
        this.buttonList.add(this.close = new GuiButton(1, (int)(width*0.95)-100, (int)(height*0.05), 100, 20, "\247cClose"));
        this.buttonList.add(this.save = new GuiButton(2, (int)(width*0.95)-100, (int)(height*0.05)+60, 100, 20, "\247aSave"));
        this.name = new GuiTextField(2, mc.fontRendererObj, (int)(width*0.95)-200, (int)(height*0.05)+30, 100, 20);
        name.setMaxStringLength(23);
        name.setText(macroName);

        this.buttonList.add(this.load = new GuiButton(3, (int)(width*0.95)-100, (int)(height*0.05)+30, 100, 20, "Load"));

        this.buttonList.add(this.addRow = new GuiButton(4, (int)(width*0.95)-100, (int)(height*0.05)+90, 100, 20, "\247bAdd Row"));
        this.buttonList.add(this.duplicateRow = new GuiButton(5, (int)(width*0.95)-100, (int)(height*0.05)+110, 100, 20, "\247dDuplicate Row"));
        this.buttonList.add(this.deleteRow = new GuiButton(2, (int)(width*0.95)-100, (int)(height*0.05)+130, 100, 20, "\2474Delete Row"));

        this.lines = new MacroFile(this, lineList, 340);
        lines.initLines();
    }

    public void mouseClicked (int mouseX, int mouseY, int mouseButton){
        if(close.isMouseOver()){
            //noinspection RedundantCast
            mc.displayGuiScreen((GuiScreen) null);
        }
        if(load.isMouseOver()){
            macroName=name.getText();
            macroName = cvsExt(macroName);
            name.setText(macroName);
            createFile(Paths.get(path + macroName));

            this.lineList = CSVManip.linesToMacroLines(path + macroName);
            this.lines = new MacroFile(this, lineList, 340);
            lines.initLines();

            System.out.println("Macro name changed to: " + macroName);
            saveLastToCfg();
        }
        if(save.isMouseOver()){
            macroName=name.getText();
            macroName = cvsExt(macroName);
            name.setText(macroName);
            createFile(Paths.get(path + macroName));
            writeLines(macroLinesToLines(lineList), path + macroName);
            System.out.println("Macro saved to: " + macroName);
            saveLastToCfg();
        }
        int index = selected;
        if(index==-1){
            index = lines.getSize()-1;
        }
        if(addRow.isMouseOver()){
            lines.addLine(index+1);
        }
        if(duplicateRow.isMouseOver()){
            lines.duplicateLine(index+1);
        }
        if(deleteRow.isMouseOver()){
            lines.deleteLine(index);
        }
        this.name.mouseClicked(mouseX, mouseY, mouseButton);
        this.lines.mouseClicked(mouseX, mouseY, mouseButton);
    }

    protected void keyTyped(char par1, int par2) throws IOException {
        super.keyTyped(par1, par2);
        this.name.textboxKeyTyped(par1, par2);
        this.lines.keyTyped(par1, par2);
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks){
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.drawCenteredString(mc.fontRendererObj, "Edit \2476Macro", width/2, (int)(height*0.05), Color.WHITE.getRGB());
        this.drawString(mc.fontRendererObj, "W   A    S   D Sprint Sneak Jump   Yaw      Pitch    LMB RMB", 52, 20, Color.WHITE.getRGB());
        this.name.drawTextBox();
        this.lines.drawScreen(mouseX, mouseY, partialTicks);
    }

    public void updateScreen()
    {
        super.updateScreen();
        this.name.updateCursorCounter();
        this.lines.updateScreen();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    Minecraft getMinecraftInstance()
    {
        return mc;
    }

    public void selectBoxIndex (int index){
        if (index == this.selected)
            return;
        this.selected = index;
    }

    public boolean boxIndexSelected(int index)
    {
        return index == selected;
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        macroName = cvsExt(macroName);
        createFile(Paths.get(path + macroName));
        writeLines(macroLinesToLines(lineList), path + macroName);
        System.out.println("Macro saved to: " + macroName);
        saveLastToCfg();
    }
    private void saveLastToCfg(){
        File configFile = new File(Loader.instance().getConfigDir(), "macromod.cfg");
        Configuration config = new Configuration(configFile);
        config.load();
        Property mName = config.get("CSV", "lastFile", "default.csv");
        macroName = cvsExt(macroName);
        mName.set(macroName);
        if(config.hasChanged())
            config.save();
    }

    private String cvsExt(String name){
        String toReturn = name;
        if(!name.endsWith(".csv")){
            toReturn = name + ".csv";
        }
        return toReturn;
    }
}
