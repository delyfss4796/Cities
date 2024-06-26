package me.xflyiwnl.cities.object.rank;

import me.xflyiwnl.cities.Cities;
import me.xflyiwnl.cities.CitiesAPI;
import me.xflyiwnl.cities.object.Citizen;
import me.xflyiwnl.cities.object.Government;
import me.xflyiwnl.cities.object.Identifyable;
import me.xflyiwnl.cities.object.Saveable;

import java.util.*;

public class Rank implements Identifyable, Saveable {

    private UUID uuid = UUID.randomUUID();
    private Government government;

    private String title;
    private List<PermissionNode> nodes = new ArrayList<PermissionNode>();
    private List<Citizen> citizens = new ArrayList<Citizen>();

    public Rank() {
    }

    public Rank(Government government, String title) {
        this.government = government;
        this.title = title;
    }

    public Rank(UUID uuid, Government government, String title, List<PermissionNode> nodes, List<Citizen> citizens) {
        this.uuid = uuid;
        this.government = government;
        this.title = title;
        this.nodes = nodes;
        this.citizens = citizens;
    }

    @Override
    public void create() {
        Map<UUID, Rank> ranks = CitiesAPI.getInstance().getRanksByGovernment(government);
        if (ranks == null)
            ranks = new HashMap<>();
        ranks.put(getUniqueId(), this);
        Cities.getInstance().getRanks().put(government.getUniqueId(), ranks);
    }

    @Override
    public void save() {
    }

    @Override
    public void remove() {
        Map<UUID, Rank> ranks = CitiesAPI.getInstance().getRanksByGovernment(government);
        if (ranks == null)
            ranks = new HashMap<>();
        ranks.remove(getUniqueId());
        Cities.getInstance().getRanks().put(government.getUniqueId(), ranks);
    }

    public boolean hasPermission(PermissionNode node) {
        return nodes.contains(node);
    }

    public void addPermission(PermissionNode node) {
        nodes.add(node);
    }

    public void removePermission(PermissionNode node) {
        nodes.remove(node);
    }

    public void setUniqueId(UUID uuid) {
        this.uuid = uuid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<PermissionNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<PermissionNode> nodes) {
        this.nodes = nodes;
    }

    public Government getGovernment() {
        return government;
    }

    public void setGovernment(Government government) {
        this.government = government;
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
    }

    public List<Citizen> getCitizens() {
        return citizens;
    }

    public void setCitizens(List<Citizen> citizens) {
        this.citizens = citizens;
    }
}
