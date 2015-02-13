package project.cards.objects.impl;

import project.cards.objects.CardIdsList;
import project.cards.objects.impl.Card;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by omerpr on 24/01/2015.
 */
public class Deck implements CardIdsList {

    private List<String> cards;

    public static final int MAX_SIZE = 52;

    public Deck() {
        cards = new ArrayList<>(MAX_SIZE);
        for(int i = 1,j;i<5;i++) {
            for(j = 2;j<15;j++) {
                cards.add(Card.getId(i, j));
            }

        }
    }

    public String get(int index) {
        return this.cards.get(index);
    }

    public void add(String cardId) {
        this.cards.add(cardId);
    }

    public String remove(int i) {
        return cards.remove(i);
    }

    public int size() {
        return this.cards.size();
    }

    @Override
    public List<String> toList() {
        return new ArrayList<>(cards);
    }
}
