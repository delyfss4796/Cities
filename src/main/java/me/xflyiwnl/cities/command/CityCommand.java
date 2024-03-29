package me.xflyiwnl.cities.command;

import me.xflyiwnl.cities.Cities;
import me.xflyiwnl.cities.gui.city.CitizensGUI;
import me.xflyiwnl.cities.gui.city.CityOnlineGUI;
import me.xflyiwnl.cities.gui.rank.RankGUI;
import me.xflyiwnl.cities.object.city.City;
import me.xflyiwnl.cities.object.confirmation.Confirmation;
import me.xflyiwnl.cities.object.*;
import me.xflyiwnl.cities.object.invite.types.CityInvite;

import me.xflyiwnl.cities.object.land.Land;
import me.xflyiwnl.cities.object.land.LandType;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class CityCommand implements CommandExecutor, TabCompleter {

    private List<String> cityTabCompletes = Arrays.asList(
            "bank",
            "land",
            "rank",
            "online",
            "broadcast",
            "kick",
            "invite",
            "add",
            "create",
            "new",
            "leave",
            "remove",
            "spawn",
            "set",
            "citizens"
    );

    private List<String> citySetTabCompletes = Arrays.asList(
            "name",
            "board",
            "spawn"
    );

    private List<String> cityBankTabCompletes = Arrays.asList(
            "deposit",
            "withdraw"
    );

    private List<String> cityLandTabCompletes = Arrays.asList(
            "claim",
            "unclaim"
    );

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            return null;
        }

        Player player = (Player) sender;
        Citizen citizen = Cities.getInstance().getCitizen(player);

        if (args.length == 1) {
            return cityTabCompletes;
        }

        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case ("bank"):
                    return cityBankTabCompletes;
                case ("land"):
                    return cityLandTabCompletes;
                case ("set"):
                    return citySetTabCompletes;
                default:
                    return null;
            }
        }

        return null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;
        Citizen citizen = Cities.getInstance().getCitizen(player);

        if (citizen == null) {
            return true;
        }

        parseCityCommand(citizen, args);

        return true;
    }

    public void parseCityCommand(Citizen citizen, String[] args) {

        if (args.length == 0) {
            citizen.sendMessage(Translator.of("command.not-enough-args"));
            return;
        }

        switch (args[0].toLowerCase()) {
            case "bank":
                bankCommand(citizen, args);
                break;
            case "land":
                landCommand(citizen, args);
                break;
            case "rank":
                rankCommand(citizen, args);
                break;
            case "online":
                onlineCommand(citizen, args);
                break;
            case "broadcast":
                broadcastCommand(citizen, args);
                break;
            case "kick":
                kickCommand(citizen, args);
                break;
            case "invite":
                inviteCommand(citizen, args);
                break;
            case "add":
                inviteCommand(citizen, args);
                break;
            case "remove":
                removeCity(citizen);
                break;
            case "leave":
                leaveCommand(citizen, args);
                break;
            case "new":
                createCity(citizen, args);
                break;
            case "create":
                createCity(citizen, args);
                break;
            case "spawn":
                spawnCommand(citizen, args);
                break;
            case "set":
                setCommand(citizen, args);
                break;
            case "citizens":
                citizensCommand(citizen, args);
                break;
            default:
                citizen.sendMessage(Translator.of("command.unknown-arg"));
                break;
        }

    }

    public void setCommand(Citizen citizen, String[] args) {

        if (args.length < 2) {
            citizen.sendMessage(Translator.of("command.not-enough-args"));
            return;
        }

        switch (args[1].toLowerCase()) {
            case "name":
                setNameCommand(citizen, args);
                break;
            case "board":
                setBoardCommand(citizen, args);
                break;
            case "spawn":
                setSpawnCommand(citizen, args);
                break;
            default:
                citizen.sendMessage(Translator.of("command.unknown-arg"));
                break;
        }

    }

    public void setNameCommand(Citizen citizen, String[] args) {

        if (!citizen.hasCity()) {
            citizen.sendMessage(Translator.of("citizen.no-city"));
            return;
        }

        if (args.length < 3) {
            citizen.sendMessage(Translator.of("command.not-enough-args"));
            return;
        }

        City city = citizen.getCity();

        city.setName(args[2]);
        city.save();

        citizen.sendMessage(Translator.of("city.set.name")
                .replace("%name%", args[2]));

    }

    public void setBoardCommand(Citizen citizen, String[] args) {

        if (!citizen.hasCity()) {
            citizen.sendMessage(Translator.of("citizen.no-city"));
            return;
        }

        if (args.length < 3) {
            citizen.sendMessage(Translator.of("command.not-enough-args"));
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            sb.append(i + 1 == args.length ? args[i] : args[i] + " ");
        }

        citizen.getCity().setBoard(sb.toString());

        citizen.sendMessage(Translator.of("city.set.board")
                .replace("%board%", sb.toString()));

    }

    public void setSpawnCommand(Citizen citizen, String[] args) {

        if (!citizen.hasCity()) {
            citizen.sendMessage(Translator.of("citizen.no-city"));
            return;
        }

        City city = citizen.getCity();

        Location spawn = citizen.getPlayer().getLocation();

        Chunk chunk = spawn.getChunk();
        WorldCord2 cord2 = new WorldCord2(chunk.getWorld(), chunk.getX(), chunk.getZ());
        Land land = Cities.getInstance().getLand(cord2);

        if (land == null || !land.getCity().equals(city)) {
            citizen.sendMessage(Translator.of("city.far-spawn"));
            return;
        }

        Land currentSpawn = city.getSpawnLand();
        currentSpawn.setSpawnLand(false);

        city.setSpawn(spawn);
        city.setSpawnLand(land);
        land.setSpawnLand(true);


        currentSpawn.save();
        city.save();
        land.save();

        citizen.sendMessage(Translator.of("city.set.spawn"));

    }

    public void citizensCommand(Citizen citizen, String[] args) {

        if (!citizen.hasCity()) {
            citizen.sendMessage(Translator.of("citizen.no-city"));
            return;
        }

        City city = citizen.getCity();

        CitizensGUI.showGUI(citizen.getPlayer(), city, null);

    }

    public void rankCommand(Citizen citizen, String[] args) {

        if (!citizen.hasCity()) {
            citizen.sendMessage(Translator.of("citizen.no-city"));
            return;
        }

        City city = citizen.getCity();

        RankGUI.showGUI(citizen.getPlayer(), city);

    }

    public void onlineCommand(Citizen citizen, String[] args) {

        if (!citizen.hasCity()) {
            citizen.sendMessage(Translator.of("citizen.no-city"));
            return;
        }

        City city = citizen.getCity();

        CityOnlineGUI.showGUI(citizen.getPlayer(), city);

    }

    public void spawnCommand(Citizen citizen, String[] args) {

        if (!citizen.hasCity()) {
            citizen.sendMessage(Translator.of("citizen.no-city"));
            return;
        }

        City city = citizen.getCity();

        citizen.getPlayer().teleport(city.getSpawn());

        citizen.sendMessage(Translator.of("citizen.teleported-spawn"));

    }

    public void kickCommand(Citizen citizen, String[] args) {

        if (!citizen.hasCity()) {
            citizen.sendMessage(Translator.of("citizen.no-city"));
            return;
        }

        if (args.length < 2) {
            citizen.sendMessage(Translator.of("command.not-enough-args"));
            return;
        }

        Citizen receiver = Cities.getInstance().getCitizen(args[1]);

        if (receiver == null) {
            citizen.sendMessage(Translator.of("citizen.unknown-citizen"));
            return;
        }

        if (receiver.equals(citizen)) {
            citizen.sendMessage(Translator.of("citizen.kick-self"));
            return;
        }

        if (!receiver.hasCity()) {
            citizen.sendMessage(Translator.of("citizen.kick-error"));
            return;
        }

        if (!receiver.getCity().equals(citizen.getCity())) {
            citizen.sendMessage(Translator.of("citizen.kick-error"));
            return;
        }

        if (receiver.isMayor()) {
            citizen.sendMessage(Translator.of("citizen.kick-mayor"));
            return;
        }

        City city = citizen.getCity();

//        city.kickCitizen(citizen, receiver);
        city.save();

    }

    public void leaveCommand(Citizen citizen, String[] args) {

        if (!citizen.hasCity()) {
            citizen.sendMessage(Translator.of("citizen.no-city"));
            return;
        }

        if (citizen.isMayor()) {
            citizen.sendMessage(Translator.of("city.mayor-leave-city"));
            return;
        }

        City city = citizen.getCity();

        city.leaveCitizen(citizen);
        city.save();

    }

    public void inviteCommand(Citizen citizen, String[] args) {

        if (!citizen.hasCity()) {
            citizen.sendMessage(Translator.of("citizen.no-city"));
            return;
        }

        if (args.length < 2) {
            citizen.sendMessage(Translator.of("command.not-enough-args"));
            return;
        }

        Citizen receiver = Cities.getInstance().getCitizen(args[1]);

        if (receiver == null) {
            citizen.sendMessage(Translator.of("citizen.unknown-citizen"));
            return;
        }

        if (receiver.equals(citizen)) {
            citizen.sendMessage(Translator.of("invite.invite-self"));
            return;
        }

        if (receiver.hasCity()) {
            citizen.sendMessage(Translator.of("invite.receiver-has.city")
                    .replace("%city%", receiver.getCity().getName()));
            return;
        }

        if (receiver.hasInvite()) {
            citizen.sendMessage(Translator.of("invite.receiver-has-invite"));
            return;
        }

        CityInvite invite = new CityInvite(
                citizen.getCity(),
                citizen, receiver
        );

    }

    public void broadcastCommand(Citizen citizen, String[] args) {

        if (!citizen.hasCity()) {
            citizen.sendMessage(Translator.of("city.no-city"));
            return;
        }

        if (args.length < 2) {
            citizen.sendMessage(Translator.of("command.not-enough-args"));
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            sb.append(i + 1 == args.length ? args[i] : args[i] + " ");
        }

        citizen.getCity().broadcast(sb.toString(), true);

        citizen.sendMessage(Translator.of("citizen.broadcast-send"));

    }

    public void bankCommand(Citizen citizen, String[] args) {

        if (!citizen.hasCity()) {
            citizen.sendMessage(Translator.of("city.no-city"));
            return;
        }

        if (args.length == 1) {
            // todo open lands menu
            return;
        }

        City city = citizen.getCity();

        if (args.length < 3) {
            citizen.sendMessage(Translator.of("command.not-enough-args"));
            return;
        }

        double amount = Double.valueOf(args[2]);

        if (amount < 1 && amount > 1000000) {
            citizen.sendMessage(Translator.of("economy.amount-error"));
            return;
        }

        switch (args[1].toLowerCase()) {
            case "deposit":

                if (citizen.getBank().current() < amount) {
                    citizen.sendMessage(Translator.of("economy.not-enough-money.citizen"));
                    return;
                }

                citizen.getBank().pay(city.getBank(), amount);

                city.broadcast(Translator.of("economy.deposit-format")
                        .replace("%name%", city.getName())
                        .replace("%player%", citizen.getName())
                        .replace("%amount%", String.valueOf(amount)), false);

                break;
            case "withdraw":

                if (city.getBank().current() < amount) {
                    citizen.sendMessage(Translator.of("economy.not-enough-money.city"));
                    return;
                }

                city.getBank().pay(citizen.getBank(), amount);

                city.broadcast(Translator.of("economy.withdraw-format")
                        .replace("%name%", city.getName())
                        .replace("%player%", citizen.getName())
                        .replace("%amount%", String.valueOf(amount)), false);

                break;
            default:
                citizen.sendMessage(Translator.of("command.unknown-arg"));
                break;
        }

        city.save();

    }

    public void landCommand(Citizen citizen, String[] args) {

        if (!citizen.hasCity()) {
            citizen.sendMessage(Translator.of("citizen.no-city"));
            return;
        }

        if (args.length == 1) {
            return;
        }

        Chunk chunk = citizen.getPlayer().getChunk();
        WorldCord2 worldCord2 = new WorldCord2(chunk.getWorld(), chunk.getX(), chunk.getZ());
        Land claimed = Cities.getInstance().getLand(worldCord2);

        switch (args[1].toLowerCase()) {
            case "claim":

                if (claimed != null) {
                    citizen.sendMessage(Translator.of("land.already-claimed")
                            .replace("%city%", claimed.getCity().getName()));
                    return;
                }

                Land land = new Land(
                        worldCord2, LandType.DEFAULT, citizen.getCity()
                );

                if (!land.connected()) {
                    citizen.sendMessage(Translator.of("land.not-connected"));
                    return;
                }

                land.create();
                land.save();

                citizen.sendMessage(Translator.of("land.land-claim")
                        .replace("%world%", land.getCord2().getWorld().getName())
                        .replace("%x%", String.valueOf(land.getCord2().getX()))
                        .replace("%z%", String.valueOf(land.getCord2().getZ())));

                break;
            case "unclaim":

                if (claimed == null) {
                    citizen.sendMessage(Translator.of("land.free-land"));
                    return;
                }

                if (!claimed.getCity().equals(citizen.getCity())) {
                    citizen.sendMessage(Translator.of("land.already-claimed")
                            .replace("city", claimed.getCity().getName()));
                    return;
                }

                if (claimed.isSpawnLand()) {
                    citizen.sendMessage(Translator.of("land.unclaim-spawnland"));
                    return;
                }

                citizen.sendMessage(Translator.of("land.land-unclaim")
                        .replace("world", claimed.getCord2().getWorld().getName())
                        .replace("x", String.valueOf(claimed.getCord2().getX()))
                        .replace("z", String.valueOf(claimed.getCord2().getZ())));

                claimed.remove();

                break;
            default:
                citizen.sendMessage(Translator.of("command.unknown-arg"));
                break;
        }

        citizen.save();

    }

    public void createCity(Citizen citizen, String[] args) {

        if (args.length < 2) {
            citizen.sendMessage(Translator.of("command.not-enough-args"));
            return;
        }

        if (citizen.getCity() != null) {
            citizen.sendMessage(Translator.of("city.already-has-city")
                    .replace("%city%", citizen.getCity().getName()));
            return;
        }

        if (Cities.getInstance().getCity(args[1]) != null) {
            citizen.sendMessage(Translator.of("city.creation-name-error"));
            return;
        }

        City landCity = Cities.getInstance().getCityByLand(citizen);

        if (landCity != null) {
            citizen.sendMessage(Translator.of("land.already-claimed")
                    .replace("%city%", landCity.getName()));
            return;
        }

        City city = new City(args[1], 0, citizen, null, citizen.getPlayer().getLocation(), Cities.getInstance().getSettings().ofString("board.default"));

        Chunk chunk = citizen.getPlayer().getChunk();
        WorldCord2 worldCord2 = new WorldCord2(chunk.getWorld(), chunk.getX(), chunk.getZ());

        Land land = new Land(
                worldCord2, LandType.DEFAULT, city
        );

        city.setSpawnLand(land);
        land.setSpawnLand(true);

        Confirmation confirmation = new Confirmation(
                citizen,
                Translator.of("confirmation.confirmation-messages.city-create"),
                () -> {
                    land.create();
                    land.save();

                    citizen.setCity(city);
                    citizen.save();

                    city.addCitizen(citizen);

                    city.create();
                    city.save();

                    citizen.sendMessage(Translator.of("city.city-created")
                            .replace("%city%", citizen.getCity().getName()));
                },
                () -> {}
        );

    }

    public void removeCity(Citizen citizen) {

        if (citizen.getConfirmation() != null) {
            citizen.sendMessage(Translator.of("confirmation.has-confirmation"));
            return;
        }

        City city = citizen.getCity();

        if (!citizen.hasCity()) {
            citizen.sendMessage(Translator.of("citizen.no-city"));
            return;
        }

        if (!citizen.isMayor()) {
            citizen.sendMessage(Translator.of("citizen.not-mayor"));
            return;
        }

        Confirmation confirmation = new Confirmation(
                citizen,
                Translator.of("confirmation.confirmation-messages.city-remove"),
                () -> {
                    citizen.sendMessage(Translator.of("city.city-removed")
                            .replace("%city%", city.getName()));

                    city.remove();
                },
                () -> {}
        );

    }

}
