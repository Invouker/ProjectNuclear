package eu.invouk.projectnuclear.register;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class ModCapabilities {

    public ModCapabilities(IEventBus modBus) {
        modBus.addListener(this::registerCapabilities);
    }

    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlocksEntities.COAL_GENERATOR_TILE.get(), (tile, side) -> tile.getEnergyStorage());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlocksEntities.COAL_GENERATOR_TILE.get(), (tile, side) -> tile.getItemHandler());
    }
}
