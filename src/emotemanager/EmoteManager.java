package emotemanager;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class EmoteManager {
    private Map<String, Icon> emoteMap;

    public EmoteManager() {
        emoteMap = new HashMap<>();
        emoteMap.put("Kappa", new ImageIcon("src/emotemanager/emotes/Kappa.png"));
        emoteMap.put("<3", new ImageIcon("src/emotemanager/emotes/3.png"));
        emoteMap.put("4Head", new ImageIcon("src/emotemanager/emotes/4Head.png"));
        emoteMap.put(":(", new ImageIcon("src/emotemanager/emotes/_(.png"));
        emoteMap.put(":)", new ImageIcon("src/emotemanager/emotes/_).png"));
        emoteMap.put("StinkyCheese", new ImageIcon("src/emotemanager/emotes/StinkyCheese.png"));
        emoteMap.put("TriHard", new ImageIcon("src/emotemanager/emotes/TriHard.png"));

    }

    public Map<String, Icon> getEmoteMap() {
        return emoteMap;
    }
}