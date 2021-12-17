package net.corda.c5template.states;

import net.corda.v5.base.annotations.CordaSerializable;

@CordaSerializable
public class Item {

    private String id;
    private String name;
    private Integer cost;

    public Item(String id, String name, Integer cost) {
        this.id = id;
        this.name = name;
        this.cost = cost;
    }

    private Item() {
    }

    public String getItemNumber() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCost() {
        return cost;
    }

    public void setCost(Integer cost) {
        this.cost = cost;
    }
}
