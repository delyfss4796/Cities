package me.xflyiwnl.cities.object.city;

import me.xflyiwnl.cities.object.Citizen;

import java.util.Map;
import java.util.UUID;

public interface CitizenList {

    Map<UUID, Citizen> getCitizens();

    void addCitizen(Citizen citizen);
    void removeCitizen(Citizen citizen);

}
