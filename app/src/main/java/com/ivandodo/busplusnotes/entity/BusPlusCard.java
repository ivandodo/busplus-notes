package com.ivandodo.busplusnotes.entity;

/**
 * Created by ivan.radojevic on 25.06.2015..
 */
public class BusPlusCard {

    public enum Type {PREPAID, POSTPAID};

    private String name;
    private String serial;
    private Double ammount;
    private Type type;

    public BusPlusCard(String name, String serial, Double ammount, Type type) {
        this.name = name;
        this.serial = serial;
        this.ammount = ammount;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public Double getAmmount() {
        return ammount;
    }

    public void setAmmount(Double ammount) {
        this.ammount = ammount;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BusPlusCard entity = (BusPlusCard) o;

        if (serial != null ? !serial.equals(entity.serial) : entity.serial != null)
            return false;

        return true;
    }
}
