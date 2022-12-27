package Server;

import Models.Movies;

import java.io.Serializable;
import java.util.ArrayList;

public class Packet implements Serializable{
    public String mesg;
    public String Icode="NA";
    public ArrayList<Movies> movies = new ArrayList<>();
    public ArrayList<Object> Data = new ArrayList<>();

    public Packet(){}

    public Packet(String mesg){
        this.mesg = mesg;
    }

    public Packet(String icode,String mesg){
        this.Icode = icode;
        this.mesg = mesg;
    }

    public Packet(String icode,ArrayList<Object> Data){
        this.Icode = icode;
        this.Data = Data;
    }

    public void setMesg(String msg){
        this.mesg = msg;
    }

    public void setMovies(ArrayList<Movies> mov){
        this.movies = mov;
    }

    public ArrayList<Movies> getMovies(){
        return this.movies;
    }

}