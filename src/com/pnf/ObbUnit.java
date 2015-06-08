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
	
	// Override superclass getDescription to show Obb-specific information
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
		// Retrieve raw bytes passed to this unit
		byte[] data = this.getBytes();
		
		// Remove the footer obb data to prevent UnitProcessor from creating an ObbUnit again
		ObbData.removeFooter(data);
		
		// Call unit processor on modified data (will return a FAT unit)
		IUnit fatChildUnit = getUnitProcessor().process(PluginData.FAT_IMAGE_NAME, data, this);
		
		// Add new FAT unit to this unit's list of children
		this.getChildren().add(fatChildUnit);
		return true;
	}
}
