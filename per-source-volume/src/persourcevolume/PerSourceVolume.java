package persourcevolume;

import java.util.Map;
import hmgapi.hook.ModContext;
import hmgapi.hook.ModEntry;

/**
 * Per-source volume for Gen5W builds whose MicomAudioService links every media
 * source to one level ("new volume group" mode).
 *
 * Kia's code groups volume slots into types: VolumeGroupTable maps slot -> type,
 * mapVolumeGroup maps type -> its slots. Every media slot is type 0, so a level
 * change is copied to all of them (setVolumeInternal), saved to all of them
 * (VolumeGroupStoreData.insertVolume), and the amp check treats any media slot
 * as "the playing source" (type equality).
 *
 * This gives each media slot its own type with itself as the only member, the
 * same shape as stock one-slot types (navigation warning, CarPlay alert, beep).
 * Copy, save and amp check then all act on the one slot. Done in the zygote, so
 * system_server is forked with the tables already changed.
 */
public final class PerSourceVolume extends ModEntry {

    // New types start at this type ID, well clear of Kia's 0-13.
    private static final int FIRST_TYPE = 1000;

    @Override
    @SuppressWarnings("unchecked")
    public void zygoteInit(ModContext mod) throws Throwable {
        Map<Integer, Integer> table;
        Map<Integer, int[]> groups;
        int[] media;
        Integer mediaType = null;

        try {
            // Load volume tables, and find which type number is "media" on this build
            Class<?> c = Class.forName("com.hkmc.media.MicomAudioManagerConstant");
            table  = (Map<Integer, Integer>) c.getField("VolumeGroupTable").get(null);
            groups = (Map<Integer, int[]>)   c.getField("mapVolumeGroup").get(null);
            media  = (int[])                 c.getField("arr_Group1_Media").get(null);

            for (Map.Entry<Integer, int[]> e : groups.entrySet())
                if (e.getValue() == media) mediaType = e.getKey();

        } catch (Exception e) {
            mod.log("Error loading volume tables not applying on this build: " + e);
            return;
        }

        if (mediaType == null) {
            mod.log("Media volume group type not found, nothing to do");
            return;
        }

        // One type per media slot: VolumeGroupTable[slot] = type, mapVolumeGroup[type] = { slot }.
        // Only creates slots for the sources actually marked as media; mapVolumeGroup[media type] is left as is.
        int split = 0, nextType = FIRST_TYPE;
        for (int g : media) {
            if (!mediaType.equals(table.get(g))) continue;
            while (groups.containsKey(nextType)) nextType++;    // never reuse an existing type
            groups.put(nextType, new int[] { g });
            table.put(g, nextType);
            split++;
            nextType++;
        }

        mod.log("split " + split + " media volume groups out of type " + mediaType
                + " (types " + FIRST_TYPE + "-" + (nextType - 1) + ")");
    }
}