package project.cards.objects.durak;

import project.cards.objects.CardIdsList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by omerpr on 30/01/2015.
 */
public class DurakYackPair implements CardIdsList {
    private String backCardId = null;
    private String frontCardId = null;

    public DurakYackPair(String backCardId) {
        setBackCardId(backCardId);
    }

    public String getBackCardId() {
        return backCardId;
    }

    public void setBackCardId(String backCardId) {
        this.backCardId = backCardId;
    }

    public String getFrontCardId() {
        return frontCardId;
    }

    public void setFrontCardId(String frontCardId) {
        this.frontCardId = frontCardId;
    }

    @Override
    public List<String> toList() {
        List<String> cards = new ArrayList<>(2);

        String card = getBackCardId();
        if(null != card) {
            cards.add(card);
        }
        card = getFrontCardId();
        if(null != card) {
            cards.add(card);
        }

        return cards;
    }
}
