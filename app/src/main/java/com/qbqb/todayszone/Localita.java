package com.qbqb.todayszone;

public class Localita {

    public String COMUNE;
    public String REGIONE;

    public Localita(){}

    public Localita(String c, String r){
        COMUNE = c;
        REGIONE = r;
    }

    public void setCOMUNE(String c){ COMUNE = c; }
    public void setREGIONE(String r){ REGIONE = r; }
    public String getCOMUNE(){return COMUNE;}
    public String getREGIONE(){return REGIONE;}
}
