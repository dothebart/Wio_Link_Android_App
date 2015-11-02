package cc.seeed.iot.ui_setnode.model;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import java.util.List;

/**
 * Created by tenwong on 15/10/12.
 */
public class PinConfigDBHelper {
    public static List<PinConfig> getPinConfigs(String node_sn) {
        return new Select()
                .from(PinConfig.class)
                .where("node_sn = ?", node_sn)
                .orderBy("position")
                .execute();
    }

    public static List<PinConfig> getPinConfigs(int position, String node_sn) {
        return new Select()
                .from(PinConfig.class)
                .where("position = ? and node_sn = ?", position, node_sn)
                .execute();
    }

    public static void delPinConfig(String groveInstanceName, String node_sn) {
        new Delete()
                .from(PinConfig.class)
                .where("grove_instance_name = ? and node_sn = ?", groveInstanceName, node_sn)
                .execute();
    }

    public static void delPinConfig(String node_sn) {
        new Delete()
                .from(PinConfig.class)
                .where("node_sn = ?", node_sn)
                .execute();
    }

    public static void delPinConfigAll() {
        new Delete()
                .from(PinConfig.class)
                .execute();
    }
}
