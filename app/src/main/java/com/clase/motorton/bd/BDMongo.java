package com.clase.motorton.bd;

import android.util.Log;

import androidx.annotation.NonNull;

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
        return null;
    }

    public List<String> ObtenerModelos(String tipoVeh, String marca){
        return null;
    }
}