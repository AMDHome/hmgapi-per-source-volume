package persourcevolume;

import java.util.Map;
import hmgapi.hook.ModContext;
import hmgapi.hook.ModEntry;

public final class PerSourceVolume extends ModEntry {
    @Override
    @SuppressWarnings("unchecked")
    public void zygoteInit(ModContext mod) throws Throwable {
        Class<?> c;
        Map<Integer, int[]> map;
        Object media;
        try {
            c = Class.forName("com.hkmc.media.MicomAudioManagerConstant");
            map = (Map<Integer, int[]>) c.getField("mapVolumeGroup").get(null);
            media = c.getField("arr_Group1_Media").get(null);
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            mod.log("no linked volume groups on this build, nothing to do: " + e);
            return;
        }
        for (Map.Entry<Integer, int[]> e : map.entrySet()) {
            if (e.getValue() == media) {             // same array object, not just equal contents
                e.setValue(new int[0]);              // empty, never null: the stock loop doesn't null-check
                mod.log("unlinked media volume groups (type " + e.getKey() + ")");
            }
        }
    }
}