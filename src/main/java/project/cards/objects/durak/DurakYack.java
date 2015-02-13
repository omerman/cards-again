package project.cards.objects.durak;

import project.cards.objects.Yack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by omerpr on 30/01/2015.
 */
public class DurakYack implements Yack {

    private List<DurakYackPair> yack;

    public DurakYack() {
        setYack(new ArrayList<DurakYackPair>());
    }

    public List<DurakYackPair> getYack() {
        return yack;
    }

    public void setYack(List<DurakYackPair> yack) {
        this.yack = yack;
    }

    public void addBackCardId(String backCardId) {
        yack.add(new DurakYackPair(backCardId));
    }

    public void addFrontCardId(int index, String frontCardId) {
        yack.get(index).setFrontCardId(frontCardId);
    }

    public void addFrontCardId(String backCardId, String frontCardId) {
        if(null != backCardId) {
            for (int i = 0; i < yack.size(); i++) {
                if (backCardId.equals(yack.get(i).getBackCardId())) {
                    addFrontCardId(i, frontCardId);
                    return;
                }
            }
        }
        throw new RuntimeException("backCardId: "+backCardId+" could not be found.");
    }

    @Override
    public List<String> toList() {
        List<String> cards = new ArrayList<>(yack.size()*2);
        for(DurakYackPair yackPair :yack) {
            cards.addAll(yackPair.toList());
        }
        return cards;
    }

    @Override
    public void clear() {
        setYack(new ArrayList<DurakYackPair>());
    }

    @Override
    public int size() {
        return yack.size();
    }
}
