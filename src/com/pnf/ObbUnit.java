package com.pnf;

import java.util.List;
import java.util.Map;

import com.pnfsoftware.jeb.core.actions.InformationForActionExecution;
import com.pnfsoftware.jeb.core.properties.IPropertyDefinitionManager;
import com.pnfsoftware.jeb.core.units.AbstractBinaryUnit;
import com.pnfsoftware.jeb.core.units.IInteractiveUnit;
import com.pnfsoftware.jeb.core.units.IUnit;
import com.pnfsoftware.jeb.core.units.IUnitProcessor;

public class ObbUnit extends AbstractBinaryUnit implements IInteractiveUnit {
	private ObbData image;

	public ObbUnit(ObbData image, String name, byte[] data, IUnitProcessor unitProcessor, IUnit parent) {
		super(null, data, PluginData.OBB_NAME, name, unitProcessor, parent, parent.getPropertyDefinitionManager());
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
		IUnit fatChildUnit = getUnitProcessor().process(this.getName(), this.getBytes(), this);
		this.getChildren().add(fatChildUnit);
		return true;
	}
	
	@Override
	public boolean executeAction(InformationForActionExecution info) {
		return false;
	}

	@Override
	public List<Integer> getItemActions(long id) {
		return null;
	}

	@Override
	public boolean prepareExecution(InformationForActionExecution info) {
		return false;
	}
}
