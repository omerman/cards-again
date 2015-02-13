package project.cards.objects.impl;

import java.util.Comparator;

/**
 * Created by omerpr on 24/01/2015.
 */
public class Card implements Comparable<Card>{

    private int suit;
    private int rank;


    public Card(int rank,int suit) {
        setRank(rank);
        setSuit(suit);
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public void setSuit(int suit) {
        this.suit = suit;
    }

    public int getRank() {
        return rank;
    }

    public int getSuit() {
        return suit;
    }

    public String getId() {
        return getId(suit,rank);
    }

    public static String getId(int suit,int rank) {
        return rank+"_"+suit;
    }

    public static Card getById(String cId) {

        String[] rankSuit = cId.split("_");
        if(rankSuit.length < 2) { //TODO: throw error.
            return null;
        }

        return new Card(Integer.valueOf(rankSuit[0]),Integer.valueOf(rankSuit[1]));
    }

    @Override
    public int compareTo(Card o) {
        return Integer.valueOf(this.getRank()).compareTo(o.getRank());
    }

}
