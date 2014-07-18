package com.jakeconley.provo.backend;

import com.jakeconley.provo.functions.sorting.PreferencesClass;
import com.jakeconley.provo.functions.sorting.PreferencesRule;
import com.jakeconley.provo.utils.Utils;
import com.jakeconley.provo.utils.inventory.InventoryCoords;
import com.jakeconley.provo.utils.inventory.InventoryRange;
import com.jakeconley.provo.utils.inventory.InventoryType;
import com.jakeconley.provo.yaml.YamlFile;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

public class SortingPreferencesBackend
{
    public Set<String> FetchPublicItemGroups() throws Exception
    {
        YamlFile itemgroups = new YamlFile("plugins/Provo/sorting/itemgroups.yml");
        itemgroups.LoadWithDefault("/itemgroups.yml");
        return itemgroups.get().getKeys(false);
    }
    public List<String> FetchPublicItemGroup(String name) throws Exception
    {
        YamlFile itemgroups = new YamlFile("plugins/Provo/sorting/itemgroups.yml");
        itemgroups.LoadWithDefault("/itemgroups.yml");
        return itemgroups.get().getStringList(name);
    }
    
    public PreferencesRule PreferencesRuleFromYaml(ConfigurationSection section, InventoryType it) throws ProvoFormatException
    {
        InventoryCoords area_ini = InventoryCoords.FromString(section.getString("area_ini"), it);
        InventoryCoords area_fin = InventoryCoords.FromString(section.getString("area_fin"), it);
        if(area_ini == null) throw new ProvoFormatException("Invalid " + section.getCurrentPath() + ".area_ini");
        if(area_fin == null) throw new ProvoFormatException("Invalid " + section.getCurrentPath() + ".area_fin");
        
        InventoryRange.Type rangetype;
        try{ rangetype = InventoryRange.Type.valueOf(section.getString("area_type")); }
        catch(Exception e){ throw new ProvoFormatException("Invalid " + section.getCurrentPath() + ".area_type"); }
        
        InventoryRange range = new InventoryRange(area_ini, area_fin, rangetype);
        return new PreferencesRule(section.getInt("priority", 1), range, section.getString("type", "any"));
    }
    
    public Set<String> FetchPlayerPreferencesClasses(String uuid) throws Exception
    {
        YamlFile y = new YamlFile("plugins/Provo/sorting/player_classes/" + uuid + ".yml");
        y.LoadDefaultNew();
        return y.get().getKeys(false);
    }    
    public PreferencesClass FetchPlayerPreferencesClass(String uuid, String name) throws ProvoFormatException, Exception
    {
        YamlFile y = new YamlFile("plugins/Provo/sorting/player_classes/" + uuid + ".yml");
        y.LoadDefaultNew();
        
        InventoryType inventorytype;
        try{ inventorytype = InventoryType.valueOf(y.get().getString(name + ".type", null)); }
        catch(Exception e)
        {
            ProvoFormatException ex = new ProvoFormatException("Invalid class " + name + ".type");
            ex.setFilePath(y.getFile().getPath());
            throw ex;
        }
        
        List<PreferencesRule> rules = new LinkedList<>();
        try{ for(ConfigurationSection i : y.get().SectionalizeMapList(name + ".rules")){ rules.add(PreferencesRuleFromYaml(i, inventorytype)); } }
        catch(ProvoFormatException e){ e.setFilePath(y.getFile().getPath()); throw e; }
        
        return new PreferencesClass(name, inventorytype, rules);
    }
    public void WritePreferencesClass(String uuid, PreferencesClass v) throws Exception
    {
        //TODO: "getpath" function or some shit
        YamlFile y = new YamlFile("plugins/Provo/sorting/player_classes/" + uuid + ".yml");
        y.LoadDefaultNew();
    }
}
