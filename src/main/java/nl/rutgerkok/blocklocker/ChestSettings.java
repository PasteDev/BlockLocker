package nl.rutgerkok.blocklocker;

import java.util.Date;

import org.bukkit.Material;

import com.google.common.base.Optional;

/**
 * Represents all settings of the plugin.
 *
 */
public interface ChestSettings {

    /**
     * Gets whether the given attack type is allowed to destroy protections.
     *
     * @param attackType
     *            The attack type.
     * @return True if the attack type is allowed to destroy protections, false
     *         otherwise.
     */
    boolean allowDestroyBy(AttackType attackType);

    /**
     * Checks if the given material can be protected by any protection type.
     *
     * @param material
     *            The material to check.
     * @return True if the given material can be protected by any protection
     *         type, false otherwise.
     */
    boolean canProtect(Material material);

    /**
     * Checks if the given material can be protected by the given type.
     *
     * @param type
     *            The protection type.
     * @param material
     *            The material to check.
     * @return True if the protection can protect the given material, false
     *         otherwise.
     */
    boolean canProtect(ProtectionType type, Material material);

    /**
     * Gets the actual date that chests must have activity after. If a chest
     * doesn't have activity after this date, it is considered expired.
     * 
     * @return The date, or absent if chests never expire.
     */
    Optional<Date> getChestExpireDate();

    /**
     * Gets the default amount of ticks a door stays open before closing
     * automatically. When set to less than 1, the door is never closed
     * automatically. Players can override this value for a door.
     * 
     * @return The amount.
     */
    int getDefaultDoorOpenSeconds();

    /**
     * Gets the localized header for the given sign type, includes brackets and
     * colors.
     *
     * @param signType
     *            The type of the sign.
     * @return The header.
     */
    String getFancyLocalizedHeader(SignType signType);

    /**
     * Gets the type of the protection.
     *
     * @param material
     *            Material of the protection.
     * @return Type of the protection.
     */
    Optional<ProtectionType> getProtectionType(Material material);

    /**
     * Gets the localized header for the given sign type, without colors.
     * 
     * @param signType
     *            The type of the sign.
     * @return The header.
     */
    String getSimpleLocalizedHeader(SignType signType);

}
