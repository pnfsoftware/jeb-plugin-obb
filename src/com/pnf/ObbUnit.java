package com.pnf;

import java.util.Map;

import com.pnfsoftware.jeb.core.properties.IPropertyDefinitionManager;
import com.pnfsoftware.jeb.core.units.AbstractBinaryUnit;
import com.pnfsoftware.jeb.core.units.IUnit;
import com.pnfsoftware.jeb.core.units.IUnitProcessor;

public class ObbUnit extends AbstractBinaryUnit{
	private ObbData image;

	public ObbUnit(ObbData image, String name, byte[] data, IUnitProcessor unitProcessor, IUnit parent, IPropertyDefinitionManager pdm) {
		super(null, data, PluginData.OBB_NAME, name, unitProcessor, parent, pdm);
		this.image = image;
	}
	
	public String getDescription(){
		String[] keys = ObbData.DATA_KEYS;
		Map<String, String> data = image.getData();
		StringBuffer desc = new StringBuffer();
		
		desc.append("Type: " + PluginData.OBB_NAME + "\n");
		desc.append("\n");
		desc.append("Properties:\n");
		String tab = " - ";
		
		for(String s: keys){
			desc.append(tab + s + " := " + data.get(s) + "\n");
		}
		
		return desc.toString();
	}
	
	public boolean process(){
		byte[] data = this.getBytes();
		ObbData.removeFooter(data);
		
		IUnit fatChildUnit = getUnitProcessor().process(PluginData.FAT_IMAGE_NAME, data, this);
		this.getChildren().add(fatChildUnit);
		return true;
	}
}
