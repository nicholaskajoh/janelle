package dev.terna.janelle.pager;

public class Page {
    String data;

    public Page(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return data;
    }
}
