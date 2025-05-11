package com.clase.motorton.bd;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class BDMongo {
    MongoClient mongoClient = MongoConection.getMongoClient();
    MongoDatabase database = mongoClient.getDatabase("vehiculosApi");
    MongoCollection<Document> collection = database.getCollection("vehiculos");

    public List<String> ObtenerMarcas(String tipoVeh){
        List<String> marcas = new ArrayList<>();
        try {
            Document documento = collection.find().first();
            if (documento != null && documento.containsKey(tipoVeh)) {
                Document tipo = documento.get(tipoVeh, Document.class);
                if (tipo != null) {
                    marcas.addAll(tipo.keySet());
                }
            }
        } catch (Exception e) {
            Log.e("BDMongo", "Error al obtener marcas: " + e.getMessage());
        }
        return marcas;
    }

    public List<String> ObtenerModelos(String tipoVeh, String marca){
        List<String> modelos = new ArrayList<>();
        try {
            Document documento = collection.find().first();
            if (documento != null && documento.containsKey(tipoVeh)) {
                Document tipo = documento.get(tipoVeh, Document.class);
                if (tipo != null && tipo.containsKey(marca)) {
                    List<String> modelosList = tipo.getList(marca, String.class);
                    if (modelosList != null) {
                        modelos.addAll(modelosList);
                    }
                }
            }
        } catch (Exception e) {
            Log.e("BDMongo", "Error al obtener modelos: " + e.getMessage());
        }
        return modelos;
    }
}