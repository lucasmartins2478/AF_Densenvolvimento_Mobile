package com.example.notation;

public  class Note {
    public String texto;

    public Note() {} // Necessário para o Firestore

    public Note(String texto) {
        this.texto = texto;
    }
}
